package com.example.sample

import android.animation.ArgbEvaluator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import kotlin.math.min

private const val DEFAULT_STROKE_WIDTH = 10
private const val DEFAULT_START_ANGLE = 135f
private const val FULL_SWEEP_ANGLE = 270f
private const val DEFAULT_MAX_ARC_PROGRESS = 100f
private const val ALPHA_MAX_VALUE = 255
private const val GRADIENT_SHADER_ROTATION_DEGREES = 90f
private const val SHADER_GRADIENT_OFFSET = 0.1f
/**
 * To prevent progress from not appearing at all, we keep some small value so a dot is visible if progress is set to minimum
 */
private const val MIN_ARC_PROGRESS = 0.01f

/**
 * This class is used to show a gauge of certain progress. The progress can be anything as long as it's a float value.
 * You cannot set a negative progress or a negative max progress value.
 * When you set the maximum progress, to move the gauge, set current progress to something greater than zero and less than maxArcProgress.
 * Before setting the progress again you should wait the animation to finish as this class does not implement a progress queue.
 */
class CustomGaugeView : View {

    private lateinit var arcPaint: Paint
    private lateinit var arcBackgroundPaint: Paint

    private var arcStrokeWidth: Int = DEFAULT_STROKE_WIDTH
    private var startAngle = DEFAULT_START_ANGLE
    private var sweepAngle = 0f

    private var currentArcProgress = 0f
    private var maxArcProgress = DEFAULT_MAX_ARC_PROGRESS

    private var top: Float = arcStrokeWidth.toFloat()
    private var left: Float = arcStrokeWidth.toFloat()
    private var right: Float = arcStrokeWidth.toFloat()
    private var bottom: Float = arcStrokeWidth.toFloat()
    private var rectF = RectF(left, top, right, bottom)

    private var startArcColor: Int = 0
    private var endArcColor: Int = 0
    private var startArcOldColor: Int = 0
    private var endArcOldColor: Int = 0
    var startArcDefaultColor: Int = 0
    var endArcDefaultColor: Int = 0
    var arcBackgroundColor: Int = 0
    var arcBackgroundColorOpacity: Float = 0f

    private val progressAnimator = ValueAnimator()
    private val colorArgbEvaluator = ArgbEvaluator()
    private lateinit var sweepGradientShader: SweepGradient
    private val progressAnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        sweepAngle = FULL_SWEEP_ANGLE * (animation.animatedValue as Float / maxArcProgress)
        initShader(width, animation.animatedFraction, animation.animatedValue as Float)
        invalidate()
    }

    constructor(context: Context) : super(context) {
        init(null, 0, 0)
    }

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0, 0)
    }

    constructor(context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr, 0)
    }

    private fun init(@Nullable attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        parseAttributes(attrs, defStyleAttr, defStyleRes)
        initPaints()
        initAnimator()
    }

    private fun parseAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        attrs?.withTypedArray(context, R.styleable.CustomGaugeView) {
            withColor(R.styleable.CustomGaugeView_startDefaultColor) { startArcDefaultColor = it }
            withColor(R.styleable.CustomGaugeView_endDefaultColor) { endArcDefaultColor = it }
            withColor(R.styleable.CustomGaugeView_arcBackgroundColor) { arcBackgroundColor = it }
            withDimensionPixelSize(R.styleable.CustomGaugeView_strokeWidth) { arcStrokeWidth = it }
            withFloat(R.styleable.CustomGaugeView_arcBackgroundOpacity) { arcBackgroundColorOpacity = it }
        }
    }

    private fun initPaints() {
        arcPaint = Paint().apply {
            strokeCap = Paint.Cap.ROUND
            strokeWidth = arcStrokeWidth.toFloat()
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        arcBackgroundPaint = Paint().apply {
            color = arcBackgroundColor
            strokeCap = Paint.Cap.ROUND
            strokeWidth = arcStrokeWidth.toFloat()
            style = Paint.Style.STROKE
            alpha = (arcBackgroundColorOpacity * ALPHA_MAX_VALUE).toInt()
            isAntiAlias = true
        }

    }

    private fun initAnimator() {
        progressAnimator.addUpdateListener(progressAnimatorUpdateListener)
    }

    private fun initShader(size: Int, animatedFraction: Float, animatedValue: Float) {
        val startColor = colorArgbEvaluator.evaluate(animatedFraction, startArcOldColor, startArcColor) as Int
        val endColor = colorArgbEvaluator.evaluate(animatedFraction, endArcOldColor, endArcColor) as Int
        sweepGradientShader =
            SweepGradient(
                size / 2f,
                size / 2f,
                intArrayOf(startColor, endColor),
                floatArrayOf(SHADER_GRADIENT_OFFSET, animatedValue / maxArcProgress + SHADER_GRADIENT_OFFSET)
            )

        rotateShader(size)

        arcPaint.shader = sweepGradientShader
    }

    private fun rotateShader(size: Int) {
        val matrix = Matrix()
        sweepGradientShader.getLocalMatrix(matrix)
        matrix.postRotate(GRADIENT_SHADER_ROTATION_DEGREES, size / 2f, size / 2f)
        sweepGradientShader.setLocalMatrix(matrix)
    }

    /**
     * We always want out gauge to be in a rectangle so we take the smaller size from width and height and set the dimension accordingly.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        val size = min(width, height)

        setMeasuredDimension(size, size)
    }

    /**
     * First arc draws the gray background. Second one draws the progress.
     */
    override fun onDraw(canvas: Canvas) {
        canvas.drawArc(
            rectF,
            startAngle,
            FULL_SWEEP_ANGLE,
            false,
            arcBackgroundPaint
        )
        canvas.drawArc(
                rectF,
                startAngle,
                sweepAngle,
                false,
                arcPaint
            )
    }

    fun setMaximumProgress(progress: Float) {
        maxArcProgress = if (progress < 0) MIN_ARC_PROGRESS else progress
    }

    fun setCurrentProgress(progress: Float) {
        val progressValue = when {
            progress > maxArcProgress -> maxArcProgress
            progress < MIN_ARC_PROGRESS -> MIN_ARC_PROGRESS
            else -> progress
        }
        progressAnimator.setFloatValues(currentArcProgress, progressValue)
        initColors()
        currentArcProgress = progressValue
        progressAnimator.start()
    }

    private fun initColors() {
        saveOldColors()

        startArcColor = startArcDefaultColor
        endArcColor = endArcDefaultColor

        /**
         * If both colors are equal to zero that means old colors were not yet set.
         */
        if (startArcOldColor == 0 && endArcOldColor == 0) {
            saveOldColors()
        }
    }

    private fun saveOldColors() {
        startArcOldColor = startArcColor
        endArcOldColor = endArcColor
    }

    private fun getCurrentProgressFraction(): Float = currentArcProgress / maxArcProgress

    fun setInterpolator(interpolator: TimeInterpolator) {
        progressAnimator.interpolator = interpolator
    }

    fun setAnimationDuration(duration: Long) {
        progressAnimator.duration = duration
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val size = min(w, h)

        rectF.apply {
            top = arcStrokeWidth.toFloat()
            left = arcStrokeWidth.toFloat()
            right = size - arcStrokeWidth.toFloat()
            bottom = size - arcStrokeWidth.toFloat()
        }
    }
}