package com.zacharee1.sliderpreferenceembedded;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class DiscreteSeekBarText extends LinearLayout implements DiscreteSeekBar.OnProgressChangeListener {
    private DiscreteSeekBar mSeekBar;
    private TextView mTextView;

    private OnProgressChangeListener mListener;

    public DiscreteSeekBarText(Context context) {
        this(context, null);
    }

    public DiscreteSeekBarText(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.discreteSeekBarStyle);
    }

    public DiscreteSeekBarText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.seekbar_with_text, this);

        mSeekBar = findViewById(R.id.seekbar);
        mTextView = findViewById(R.id.textview);

        mSeekBar.setOnProgressChangeListener(this);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        @ColorInt int color = typedValue.data;

        mSeekBar.setThumbColor(color, color);
        mSeekBar.setScrubberColor(color);
        mSeekBar.setTrackColor(color);
        mSeekBar.setRippleColor(color);
    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
        if (mListener != null) mListener.onStartTrackingTouch(seekBar);
    }

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
        if (mListener != null) mListener.onStopTrackingTouch(seekBar);
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        if (mListener != null) mListener.onProgressChanged(seekBar, value, fromUser);

        setText(String.valueOf(value));
    }

    public void setOnProgressChangeListener(OnProgressChangeListener listener) {
        mListener = listener;
    }

    public void setProgress(int progress) {
        mSeekBar.setProgress(progress);
        setText(String.valueOf(progress));
    }

    public void setMin(int min) {
        mSeekBar.setMin(min);
    }

    public void setMax(int max) {
        mSeekBar.setMax(max);
    }

    public void setIndicatorFormatter(String formatter) {
        mSeekBar.setIndicatorFormatter(formatter);
    }

    public void setPopupIndicatorEnabled(boolean enabled) {
        mSeekBar.setIndicatorPopupEnabled(enabled);
    }

    public void setTextIndicatorEnabled(boolean enabled) {
        mTextView.setVisibility(enabled ? VISIBLE : GONE);
    }

    public DiscreteSeekBar getSeekBar() {
        return mSeekBar;
    }

    public int getProgress() {
        return mSeekBar.getProgress();
    }

    public int getMin() {
        return mSeekBar.getMin();
    }

    public int getMax() {
        return mSeekBar.getMax();
    }

    public boolean getPopupIndicatorEnabled() {
        return mSeekBar.getIndicatorPopupEnabled();
    }

    public boolean getTextIndicatorEnabled() {
        return mTextView.getVisibility() == VISIBLE;
    }

    private void setText(String text) {
        String format = mSeekBar.getIndicatorFormatter();
        if (format == null) {
            mTextView.setText(text);
        } else {
            mTextView.setText(String.format(format, text));
        }
    }

    public interface OnProgressChangeListener {
        void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser);
        void onStopTrackingTouch(DiscreteSeekBar seekBar);
        void onStartTrackingTouch(DiscreteSeekBar seekBar);
    }
}
