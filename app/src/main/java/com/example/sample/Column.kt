package com.example.sample

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

private const val DEFAULT_CORNER_RADIUS = 10f

class Column : View {

    private var columnCornerRadius = DEFAULT_CORNER_RADIUS

    private var columnRectF = RectF()
    private var currentColumnRectF = RectF()

    private val columnPaint: Paint = Paint().apply {
        isAntiAlias = true
        color = Color.GREEN
        style = Paint.Style.FILL_AND_STROKE
    }

    private val heightAnimator = ValueAnimator.ofInt(0, 100).apply {
        addUpdateListener {
            with(columnRectF) {
                currentColumnRectF = RectF(left, bottom - top * animatedFraction, right, bottom)
                invalidate()
            }
        }
        duration = 1000
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRoundRect(currentColumnRectF, columnCornerRadius, columnCornerRadius, columnPaint)
    }

    fun setColumnRect(rectF: RectF) {
        this.columnRectF = rectF
        heightAnimator.start()
    }

    fun setColumnCornerRadius(radius: Float) {
        this.columnCornerRadius = radius
    }

    fun setColumnColor(color: Int) {
        this.columnPaint.color = color
    }
}