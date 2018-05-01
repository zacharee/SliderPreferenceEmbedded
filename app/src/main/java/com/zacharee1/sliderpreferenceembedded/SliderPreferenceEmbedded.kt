package com.zacharee1.sliderpreferenceembedded

import android.content.Context
import android.preference.Preference
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar

class SliderPreferenceEmbedded(context: Context, private val attrs: AttributeSet) : Preference(context, attrs) {
    private var listener: Preference.OnPreferenceChangeListener? = null

    private lateinit var view: View
    private lateinit var seekBar: DiscreteSeekBarText

    var scale: Float //doesn't work for the popup view
        get() = seekBar.scale
        set(value) {
            seekBar.scale = value
        }

    var max: Int
        get() = seekBar.max
        set(max) {
            seekBar.max = max
        }

    var min: Int
        get() = seekBar.min
        set(min) {
            seekBar.min = min
        }

    var xml: Int = 0

    var progress: Int
        get() = seekBar.progress
        set(progress) {
            seekBar.progress = progress
            sharedPreferences.edit().putInt(key, progress).apply()
        }

    var format: String
        get() = seekBar.format
        set(value) {
            seekBar.format = value
        }

    var popupEnabled: Boolean
        get() = seekBar.popupIndicatorEnabled
        set(value) {
            seekBar.popupIndicatorEnabled = value
        }

    var textEnabled: Boolean
        get() = seekBar.textIndicatorEnabled
        set(value) {
            seekBar.textIndicatorEnabled = value
        }

    var viewListener: OnViewCreatedListener? = null

    private val savedProgress: Int
        get() = sharedPreferences.getInt(key, xml)

    override fun onCreateView(parent: ViewGroup): View? {
        layoutResource = R.layout.pref_view_embedded
        widgetLayoutResource = R.layout.slider_pref_view
        seekBar = view.findViewById(R.id.seekbar_view)
        view = super.onCreateView(parent)

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

        progress = if (progress == -1) savedProgress else progress
        max = if (max == -1) 100 else max
        min = if (min == -1) 0 else min

        seekBar.listener = object : DiscreteSeekBarText.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: DiscreteSeekBar, value: Int, fromUser: Boolean) {
                listener?.onPreferenceChange(this@SliderPreferenceEmbedded, value)
            }

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {
                listener?.onPreferenceChange(this@SliderPreferenceEmbedded, progress)
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
}
