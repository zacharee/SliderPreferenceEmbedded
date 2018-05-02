package com.zacharee1.sliderpreferenceembedded

import android.content.Context
import android.preference.Preference
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
import java.util.*

class SliderPreferenceEmbedded(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
    private var listener: Preference.OnPreferenceChangeListener? = null
    var viewListener: OnViewCreatedListener? = null

    lateinit var view: View

    val seekBar: DiscreteSeekBarText

    init {
        seekBar = DiscreteSeekBarText(context)

        layoutResource = R.layout.pref_view_embedded
        widgetLayoutResource = R.layout.slider_pref_view

        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SliderPreferenceEmbedded,
                0, 0)

        try {
            seekBar.max = a.getInteger(R.styleable.SliderPreferenceEmbedded_seek_max, seekBar.max)
            seekBar.min = a.getInteger(R.styleable.SliderPreferenceEmbedded_seek_min, seekBar.min)
            seekBar.origProgress = a.getInteger(R.styleable.SliderPreferenceEmbedded_default_progress, 0)
            seekBar.format = a.getString(R.styleable.SliderPreferenceEmbedded_format) ?: seekBar.format
            seekBar.popupIndicatorEnabled = a.getBoolean(R.styleable.SliderPreferenceEmbedded_show_popup, seekBar.popupIndicatorEnabled)
            seekBar.textIndicatorEnabled = a.getBoolean(R.styleable.SliderPreferenceEmbedded_show_text, seekBar.textIndicatorEnabled)
            seekBar.scale = a.getFloat(R.styleable.SliderPreferenceEmbedded_scale, seekBar.scale)
        } finally {
            a.recycle()
        }
    }

    override fun onCreateView(parent: ViewGroup): View? {
        view = super.onCreateView(parent)
        view.findViewById<LinearLayout>(R.id.seekbar_wrapper).apply {
            if (seekBar.parent != null) (seekBar.parent as ViewGroup).removeAllViews()
            addView(seekBar)

            (seekBar.layoutParams as LinearLayout.LayoutParams).apply {
                weight = 1f
                width = LinearLayout.LayoutParams.WRAP_CONTENT
            }
        }

        seekBar.listener = object : OnProgressChangeListener {
            override fun onProgressChanged(seekBar: DiscreteSeekBar, value: Int, fromUser: Boolean) {
                listener?.onPreferenceChange(this@SliderPreferenceEmbedded, value)
            }

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {
                listener?.onPreferenceChange(this@SliderPreferenceEmbedded, seekBar.progress)
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar) {}
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
        : LinearLayout(context, null, R.attr.discreteSeekBarStyle),
            DiscreteSeekBar.OnProgressChangeListener {

        private val seekBar: DiscreteSeekBar
        private val textView: TextView
        private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        var scale = 1f //doesn't work for the popup view

        var listener: OnProgressChangeListener? = null

        var origProgress = 0

        var progress: Int
            get() = prefs.getInt(key, 0)
            set(progress) {
                seekBar.progress = progress
                text = progress.toString()
                prefs.edit().putInt(key, progress).apply()
            }

        var min: Int
            get() = seekBar.min
            set(min) {
                seekBar.min = min
            }

        var max: Int
            get() = seekBar.max
            set(max) {
                seekBar.max = max
            }

        var popupIndicatorEnabled: Boolean
            get() = seekBar.indicatorPopupEnabled
            set(enabled) {
                seekBar.indicatorPopupEnabled = enabled
            }

        var textIndicatorEnabled: Boolean
            get() = textView.visibility == View.VISIBLE
            set(enabled) {
                textView.visibility = if (enabled) View.VISIBLE else View.GONE
            }

        var format: String?
            get() = seekBar.indicatorFormatter
            set(value) {
                seekBar.indicatorFormatter = value
            }

        var text: String
            get() = textView.text.toString()
            set(value) {
                val scaled = (value.toDouble() * scale)

                val format = seekBar.indicatorFormatter
                val floatFormat: String

                floatFormat = if (scaled == scaled.toLong().toDouble()) {
                    String.format(Locale.US, "%d", scaled.toLong())
                } else {
                    String.format(Locale.US, "%.2f", scaled)
                }

                if (format == null) {
                    textView.text = if (scale < 1F) floatFormat else scaled.toInt().toString()
                } else {
                    textView.text = String.format(format, if (scale < 1F) floatFormat else scaled.toString())
                }
            }

        init {
            View.inflate(context, R.layout.seekbar_with_text, this)

            seekBar = findViewById(R.id.seekbar)
            textView = findViewById(R.id.textview)

            seekBar.setOnProgressChangeListener(this)

            val typedValue = TypedValue()
            val theme = context.theme
            theme.resolveAttribute(R.attr.colorAccent, typedValue, true)
            val color = typedValue.data

            seekBar.setThumbColor(color, color)
            seekBar.setScrubberColor(color)
            seekBar.setTrackColor(color)
            seekBar.setRippleColor(color)
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()

            if (progress == 0) progress = origProgress
        }

        override fun onStartTrackingTouch(seekBar: DiscreteSeekBar) {
            listener?.onStartTrackingTouch(seekBar)
        }

        override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {
            listener?.onStopTrackingTouch(seekBar)
        }

        override fun onProgressChanged(seekBar: DiscreteSeekBar, value: Int, fromUser: Boolean) {
            listener?.onProgressChanged(seekBar, value, fromUser)

            if (fromUser) progress = value
        }
    }

    interface OnProgressChangeListener {
        fun onProgressChanged(seekBar: DiscreteSeekBar, value: Int, fromUser: Boolean)
        fun onStopTrackingTouch(seekBar: DiscreteSeekBar)
        fun onStartTrackingTouch(seekBar: DiscreteSeekBar)
    }
}