package com.example.sample

import android.content.Context
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout

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

    private var columnColor: Int = Color.GREEN

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
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        createColumns()
    }

    private fun createColumns() {

        var left = paddingStart.toFloat()
        var right = paddingLeft + columnWidth.toFloat()
        val bottom = height.toFloat() - paddingBottom

        var index = 0
        dataSet.forEach {
            val top = getColumnTop(it.second)

            if (columnList.size > index) {
                val column = columnList[index].second
                column.setColumnRect(RectF(left, top, right, bottom))
            } else {
                val column = Column(context).apply {
                    setColumnColor(columnColor)
                    setColumnRect(RectF(left, top, right, bottom))
                }
                columnList.add(Pair(it.first, column))
                addView(column)
            }

            right += columnWidth + columnSpacing
            left += columnWidth + columnSpacing

            index++
        }

        removeUnusedColumns(index)
    }

    private fun removeUnusedColumns(index: Int) {
        var numberOfViewsRemoved = 0
        columnList.filter { columnList.indexOf(it) > index }.forEach {
            removeViewAt(columnList.indexOf(it) - numberOfViewsRemoved)
            numberOfViewsRemoved++
        }
        columnList.removeAll { columnList.indexOf(it) > index }
    }

    private fun getColumnTop(value: Number) =
        (height - paddingBottom - paddingTop).toFloat() * (1f - value.toFloat() / maxColumnValue.toFloat()) + maxColumnValueOffsetTop
}