package com.example.sample

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View

private const val DEFAULT_CORNER_RADIUS = 10f
private const val DEFAULT_HEIGHT_ANIMATOR_DURATION = 300L

class Column : View {

    private var columnCornerRadius = DEFAULT_CORNER_RADIUS

    private var top: Float = 0f
    private var currentTop: Float = 0f

    private val columnPaint: Paint = Paint().apply {
        isAntiAlias = true
        color = Color.GREEN
        style = Paint.Style.FILL_AND_STROKE
    }

    private lateinit var heightAnimator: ValueAnimator
    private var heightAnimatorDuration: Long = DEFAULT_HEIGHT_ANIMATOR_DURATION

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        background = ColorDrawable(Color.RED)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRoundRect(0f, currentTop, width.toFloat(), bottom.toFloat(), columnCornerRadius, columnCornerRadius, columnPaint)
    }

    fun setColumnTop(top: Float) {
        this.top = top
        initAnimator()

        heightAnimator.start()
    }

    private fun initAnimator() {
        if (::heightAnimator.isInitialized) {
            heightAnimator.setFloatValues(currentTop, top)
        } else {
            heightAnimator = ValueAnimator.ofFloat(height.toFloat(), top).apply {
                duration = heightAnimatorDuration
                addUpdateListener {
                    currentTop = it.animatedValue as Float
                    invalidate()
                }
            }
        }
    }

    fun setColumnCornerRadius(radius: Float) {
        this.columnCornerRadius = radius
    }

    fun setColumnColor(color: Int) {
        this.columnPaint.color = color
    }
}