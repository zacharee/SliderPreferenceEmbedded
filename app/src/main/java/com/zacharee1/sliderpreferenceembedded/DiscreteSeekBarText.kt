package com.zacharee1.sliderpreferenceembedded

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
import java.util.*

class DiscreteSeekBarText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.discreteSeekBarStyle)
    : LinearLayout(context, attrs, defStyleAttr),
        DiscreteSeekBar.OnProgressChangeListener {

    private val seekBar: DiscreteSeekBar
    private val textView: TextView

    var scale = 1f //doesn't work for the popup view

    var listener: OnProgressChangeListener? = null

    var progress: Int
        get() = seekBar.progress
        set(progress) {
            seekBar.progress = progress
            text = progress.toString()
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

    var format: String
        get() = seekBar.indicatorFormatter
        set(value) {
            seekBar.indicatorFormatter = value
        }

    var text: String
        get() = textView.text.toString()
        set(value) {
            val scaled = (value.toInt() * scale).toDouble()

            val format = seekBar.indicatorFormatter
            val floatFormat: String

            floatFormat = if (scaled == scaled.toLong().toDouble()) {
                String.format(Locale.US, "%d", scaled.toLong())
            } else {
                String.format(Locale.US, "%.2f", scaled)
            }

            if (format == null) {
                textView.text = if (scale < 1) floatFormat else scaled.toString()
            } else {
                textView.text = String.format(format, if (scale < 1) floatFormat else scaled.toString())
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

    override fun onStartTrackingTouch(seekBar: DiscreteSeekBar) {
        listener?.onStartTrackingTouch(seekBar)
    }

    override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {
        listener?.onStopTrackingTouch(seekBar)
    }

    override fun onProgressChanged(seekBar: DiscreteSeekBar, value: Int, fromUser: Boolean) {
        listener?.onProgressChanged(seekBar, value, fromUser)

        text = value.toString()
    }

    interface OnProgressChangeListener {
        fun onProgressChanged(seekBar: DiscreteSeekBar, value: Int, fromUser: Boolean)
        fun onStopTrackingTouch(seekBar: DiscreteSeekBar)
        fun onStartTrackingTouch(seekBar: DiscreteSeekBar)
    }
}
