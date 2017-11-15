package com.zacharee1.sliderpreferenceembedded;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class SliderPreferenceEmbedded extends Preference implements SeekBar.OnSeekBarChangeListener
{
    private View view;
    private OnPreferenceChangeListener mListener;
    private OnViewCreatedListener mViewListener;
    private int mProgress = -1;
    private int mMaxProgress = -1;
    private int mMinProgress = -1;

    private int mXmlProgress = 0;

    private boolean isPercentage = false;

    private TextView textView;
    private SeekBar seekBar;

    @TargetApi(21)
    public SliderPreferenceEmbedded(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SliderPreferenceEmbedded(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SliderPreferenceEmbedded(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SliderPreferenceEmbedded,
                0, 0);

        try {
            mMaxProgress = a.getInteger(R.styleable.SliderPreferenceEmbedded_max, -1);
            mMinProgress = a.getInteger(R.styleable.SliderPreferenceEmbedded_min, -1);
            mXmlProgress = a.getInteger(R.styleable.SliderPreferenceEmbedded_default_progress, -1);

            isPercentage = a.getBoolean(R.styleable.SliderPreferenceEmbedded_percentage, false);
        } finally {
            a.recycle();
        }
    }

    public SliderPreferenceEmbedded(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent)
    {
        setLayoutResource(R.layout.pref_view_embedded);
        setWidgetLayoutResource(R.layout.slider_pref_view);

        final View view = super.onCreateView(parent);

        this.view = view;

        mProgress = (mProgress == -1 ? getSavedProgress() : mProgress);
        mMaxProgress = (mMaxProgress == -1 ? 100 : mMaxProgress);
        mMinProgress = (mMinProgress == -1 ? 0 : mMinProgress);

        seekBar = view.findViewById(R.id.slider_pref_seekbar);
        textView = view.findViewById(R.id.slider_pref_text);

        seekBar.setMax(mMaxProgress);
        seekBar.setProgress(mProgress);
        textView.setText(String.valueOf(mProgress));

        seekBar.setOnSeekBarChangeListener(this);

        if (mViewListener != null) mViewListener.viewCreated();

        return view;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (progress < mMinProgress) {
            progress = mMinProgress;
        }

        setProgress(progress);

        if (mListener != null) mListener.onPreferenceChange(SliderPreferenceEmbedded.this, progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar.getProgress() < mMinProgress) {
            setProgress(mMinProgress);

            if (mListener != null) mListener.onPreferenceChange(SliderPreferenceEmbedded.this, mMinProgress);
        }
    }

    @Override
    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener)
    {
        mListener = onPreferenceChangeListener;
    }

    public void setOnViewCreatedListener(OnViewCreatedListener listener) {
        mViewListener = listener;
    }

    public void setProgress(final int progress) {
        if (seekBar != null) {
            seekBar.setProgress(progress);
        }
        setProgressWithoutSeekBar(progress);
    }

    private void setProgressWithoutSeekBar(int progress) {
        setProgressState(progress);
        saveProgress(progress);
        setText(progress);
    }

    public void setProgressState(int progress) {
        mProgress = progress;
    }

    public void setMaxProgess(int maxProgess) {
        mMaxProgress = maxProgess;
        if (seekBar != null) {
            seekBar.setMax(maxProgess);
        }
    }

    public void setMinProgress(int minProgress) {
        mMinProgress = minProgress;
    }

    public void setIsPercentage(boolean isPercentage) {
        this.isPercentage = isPercentage;

        setText(getCurrentProgress());
    }

    public boolean getIsPercentage() {
        return isPercentage;
    }

    private void setText(int progress) {
        if (textView != null) {
            textView.setText(String.valueOf(progress).concat(isPercentage && !textView.getText().toString().contains("%") ? "%" : ""));
        }
    }

    public View getView() {
        return view;
    }

    @SuppressWarnings("WeakerAccess")
    public int getSavedProgress() {
        return getSharedPreferences().getInt(getKey(), mXmlProgress);
    }

    @SuppressWarnings("WeakerAccess")
    public int getCurrentProgress() {
        return ((SeekBar) view.findViewById(R.id.slider_pref_seekbar)).getProgress();
    }

    public void saveProgress(int progress) {
        getSharedPreferences().edit().putInt(getKey(), progress).apply();
    }

    public interface OnViewCreatedListener {
        void viewCreated();
    }
}
