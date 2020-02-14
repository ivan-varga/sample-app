package com.example.mintmobiledemoapp

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.FrameLayout
import androidx.annotation.Nullable
import androidx.core.view.children
import com.example.sample.AnimatedBorderView

class AnimatedBorderEditTextView : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onFinishInflate() {
        super.onFinishInflate()

        children.firstOrNull { it is EditText }?.apply { initEditText(this as EditText) }
    }

    private fun initEditText(editText: EditText) {
        editText.setOnFocusChangeListener { view, hasFocus ->
            val animatedBorderFrameLayout = children.firstOrNull { it is AnimatedBorderView } as AnimatedBorderView

            isSelected = hasFocus


            if (hasFocus) {
                animatedBorderFrameLayout.showElevatedDefaultState()
            } else {
                animatedBorderFrameLayout.showDefaultState()
            }
        }
    }
}