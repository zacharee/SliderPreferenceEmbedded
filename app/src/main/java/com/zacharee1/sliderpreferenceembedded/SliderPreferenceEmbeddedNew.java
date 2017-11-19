package com.zacharee1.sliderpreferenceembedded;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class SliderPreferenceEmbeddedNew extends Preference {
    private View mView;
    private OnPreferenceChangeListener mListener;
    private OnViewCreatedListener mViewListener;
    private int mProgress = -1;
    private int mMaxProgress = -1;
    private int mMinProgress = -1;

    private int mXmlProgress = 0;

    public DiscreteSeekBar seekBar;

    @TargetApi(21)
    public SliderPreferenceEmbeddedNew(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SliderPreferenceEmbeddedNew(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SliderPreferenceEmbeddedNew(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SliderPreferenceEmbedded,
                0, 0);

        try {
            mMaxProgress = a.getInteger(R.styleable.SliderPreferenceEmbedded_max, -1);
            mMinProgress = a.getInteger(R.styleable.SliderPreferenceEmbedded_min, -1);
            mXmlProgress = a.getInteger(R.styleable.SliderPreferenceEmbedded_default_progress, -1);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        setLayoutResource(R.layout.pref_view_embedded);
        setWidgetLayoutResource(R.layout.slider_pref_view_new);

        mView = super.onCreateView(parent);

        mProgress = (mXmlProgress == -1 ? getSavedProgress() : mXmlProgress);
        mMaxProgress = (mMaxProgress == -1 ? 100 : mMaxProgress);
        mMinProgress = (mMinProgress == -1 ? 0 : mMinProgress);

        seekBar = mView.findViewById(R.id.slider_pref_seekbar_discrete);
        seekBar.setMin(mMinProgress);
        seekBar.setMax(mMaxProgress);
        seekBar.setProgress(mProgress);

        seekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                setProgressWithoutBar(value);
                if (mListener != null) mListener.onPreferenceChange(SliderPreferenceEmbeddedNew.this, value);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                setProgressWithoutBar(seekBar.getProgress());
                if (mListener != null) mListener.onPreferenceChange(SliderPreferenceEmbeddedNew.this, seekBar.getProgress());
            }
        });

        if (mViewListener != null) mViewListener.viewCreated(this);
        return mView;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        if (mViewListener != null) mViewListener.viewBound(this);
    }

    @Override
    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        mListener = onPreferenceChangeListener;
    }

    public void setOnViewCreatedListener(OnViewCreatedListener listener) {
        mViewListener = listener;
    }

    public void setProgress(int progress) {
        if (seekBar != null) seekBar.setProgress(progress);

        setProgressWithoutBar(progress);
    }

    public void setMax(int max) {
        mMaxProgress = max;
        if (seekBar != null) seekBar.setMax(max);
    }

    public void setMin(int min) {
        mMinProgress = min;
        if (seekBar != null) seekBar.setMin(min);
    }

    public int getMax() {
        return mMaxProgress;
    }

    public int getMin() {
        return mMinProgress;
    }

    public int getProgress() {
        return mProgress;
    }

    private void setProgressWithoutBar(int progress) {
        setProgressState(progress);
        saveProgress(progress);
    }

    private void saveProgress(int progress) {
        getSharedPreferences().edit().putInt(getKey(), progress).apply();
    }

    private void setProgressState(int progress) {
        mProgress = progress;
    }

    private int getSavedProgress() {
        return getSharedPreferences().getInt(getKey(), 0);
    }

    public interface OnViewCreatedListener {
        void viewCreated(SliderPreferenceEmbeddedNew preferenceEmbeddedNew);
        void viewBound(SliderPreferenceEmbeddedNew preferenceEmbeddedNew);
    }
}
