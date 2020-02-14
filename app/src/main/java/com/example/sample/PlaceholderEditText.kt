package com.example.sample

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Editable
import android.text.InputFilter
import android.text.Selection
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.EditText
import androidx.annotation.Nullable
import androidx.core.animation.doOnRepeat

private const val DEFAULT_NUMBER_OF_CHARACTERS = 10
private const val CHAR_SIZE = 16.0f
private const val LINE_SPACING = 14.0f
private const val STROKE_WIDTH = 2.0f
private const val SPACE_WIDTH = 6.0f
private const val CURSOR_WIDTH = 2.0f

private const val START_POSITION = 0
private const val OFFSET_ONE_CHARACTER = 1
private const val HALF_DIVIDER = 2

class PlaceholderEditText : EditText {

    private var startY = 0
    private var numberOfChars: Int = DEFAULT_NUMBER_OF_CHARACTERS
    private var charSize = CHAR_SIZE
    private var lineSpacing = LINE_SPACING
    private var spaceWidth = SPACE_WIDTH
    private var placeholderStrokeWidth = STROKE_WIDTH
    private var cursorWidth = CURSOR_WIDTH
    private lateinit var charWidths: FloatArray

    private lateinit var placeholderPaint: Paint
    private lateinit var textPaint: Paint
    private lateinit var cursorPaint: Paint

    private var cursorColor = Color.BLACK
    private var placeholderColor = Color.BLACK
    private var placeholderLightColor = Color.LTGRAY

    private var isInitialized = false

    private var blinkingCursorAnimator = ValueAnimator.ofInt(0, 1).apply {
        duration = 500
        repeatCount = ValueAnimator.INFINITE
        doOnRepeat { isCursorVisible = !isCursorVisible }
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

        adjustDimensionsForDensity(context.resources.displayMetrics.density)

        initPaint()

        initCharWidths()

        initLengthFilter()

        disablePopupDialog()

        initMultiline()

        blinkingCursorAnimator.start()

        isInitialized = true
    }

    private fun initMultiline() {
        maxLines = 1
    }

    private fun disablePopupDialog() {
        isLongClickable = false
        setTextIsSelectable(false)
    }

    private fun initLengthFilter() {
        filters = arrayOf(InputFilter.LengthFilter(numberOfChars))
    }

    private fun parseAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        attrs?.withTypedArray(context, R.styleable.PlaceholderEditText) {
            withInt(R.styleable.PlaceholderEditText_characterNumber) { numberOfChars = it }
            withColor(R.styleable.PlaceholderEditText_cursorColor) { cursorColor = it }
            withColor(R.styleable.PlaceholderEditText_placeholderColor) { placeholderColor = it }
        }
    }

    private fun initCharWidths() {
        charWidths = FloatArray(numberOfChars)
    }

    private fun adjustDimensionsForDensity(density: Float) {
        lineSpacing *= density
        placeholderStrokeWidth *= density
        cursorWidth *= density
        spaceWidth *= density
        charSize *= density
    }

    private fun initPaint() {
        placeholderPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = placeholderStrokeWidth
            strokeCap = Paint.Cap.ROUND
        }

        cursorPaint = Paint().apply {
            strokeWidth = cursorWidth
            color = cursorColor
        }

        textPaint = Paint(paint).apply {
            color = currentTextColor
        }
    }

    override fun onDraw(canvas: Canvas) {
        val text = text
        var startX = paddingLeft.toFloat()

        for (i in START_POSITION until numberOfChars) {

            if (text.isNotEmpty() && i < text.length) {
                placeholderPaint.color = placeholderLightColor
                canvas.drawLine(startX, getBottomY(), startX + charSize, getBottomY(), placeholderPaint)
                drawChar(canvas, text, startX, i, startY)
            } else {
                placeholderPaint.color = placeholderColor
                canvas.drawLine(startX, getBottomY(), startX + charSize, getBottomY(), placeholderPaint)
            }

            startX += charSize + spaceWidth
        }

        drawCursor(canvas)
    }

    private fun drawChar(canvas: Canvas, text: Editable, startX: Float, index: Int, bottom: Int) {
        val middle = startX + charSize / HALF_DIVIDER
        canvas.drawText(text, index, index + OFFSET_ONE_CHARACTER, middle - charWidths[index] / HALF_DIVIDER, bottom - lineSpacing, textPaint)
    }

    private fun drawCursor(canvas: Canvas) {
        val selectionStart = Selection.getSelectionStart(text)
        val selectionEnd = Selection.getSelectionEnd(text)
        if (selectionStart == selectionEnd && isFocused && isCursorVisible) {
            var cursorX = selectionStart * (charSize + spaceWidth) + charSize / HALF_DIVIDER + paddingLeft
            //If the cursor position is out of the view, correct the position to the end of the view
            if (cursorX >= width - cursorWidth / HALF_DIVIDER) cursorX = width - cursorWidth

            //If the cursor is in the middle of the text, set the position to the right side of the letter (not the center)
            if (selectionStart < text.length) cursorX -= charSize / HALF_DIVIDER
            if (selectionStart == START_POSITION) cursorX += cursorWidth / HALF_DIVIDER

            val top = getBottomY() - charSize - placeholderStrokeWidth
            val bottom = getBottomY() - placeholderStrokeWidth
            canvas.drawLine(cursorX, top, cursorX, bottom, cursorPaint)
        }
    }

    private fun getBottomY() = (height - paddingTop - paddingBottom + charSize) / HALF_DIVIDER.toFloat()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        startY = height - paddingBottom
        calculateCharSize()
    }

    private fun calculateCharSize() {
        val availableWidth = width - paddingLeft - paddingRight
        charSize = (availableWidth - spaceWidth * numberOfChars) / numberOfChars
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        if (isInitialized) {
            isCursorVisible = true
            calculateCharWidths(text)
        }
    }

    private fun calculateCharWidths(text: CharSequence) {
        (START_POSITION until text.length).forEach { i -> charWidths[i] = paint.measureText(text, i, i + OFFSET_ONE_CHARACTER) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isEnabled && isClickable && event.action == MotionEvent.ACTION_UP) {
            updateCursorPosition(event)
        }
        return super.onTouchEvent(event)
    }
    private fun updateCursorPosition(event: MotionEvent) {
        val currentX = event.x
        val textLength = text.length
        val currentSelection = getCurrentSelection(currentX)

        if (currentSelection < textLength) {
            setSelection(currentSelection)
        } else {
            setSelection(textLength)
        }
    }

    private fun getCurrentSelection(currentX: Float): Int {
        return if (currentX < charSize / HALF_DIVIDER) {
            START_POSITION
        } else (currentX / (charSize + spaceWidth)).toInt() + OFFSET_ONE_CHARACTER
    }

    fun setNumberOfChars(numOfChars: Int) {
        numberOfChars = if (numOfChars < 0) DEFAULT_NUMBER_OF_CHARACTERS else numOfChars
        initLengthFilter()
        initCharWidths()
        calculateCharSize()
        invalidate()
    }
}