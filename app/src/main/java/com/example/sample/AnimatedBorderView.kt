package com.example.sample

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.Nullable
import androidx.core.animation.doOnEnd
import androidx.core.graphics.ColorUtils

private const val START_INT_VALUE = 0
private const val END_INT_VALUE = 100

private const val DEFAULT_STROKE_WIDTH = 6
private const val DEFAULT_ARC_RADIUS = 32
private const val ARC_TO_LINE_DURATION_FRACTION = 1 / 6f
private const val ARC_SWEEP_ANGLE = 90f

/* ARC START ANGLES */
private const val TOP_LEFT_START_ANGLE = 225f
private const val TOP_RIGHT_START_ANGLE = 270f
private const val BOTTOM_LEFT_START_ANGLE = 180f
private const val BOTTOM_RIGHT_TOP_START_ANGLE = 0f
private const val BOTTOM_RIGHT_BOTTOM_START_ANGLE = 90f

private const val DEFAULT_COLOR = Color.WHITE
private const val DEFAULT_BACKGROUND_ANIMATION_DURATION = 300
private const val ALPHA_COMPONENT_MAX_VALUE = 255
/**
 * This const represents how many distinct points are in the border animation.
 * It is used to divide the duration between different animators since the arc animation shouldn't last as long as a border animation.
 * 2 = top and left edge; bottom and right edge
 * 3 = topLeftArc; bottomLeft and topRight arcs; bottomRightArc
 */
private const val NUMBER_OF_BORDER_ANIMATIONS = 2
private const val NUMBER_OF_CORNER_ANIMATIONS = 3

class AnimatedBorderView : View {

    private var hasFocus: Boolean = false

    private val defaultInterpolator = AccelerateDecelerateInterpolator()

    private var lineStrokeWidth: Int = DEFAULT_STROKE_WIDTH

    /* PAINT */
    private lateinit var linePaint: Paint
    private lateinit var backgroundPaint: Paint

    /* BACKGROUND */
    var backgroundColorFocused: Int = DEFAULT_COLOR
    var backgroundColorUnfocused: Int = DEFAULT_COLOR
    private var backgroundAnimationDuration: Int = DEFAULT_BACKGROUND_ANIMATION_DURATION
    private lateinit var backgroundRectF: RectF

    private val backgroundAnimator = ValueAnimator()
    private val backgroundAnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
        backgroundPaint.color = ColorUtils.setAlphaComponent(backgroundPaint.color, (it.animatedFraction * ALPHA_COMPONENT_MAX_VALUE).toInt())
        invalidate()
    }

    /* ARC */
    private var arcRadius = DEFAULT_ARC_RADIUS

    /* sweep angles */
    private var topLeftSweepAngle = 0f
    private var bottomRightSweepAngle = 0f
    private var bottomLeftSweepAngle = 0f
    private var topRightSweepAngle = 0f

    /* rectangles for drawing arcs */
    private lateinit var topLeftArcRectF: RectF
    private lateinit var topRightArcRectF: RectF
    private lateinit var bottomLeftArcRectF: RectF
    private lateinit var bottomRightArcRectF: RectF

    /* animators */
    private val leftTopArcAnimator = ValueAnimator()
    private val rightBottomArcAnimator = ValueAnimator()
    private val rightTopArcAnimator = ValueAnimator()
    private val leftBottomArcAnimator = ValueAnimator()

    /* listeners */
    private val leftTopArcAnimatorProgressUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        topLeftSweepAngle = ARC_SWEEP_ANGLE / 2 * animation.animatedFraction
        invalidate()
    }
    private val rightBottomArcAnimatorProgressUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        bottomRightSweepAngle = ARC_SWEEP_ANGLE / 2 * animation.animatedFraction
        invalidate()
    }
    private val rightTopArcAnimatorProgressUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        topRightSweepAngle = ARC_SWEEP_ANGLE * animation.animatedFraction
        invalidate()
    }
    private val leftBottomArcAnimatorProgressUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        bottomLeftSweepAngle = -ARC_SWEEP_ANGLE * animation.animatedFraction
        invalidate()
    }
    /* <--END ARC--> */

    /* BORDERS */

    /* colors */
    var borderColorCorrectState: Int = DEFAULT_COLOR
    var borderColorDefaultState: Int = DEFAULT_COLOR
    var borderColorErrorState: Int = DEFAULT_COLOR

    /* animators */
    private val leftTopProgressAnimator = ValueAnimator()
    private val rightBottomProgressAnimator = ValueAnimator()

    /* listeners */
    private val leftTopProgressUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        topHorizontalLineLength = (width - lineStrokeWidth - 2 * arcRadius) * animation.animatedFraction
        leftVerticalLineLength = (height - lineStrokeWidth - 2 * arcRadius) * animation.animatedFraction
        invalidate()
    }
    private val rightBottomProgressUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        bottomHorizontalLineLength = (width - lineStrokeWidth - 2 * arcRadius) * animation.animatedFraction
        rightVerticalLineLength = (height - lineStrokeWidth - 2 * arcRadius) * animation.animatedFraction
        invalidate()
    }

    /* line lengths */
    private var topHorizontalLineLength = 0f
    private var leftVerticalLineLength = 0f
    private var bottomHorizontalLineLength = 0f
    private var rightVerticalLineLength = 0f

    /* <--END BORDERS--> */

    /* CONSTRUCTORS */
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

        initAnimators()
    }

    private fun initPaints() {
        linePaint = Paint().apply {
            color = borderColorDefaultState
            strokeWidth = lineStrokeWidth.toFloat()
            style = Paint.Style.STROKE
        }

        backgroundPaint = Paint().apply {
            color = backgroundColorUnfocused
            strokeWidth = lineStrokeWidth.toFloat()
            style = Paint.Style.FILL
        }
    }

    private fun parseAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        attrs?.withTypedArray(context, R.styleable.AnimatedBorderView) {
            withColor(R.styleable.AnimatedBorderView_backgroundColorFocused, DEFAULT_COLOR) { backgroundColorFocused = it }
            withColor(R.styleable.AnimatedBorderView_backgroundColorUnfocused, DEFAULT_COLOR) { backgroundColorUnfocused = it }
            withColor(R.styleable.AnimatedBorderView_borderColorCorrect, DEFAULT_COLOR) { borderColorCorrectState = it }
            withColor(R.styleable.AnimatedBorderView_borderColorDefault, DEFAULT_COLOR) { borderColorDefaultState = it }
            withColor(R.styleable.AnimatedBorderView_borderColorError, DEFAULT_COLOR) { borderColorErrorState = it }
            withDimensionPixelSize(R.styleable.AnimatedBorderView_borderStrokeWidth, DEFAULT_STROKE_WIDTH) { setLineStrokeWidth(it) }
            withDimensionPixelSize(R.styleable.AnimatedBorderView_borderRadius, DEFAULT_ARC_RADIUS) { arcRadius = it }
            withInt(R.styleable.AnimatedBorderView_backgroundAnimationDuration) { backgroundAnimationDuration = it }
            withInt(R.styleable.AnimatedBorderView_borderAnimationDuration) { setBorderAnimationDuration(it.toLong()) }
        }
    }

    fun setLineStrokeWidth(width: Int) {
        lineStrokeWidth = width

        /**
         * Since line lengths account for line stroke width we need to reset them here
         */
        resetLineLengths()
    }

    private fun initAnimators() {
        //LINE ANIMATORS
        leftTopProgressAnimator.doOnEnd {
            rightTopArcAnimator.start()
            leftBottomArcAnimator.start()
        }
        rightBottomProgressAnimator.doOnEnd { rightBottomArcAnimator.start() }

        setIntAnimatorValues(leftTopProgressAnimator)
        setIntAnimatorValues(rightBottomProgressAnimator)

        leftTopProgressAnimator.addUpdateListener(leftTopProgressUpdateListener)
        rightBottomProgressAnimator.addUpdateListener(rightBottomProgressUpdateListener)

        leftTopProgressAnimator.interpolator = defaultInterpolator
        rightBottomProgressAnimator.interpolator = defaultInterpolator

        //ARC ANIMATORS
        leftTopArcAnimator.doOnEnd { leftTopProgressAnimator.start() }
        rightTopArcAnimator.doOnEnd { rightBottomProgressAnimator.start() }

        setIntAnimatorValues(leftTopArcAnimator)
        setIntAnimatorValues(rightBottomArcAnimator)
        setIntAnimatorValues(rightTopArcAnimator)
        setIntAnimatorValues(leftBottomArcAnimator)

        leftTopArcAnimator.addUpdateListener(leftTopArcAnimatorProgressUpdateListener)
        rightBottomArcAnimator.addUpdateListener(rightBottomArcAnimatorProgressUpdateListener)
        rightTopArcAnimator.addUpdateListener(rightTopArcAnimatorProgressUpdateListener)
        leftBottomArcAnimator.addUpdateListener(leftBottomArcAnimatorProgressUpdateListener)

        leftTopArcAnimator.interpolator = defaultInterpolator
        rightBottomArcAnimator.interpolator = defaultInterpolator
        rightTopArcAnimator.interpolator = defaultInterpolator
        leftBottomArcAnimator.interpolator = defaultInterpolator

        //BACKGROUND ANIMATOR
        setIntAnimatorValues(backgroundAnimator)
        backgroundAnimator.apply {
            duration = backgroundAnimationDuration.toLong()
            interpolator = defaultInterpolator
            addUpdateListener(backgroundAnimatorUpdateListener)
        }
    }

    private fun setIntAnimatorValues(animator: ValueAnimator, startIntValue: Int = START_INT_VALUE, endIntValue: Int = END_INT_VALUE) {
        animator.setIntValues(startIntValue, endIntValue)
    }

    /**
     * Since the animation consists of multiple animations played sequentially, we need to distribute that length among different animations.
     * As the arcs are much shorter than border lines, we assign more animation time to borders than to arcs.
     * The arc/line duration ratio is defined with [ARC_TO_LINE_DURATION_FRACTION].
     *
     * Note: Since the duration is defined in Long, and our calculations are in Float there is a relatively small rounding error when
     * distributing animation time.
     */
    fun setBorderAnimationDuration(duration: Long) {
        leftTopProgressAnimator.duration = calculateLineAnimationDuration(duration)
        rightBottomProgressAnimator.duration = calculateLineAnimationDuration(duration)

        leftTopArcAnimator.duration = calculateArcAnimationDuration(duration)
        rightBottomArcAnimator.duration = calculateArcAnimationDuration(duration)
        rightTopArcAnimator.duration = calculateArcAnimationDuration(duration)
        leftBottomArcAnimator.duration = calculateArcAnimationDuration(duration)
    }

    fun setBackgroundAnimationDuration(duration: Int) {
        backgroundAnimationDuration = duration
    }

    private fun calculateArcAnimationDuration(duration: Long): Long =
        ((duration / (NUMBER_OF_BORDER_ANIMATIONS / ARC_TO_LINE_DURATION_FRACTION + NUMBER_OF_CORNER_ANIMATIONS))).toLong()

    private fun calculateLineAnimationDuration(duration: Long): Long =
        (duration / (NUMBER_OF_BORDER_ANIMATIONS + NUMBER_OF_CORNER_ANIMATIONS.toDouble() * ARC_TO_LINE_DURATION_FRACTION)).toLong()

    /**
     * Shows default non-activated state without animated border
     */
    fun showDefaultState() {
        stopBorderAnimators()
        linePaint.color = borderColorDefaultState
        setFullArcSweepAngles()
        setFullLineLengths()
    }

    /**
     * Shows activated state when waiting for user input and validation check
     */
    fun showElevatedDefaultState() {
        stopBorderAnimators()
        linePaint.color = Color.TRANSPARENT
        setFullArcSweepAngles()
        setFullLineLengths()
    }

    /**
     * Shows error state with animated border
     */
    fun showErrorState() {
        stopBorderAnimators()
        linePaint.color = borderColorErrorState
        resetLineLengths()
        resetArcSweepAngles()
        startBorderAnimators()
    }

    /**
     * Shows correct state with animated border
     */
    fun showCorrectState() {
        stopBorderAnimators()
        linePaint.color = borderColorCorrectState
        resetLineLengths()
        resetArcSweepAngles()
        startBorderAnimators()
    }

    private fun startBorderAnimators() {
        leftTopArcAnimator.start()
    }

    private fun stopBorderAnimators() {
        leftTopArcAnimator.cancel()
        leftTopProgressAnimator.cancel()
        rightTopArcAnimator.cancel()
        leftBottomArcAnimator.cancel()
        rightBottomProgressAnimator.cancel()
        rightBottomArcAnimator.cancel()
    }

    private fun resetArcSweepAngles() {
        topLeftSweepAngle = 0f
        bottomRightSweepAngle = 0f
        bottomLeftSweepAngle = 0f
        topRightSweepAngle = 0f
    }

    private fun resetLineLengths() {
        topHorizontalLineLength = 0f
        leftVerticalLineLength = 0f
        bottomHorizontalLineLength = 0f
        rightVerticalLineLength = 0f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        initArcs()

        initBackgroundRect()

        setFullLineLengths()

        setFullArcSweepAngles()
    }

    private fun initArcs() {
        topLeftArcRectF = RectF(
            getHalfLineStrokeWidth(),
            getHalfLineStrokeWidth(),
            getHalfLineStrokeWidth() + 2 * arcRadius,
            getHalfLineStrokeWidth() + 2 * arcRadius
        )

        topRightArcRectF = RectF(
            width - getHalfLineStrokeWidth() - 2 * arcRadius,
            getHalfLineStrokeWidth(),
            width - getHalfLineStrokeWidth(),
            getHalfLineStrokeWidth() + 2 * arcRadius
        )
        bottomLeftArcRectF = RectF(
            getHalfLineStrokeWidth(),
            height - getHalfLineStrokeWidth() - 2 * arcRadius,
            getHalfLineStrokeWidth() + 2 * arcRadius,
            height - getHalfLineStrokeWidth()
        )
        bottomRightArcRectF = RectF(
            width - getHalfLineStrokeWidth() - 2 * arcRadius,
            height - getHalfLineStrokeWidth() - 2 * arcRadius,
            width - getHalfLineStrokeWidth(),
            height - getHalfLineStrokeWidth()
        )
    }

    private fun initBackgroundRect() {
        backgroundRectF = RectF(
            getHalfLineStrokeWidth(),
            getHalfLineStrokeWidth(),
            width - getHalfLineStrokeWidth(),
            height - getHalfLineStrokeWidth()
        )
    }

    override fun onDraw(canvas: Canvas) {
        /* DRAW BACKGROUND */
        canvas.drawRoundRect(backgroundRectF, arcRadius.toFloat(), arcRadius.toFloat(), backgroundPaint)

        /* DRAW ARCS */
        canvas.drawArc(topLeftArcRectF, TOP_LEFT_START_ANGLE, topLeftSweepAngle, false, linePaint)
        canvas.drawArc(topLeftArcRectF, TOP_LEFT_START_ANGLE, -topLeftSweepAngle, false, linePaint)
        canvas.drawArc(topRightArcRectF, TOP_RIGHT_START_ANGLE, topRightSweepAngle, false, linePaint)
        canvas.drawArc(bottomLeftArcRectF, BOTTOM_LEFT_START_ANGLE, bottomLeftSweepAngle, false, linePaint)
        canvas.drawArc(bottomRightArcRectF, BOTTOM_RIGHT_TOP_START_ANGLE, bottomRightSweepAngle, false, linePaint)
        canvas.drawArc(bottomRightArcRectF, BOTTOM_RIGHT_BOTTOM_START_ANGLE, -bottomRightSweepAngle, false, linePaint)

        /* DRAW LINES */
        canvas.drawLine(
            getHalfLineStrokeWidth() + arcRadius,
            getHalfLineStrokeWidth(),
            topHorizontalLineLength + arcRadius + getHalfLineStrokeWidth(),
            getHalfLineStrokeWidth(),
            linePaint
        )

        canvas.drawLine(
            getHalfLineStrokeWidth(),
            getHalfLineStrokeWidth() + arcRadius,
            getHalfLineStrokeWidth(),
            leftVerticalLineLength + arcRadius + getHalfLineStrokeWidth(),
            linePaint
        )

        canvas.drawLine(
            getHalfLineStrokeWidth() + arcRadius,
            height - getHalfLineStrokeWidth(),
            bottomHorizontalLineLength + arcRadius + getHalfLineStrokeWidth(),
            height - getHalfLineStrokeWidth(),
            linePaint
        )
        canvas.drawLine(
            width - getHalfLineStrokeWidth(),
            getHalfLineStrokeWidth() + arcRadius,
            width - getHalfLineStrokeWidth(),
            rightVerticalLineLength + arcRadius + getHalfLineStrokeWidth(),
            linePaint
        )
    }

    fun setFocus(focused: Boolean) {
        hasFocus = focused
    }

    private fun getHalfLineStrokeWidth(): Float = lineStrokeWidth / 2f

    private fun setFullLineLengths() {
        topHorizontalLineLength = getFullHorizontalLineLength()
        leftVerticalLineLength = getFullVerticalLineLength()
        bottomHorizontalLineLength = getFullHorizontalLineLength()
        rightVerticalLineLength = getFullVerticalLineLength()
    }

    private fun getFullHorizontalLineLength(): Float = (width - lineStrokeWidth - 2 * arcRadius).toFloat() + getHalfLineStrokeWidth()

    private fun getFullVerticalLineLength(): Float = (height - lineStrokeWidth - 2 * arcRadius).toFloat() + getHalfLineStrokeWidth()

    private fun setFullArcSweepAngles() {
        topLeftSweepAngle = ARC_SWEEP_ANGLE / 2
        bottomRightSweepAngle = ARC_SWEEP_ANGLE / 2
        topRightSweepAngle = ARC_SWEEP_ANGLE
        bottomLeftSweepAngle = -ARC_SWEEP_ANGLE
    }
}