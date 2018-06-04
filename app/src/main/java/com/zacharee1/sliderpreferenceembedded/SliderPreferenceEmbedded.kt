package com.zacharee1.sliderpreferenceembedded

import android.content.Context
import android.content.res.TypedArray
import android.preference.Preference
import android.preference.PreferenceManager
import android.support.v4.graphics.ColorUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.rey.material.widget.Slider
import java.util.*

class SliderPreferenceEmbedded(context: Context, attrs: AttributeSet) : DefaultValuePreference(context, attrs) {
    var viewListener: OnViewCreatedListener? = null

    lateinit var view: View

    var seekBar: DiscreteSeekBarText

    private var listener: Preference.OnPreferenceChangeListener? = null

    init {
        seekBar = DiscreteSeekBarText(context)

        layoutResource = R.layout.pref_view_embedded
        widgetLayoutResource = R.layout.slider_pref_view

        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SliderPreferenceEmbedded,
                0, 0)

        try {
            for (i in 0 until a.indexCount) {
                val attr = a.getIndex(i)

                when (attr) {
                    R.styleable.SliderPreferenceEmbedded_seek_max -> seekBar.max = a.getInteger(attr, 0)
                    R.styleable.SliderPreferenceEmbedded_seek_min -> seekBar.min = a.getInteger(attr, 0)
                    R.styleable.SliderPreferenceEmbedded_scale -> seekBar.scale = a.getFloat(attr, 1f)
                    R.styleable.SliderPreferenceEmbedded_format -> seekBar.format = a.getString(attr)
                    R.styleable.SliderPreferenceEmbedded_show_text -> seekBar.textIndicatorEnabled = a.getBoolean(attr, true)
                    R.styleable.SliderPreferenceEmbedded_discrete_mode -> seekBar.discrete = a.getBoolean(attr, false)

                    R.styleable.SliderPreferenceEmbedded_show_buttons -> {
                        if (!a.getBoolean(attr, true)) {
                            seekBar.up.visibility = View.GONE
                            seekBar.down.visibility = View.GONE
                        }
                    }
                    R.styleable.SliderPreferenceEmbedded_show_reset -> {
                        if (!a.getBoolean(attr, true)) {
                            seekBar.reset.visibility = View.GONE
                        }
                    }
                    R.styleable.SliderPreferenceEmbedded_use_accent_color_for_widget_items -> {
                        if (a.getBoolean(attr, false)) {
                            val colorAttr = context.theme.obtainStyledAttributes(TypedValue().data, intArrayOf(R.attr.colorAccent))
                            val color = colorAttr.getColor(0, 0)
                            colorAttr.recycle()
                            seekBar.up.setColorFilter(color)
                            seekBar.down.setColorFilter(color)
                            seekBar.reset.setColorFilter(color)
                        }
                    }
                }
            }
        } finally {
            a.recycle()
        }
    }

    override fun setDefaultValue(defaultValue: Any?) {
        super.setDefaultValue(defaultValue)
        onSetInitialValue(preferenceManager.sharedPreferences.contains(key), defaultValue)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Int {
        return a.getInt(index, 0)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        seekBar.progress = if (restorePersistedValue) preferenceManager.sharedPreferences.getInt(key, 0) else (defaultValue ?: "0").toString().toInt()
    }

    override fun onCreateView(parent: ViewGroup): View? {
        view = super.onCreateView(parent)
        view.findViewById<LinearLayout>(R.id.seekbar_wrapper).apply {
            seekBar = DiscreteSeekBarText(seekBar)
            addView(seekBar)

            (seekBar.layoutParams as LinearLayout.LayoutParams).apply {
                weight = 1f
                width = LinearLayout.LayoutParams.WRAP_CONTENT
            }
        }

        seekBar.listener = object : OnProgressChangeListener {
            override fun onProgressChanged(seekBar: Slider, fromUser: Boolean, oldPos: Float, newPos: Float, oldValue: Int, newValue: Int) {
                listener?.onPreferenceChange(this@SliderPreferenceEmbedded, newValue)
            }
        }

        viewListener?.viewCreated(this)

        return view
    }

    override fun onBindView(view: View) {
        super.onBindView(view)

        viewListener?.viewBound(this)
    }

    override fun setOnPreferenceChangeListener(onPreferenceChangeListener: Preference.OnPreferenceChangeListener) {
        listener = onPreferenceChangeListener
    }

    interface OnViewCreatedListener {
        fun viewCreated(preferenceEmbeddedNew: SliderPreferenceEmbedded)
        fun viewBound(preferenceEmbeddedNew: SliderPreferenceEmbedded)
    }

    inner class DiscreteSeekBarText constructor(context: Context)
        : LinearLayout(context, null), Slider.OnPositionChangeListener {

        val seekBar: Slider
        val textView: TextView
        val up: ImageView
        val down: ImageView
        val reset: ImageView

        private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        var scale = 1f //doesn't work for the popup view

        var listener: OnProgressChangeListener? = null

//        var defaultProgress = 0

        var progress: Int
            get() = prefs.getInt(key, -1)
            set(progress) {
                setProgress(progress, true, fromUser)
            }

        var min: Int
            get() = seekBar.minValue
            set(min) {
                seekBar.setValueRange(min, max, false)
                seekBar.setValue(progress.toFloat(), false)
            }

        var max: Int
            get() = seekBar.maxValue
            set(max) {
                seekBar.setValueRange(min, max, false)
                seekBar.setValue(progress.toFloat(), false)
            }

        var textIndicatorEnabled: Boolean
            get() = textView.visibility == View.VISIBLE
            set(enabled) {
                textView.visibility = if (enabled) View.VISIBLE else View.GONE
            }

        var discrete: Boolean
            get() = seekBar.discreteMode
            set(mode) {
                seekBar.discreteMode = mode
            }

        var format: String? = null

        var text: String
            get() = textView.text.toString()
            set(value) {
                val scaled = (value.toDouble() * scale)

                val floatFormat: String

                floatFormat = if (scaled == scaled.toLong().toDouble()) {
                    String.format(Locale.US, "%d", scaled.toLong())
                } else {
                    String.format(Locale.US, "%.2f", scaled)
                }

                if (format == null) {
                    textView.text = if (scale < 1F) floatFormat else scaled.toInt().toString()
                } else {
                    textView.text = String.format(format!!, if (scale < 1F) floatFormat else scaled.toInt().toString())
                }
            }

        private var fromUser = false

        constructor(seekBar: DiscreteSeekBarText) : this(seekBar.context) {
            format = seekBar.format
            scale = seekBar.scale
            listener = seekBar.listener
            progress = seekBar.progress
            min = seekBar.min
            max = seekBar.max
            textIndicatorEnabled = seekBar.textIndicatorEnabled
            discrete = seekBar.discrete
        }

        init {
            View.inflate(context, R.layout.seekbar_with_text, this)

            seekBar = findViewById(R.id.seekbar)
            textView = findViewById(R.id.textview)
            up = findViewById(R.id.up)
            down = findViewById(R.id.down)
            reset = findViewById(R.id.reset)

            seekBar.setOnPositionChangeListener(this)

            val accent = context.theme.obtainStyledAttributes(TypedValue().data, intArrayOf(R.attr.colorAccent))
            val color = accent.getColor(0, 0)
            accent.recycle()

            seekBar.setSecondaryColor(ColorUtils.setAlphaComponent(color, 0x33))

            up.setOnClickListener {
                if (progress < max) setProgress(progress + 1, true, false)
            }

            down.setOnClickListener {
                if (progress > min) setProgress(progress - 1, true, false)
            }

            reset.setOnClickListener {
                resetProgress()
            }
        }

        override fun onPositionChanged(view: Slider, fromUser: Boolean, oldPos: Float, newPos: Float, oldValue: Int, newValue: Int) {
            listener?.onProgressChanged(view, fromUser, oldPos, newPos, oldValue, newValue)

            this.fromUser = fromUser
            if (fromUser) progress = newValue
        }

        fun resetProgress() {
            setProgress(defaultValue.toString().toInt(), true, false)
        }

        fun setProgress(progress: Int, animate: Boolean, fromUser: Boolean) {
            if (!fromUser) seekBar.setValue(progress.toFloat(), animate)
            text = progress.toString()
            prefs.edit().putInt(key, progress).apply()
        }
    }

    interface OnProgressChangeListener {
        fun onProgressChanged(seekBar: Slider, fromUser: Boolean, oldPos: Float, newPos: Float, oldValue: Int, newValue: Int)
    }
}