package com.example.sample

import android.content.Context
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.roundToInt

private const val DEFAULT_COLUMN_WIDTH = 100
private const val DEFAULT_MAX_COLUMN_VALUE_OFFSET_TOP = 20
private const val DEFAULT_COLUMN_SPACING = 10

class Histogram : ConstraintLayout {

    private val dataSet: ArrayList<Pair<String, Number>> = arrayListOf()

    private val columnList: ArrayList<Pair<String, Column>> = arrayListOf()

    private var columnWidth = DEFAULT_COLUMN_WIDTH
    private var columnSpacing = DEFAULT_COLUMN_SPACING

    private var maxColumnValue: Number = 0
    private var maxColumnValueOffsetTop = DEFAULT_MAX_COLUMN_VALUE_OFFSET_TOP

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setData(dataSet: List<Pair<String, Number>>) {
        this.dataSet.clear()
        this.dataSet.addAll(dataSet)
        maxColumnValue = this.dataSet.maxWith(Comparator { first, second ->
            val firstValue = first.second.toDouble()
            val secondValue = second.second.toDouble()

            when {
                firstValue == secondValue -> 0
                firstValue < secondValue -> -1
                else -> 1
            }
        })?.second ?: 0

        createColumns()

        addColumns()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        createColumns()
    }

    private fun createColumns() {
        columnList.clear()

        var left = paddingStart.toFloat()
        var right = paddingLeft + columnWidth.toFloat()
        val bottom = height.toFloat() - paddingBottom

        dataSet.forEach {
            val top = (height - paddingBottom - paddingTop - maxColumnValueOffsetTop) * (it.second.toFloat() / maxColumnValue.toFloat())

            val column = Column(context).apply {
                setColumnColor(
                    Color.argb(
                        255,
                        155,
                        155,
                        155
                    )
                )
            }
            right += columnWidth
            left += columnWidth

            column.setColumnRect(RectF(left, top, right, bottom))
            columnList.add(Pair(it.first, column))

            right += columnSpacing
            left += columnSpacing
        }
    }

    private fun addColumns() {
        removeAllViews()

        columnList.forEach {
            addView(it.second)
        }
    }
}