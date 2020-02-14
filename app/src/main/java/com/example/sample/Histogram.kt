package com.example.sample

import android.content.Context
import android.graphics.Color
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

    private fun createColumns() {
        dataSet.forEach {
            val column = Column(context).apply {
                setColumnColor(
                    Color.argb(
                        (Math.random() * 255).roundToInt(),
                        (Math.random() * 255).roundToInt(),
                        (Math.random() * 255).roundToInt(),
                        (Math.random() * 255).roundToInt()
                    )
                )
                setColumnWidth(columnWidth)
            }
            column.setColumnHeight((it.second.toDouble() / maxColumnValue.toDouble() * (height - maxColumnValueOffsetTop)).roundToInt())
            columnList.add(Pair(it.first, column))
        }
    }

    private fun addColumns() {
        columnList.forEach {
            addView(it.second)
        }
    }
}