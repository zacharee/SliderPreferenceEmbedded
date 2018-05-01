package com.zacharee1.sliderpreferenceembedded

import android.content.Context
import android.preference.Preference
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar

class SliderPreferenceEmbedded(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
    private lateinit var view: View
    private lateinit var seekBar: DiscreteSeekBarText

    private var scale = 1f //doesn't work for the popup view

    private var popupEnabled = true
    private var textEnabled = false

    private var format: String

    private var listener: Preference.OnPreferenceChangeListener? = null
    private var viewListener: OnViewCreatedListener? = null

    var max: Int = -1
        set(max) {
            field = max
            seekBar.max = max
        }

    var min: Int = -1
        set(min) {
            field = min
            seekBar.min = min
        }

    var xml: Int = 0

    var progress: Int = -1
        set(progress) {
            field = progress
            seekBar.progress = progress

            setProgressWithoutBar(progress)
        }

    private val savedProgress: Int
        get() = sharedPreferences.getInt(key, xml)

    init {
        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SliderPreferenceEmbedded,
                0, 0)

        try {
            max = a.getInteger(R.styleable.SliderPreferenceEmbedded_seek_max, -1)
            min = a.getInteger(R.styleable.SliderPreferenceEmbedded_seek_min, -1)
            xml = a.getInteger(R.styleable.SliderPreferenceEmbedded_default_progress, -1)
            format = a.getString(R.styleable.SliderPreferenceEmbedded_format)
            popupEnabled = a.getBoolean(R.styleable.SliderPreferenceEmbedded_show_popup, popupEnabled)
            textEnabled = a.getBoolean(R.styleable.SliderPreferenceEmbedded_show_text, textEnabled)
            scale = a.getFloat(R.styleable.SliderPreferenceEmbedded_scale, scale)
        } finally {
            a.recycle()
        }
    }

    override fun onCreateView(parent: ViewGroup): View {
        layoutResource = R.layout.pref_view_embedded
        widgetLayoutResource = R.layout.slider_pref_view

        view = super.onCreateView(parent)

        this.progress = if (this.progress == -1) savedProgress else this.progress
        max = if (max == -1) 100 else max
        min = if (min == -1) 0 else min

        seekBar = view.findViewById(R.id.seekbar_view)
        seekBar.setScale(scale)
        seekBar.min = min
        seekBar.max = max
        seekBar.setIndicatorFormatter(format)
        seekBar.progress = this.progress
        seekBar.textIndicatorEnabled = textEnabled
        seekBar.popupIndicatorEnabled = popupEnabled

        seekBar.setOnProgressChangeListener(object : DiscreteSeekBarText.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: DiscreteSeekBar, value: Int, fromUser: Boolean) {
                setProgressWithoutBar(value)
                listener?.onPreferenceChange(this@SliderPreferenceEmbedded, value)
            }

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {
                setProgressWithoutBar(seekBar.progress)
                listener?.onPreferenceChange(this@SliderPreferenceEmbedded, seekBar.progress)
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar) {}
        })

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

    fun setOnViewCreatedListener(listener: OnViewCreatedListener) {
        viewListener = listener
    }

    fun setPopupEnabled(enabled: Boolean) {
        popupEnabled = enabled
        seekBar.popupIndicatorEnabled = enabled
    }

    fun setTextEnabled(enabled: Boolean) {
        textEnabled = enabled
        seekBar.textIndicatorEnabled = enabled
    }

    fun setScale(scale: Float) {
        this.scale = scale
        seekBar.setScale(scale)
    }

    fun getPopupEnabled(): Boolean {
        return seekBar.popupIndicatorEnabled
    }

    fun getTextEnabled(): Boolean {
        return seekBar.textIndicatorEnabled
    }

    private fun setProgressWithoutBar(progress: Int) {
        setProgressState(progress)
        saveProgress(progress)
    }

    private fun saveProgress(progress: Int) {
        sharedPreferences.edit().putInt(key, progress).apply()
    }

    private fun setProgressState(progress: Int) {
        this.progress = progress
    }

    interface OnViewCreatedListener {
        fun viewCreated(preferenceEmbeddedNew: SliderPreferenceEmbedded)
        fun viewBound(preferenceEmbeddedNew: SliderPreferenceEmbedded)
    }
}
