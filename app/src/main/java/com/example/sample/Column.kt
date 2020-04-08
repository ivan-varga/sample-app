package com.example.sample

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

private const val DEFAULT_CORNER_RADIUS = 10f
private const val DEFAULT_HEIGHT_ANIMATOR_DURATION = 300L

class Column : View {

    private var columnCornerRadius = DEFAULT_CORNER_RADIUS

    var top: Float = 0f
        private set

    private var currentTop: Float = 0f

    private val columnPaint: Paint = Paint().apply {
        isAntiAlias = true
        color = Color.GREEN
        style = Paint.Style.FILL_AND_STROKE
    }

    private var heightAnimatorDuration: Long = DEFAULT_HEIGHT_ANIMATOR_DURATION

    private val heightAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 0f).apply {
        duration = heightAnimatorDuration
        addUpdateListener {
            currentTop = it.animatedValue as Float
            invalidate()
        }
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRoundRect(0f, currentTop, width.toFloat(), bottom.toFloat(), columnCornerRadius, columnCornerRadius, columnPaint)
    }

    fun setColumnTop(top: Float) {
        this.top = top

        heightAnimator.setFloatValues(currentTop, top)

        heightAnimator.start()
    }

    fun setColumnCornerRadius(radius: Float) {
        this.columnCornerRadius = radius
    }

    fun setColumnColor(color: Int) {
        this.columnPaint.color = color
    }
}