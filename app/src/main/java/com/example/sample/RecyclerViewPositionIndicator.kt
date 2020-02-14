package com.example.sample

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

private const val DOUBLE_DIVIDER = 2f
private const val ALPHA_MAX = 255
private const val DEFAULT_ANIMATOR_DURATION = 400L

class RecyclerViewPositionIndicator : View {

    private var xCoordinate: Float = 0f

    private var top: Float = 0f
    private var left: Float = 0f

    private var offsetStart = 0
    private var offsetEnd = 0

    private lateinit var indicatorBitmap: Bitmap

    private lateinit var pointerPaint: Paint

    private lateinit var positionAnimator: ValueAnimator
    private var animatorDuration = DEFAULT_ANIMATOR_DURATION

    constructor(context: Context) : super(context) {
        init(null, 0, 0)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init(attributeSet, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr, 0)
    }

    private fun init(@Nullable attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        attrs?.withTypedArray(context, R.styleable.RecyclerViewPositionIndicator) {
            withReference(R.styleable.RecyclerViewPositionIndicator_indicatorDrawable) {
                indicatorBitmap = drawableToBitmap(it)
                xCoordinate = indicatorBitmap.width / DOUBLE_DIVIDER
            }
            withDimensionPixelSize(R.styleable.RecyclerViewPositionIndicator_indicatorOffsetStart, 0) { offsetStart = it }
            withDimensionPixelSize(R.styleable.RecyclerViewPositionIndicator_indicatorOffsetEnd, 0) { offsetEnd = it }
        }
        pointerPaint = Paint()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        top = h - indicatorBitmap.height.toFloat()
        left = offsetStart.toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = indicatorBitmap.height

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(indicatorBitmap, left, top, pointerPaint)
        super.onDraw(canvas)
    }

    private fun setXCoordinate(xCoordinate: Float) {
        this.xCoordinate = xCoordinate

        left = xCoordinate - indicatorBitmap.width / DOUBLE_DIVIDER

        if (left - offsetStart < 0 || left + indicatorBitmap.width + offsetStart > width) {
            if (left - offsetStart < 0) {
                left = offsetStart.toFloat()

                pointerPaint.alpha =
                    (ALPHA_MAX * ((xCoordinate + indicatorBitmap.width / DOUBLE_DIVIDER) / indicatorBitmap.width)).roundToInt()
            }
            if (left + indicatorBitmap.width > width - offsetEnd) {
                left = width - indicatorBitmap.width.toFloat() - offsetEnd

                pointerPaint.alpha =
                    (ALPHA_MAX * ((width - xCoordinate + indicatorBitmap.width / DOUBLE_DIVIDER) / indicatorBitmap.width)).roundToInt()
            }
        } else {
            pointerPaint.alpha = ALPHA_MAX
        }

        invalidate()
    }

    fun animateToXCoordinate(newXCoordinate: Float) {
        if (::positionAnimator.isInitialized) {
            positionAnimator.cancel()
        }
        var localCoordinate = xCoordinate

        positionAnimator = ValueAnimator.ofFloat(localCoordinate, newXCoordinate).apply {
            addUpdateListener {
                consumeDx((it.animatedFraction * (localCoordinate - newXCoordinate)).roundToInt())
                localCoordinate -= (it.animatedFraction * (localCoordinate - newXCoordinate)).roundToInt()
            }
            duration = animatorDuration
        }
        positionAnimator.start()
    }

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                consumeDx(dx)
            }
        })
    }

    private fun consumeDx(dX: Int) {
        setXCoordinate(xCoordinate - dX)
    }

    fun setAnimationDuration(animationDuration: Long) {
        animatorDuration = animationDuration
    }
}