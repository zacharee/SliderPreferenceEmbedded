package com.zacharee1.sliderpreferenceembedded

import android.content.Context
import android.os.Build
import android.preference.Preference
import android.support.annotation.RequiresApi
import android.util.AttributeSet

open class DefaultValuePreference : Preference {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    val defaultValue: Any
        get() {
            val mDefaultProgress = Preference::class.java.getDeclaredField("mDefaultValue")
            mDefaultProgress.isAccessible = true

            return mDefaultProgress.get(this)
        }
}