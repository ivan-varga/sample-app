package com.example.sample

import android.content.Context
import android.graphics.Color
import android.transition.TransitionManager
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

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

    private var columnColor: Int = Color.rgb(50, 168, 82)

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

        applyConstraints()
    }

    fun sortColumns(descending: Boolean = true) {
        if (descending) columnList.sortBy { it.second.top } else columnList.sortByDescending { it.second.top }
        applyConstraints()
    }

    private fun createColumns() {
        var index = 0
        dataSet.forEach {
            val top = getColumnTop(it.second)

            val column = getColumn(index)
            column.apply {
                setColumnColor(columnColor)
                id = column.hashCode()
            }

            column.setColumnTop(top)

            if (columnList.size <= index) {
                columnList.add(Pair(it.first, column))
                addView(column)
            }
            index++

        }
        removeUnusedColumns(index)
    }

    private fun applyConstraints() {
        val set = ConstraintSet()
        set.clone(this)

        var previousColumn = columnList.firstOrNull()?.second ?: return
        set.connect(previousColumn.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
        set.connect(previousColumn.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)

        for (i in 1 until columnList.size) {
            val currentColumn = columnList[i].second
            set.connect(currentColumn.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
            set.connect(currentColumn.id, ConstraintSet.START, previousColumn.id, ConstraintSet.END, columnSpacing)
            previousColumn = currentColumn
        }

        animateColumnReordering(set)
    }

    private fun animateColumnReordering(endConstraintSet: ConstraintSet) {
        TransitionManager.beginDelayedTransition(this)
        endConstraintSet.applyTo(this)
    }

    /**
     * If there is already an existing column, reuse it.
     * Else create a new column
     */
    private fun getColumn(index: Int): Column =
        if (columnList.size > index) columnList[index].second
        else Column(context).apply { layoutParams = LayoutParams(columnWidth, height) }

    /**
     * If the new data set contains a smaller number of columns than the current number of columns, remove unused columns.
     */
    private fun removeUnusedColumns(index: Int) {
        var numberOfViewsRemoved = 0
        columnList.filter { columnList.indexOf(it) > index }.forEach {
            removeView(it.second)
            numberOfViewsRemoved++
        }
        columnList.removeAll { columnList.indexOf(it) > index }
    }

    private fun getColumnTop(value: Number) =
        (height - paddingBottom - paddingTop).toFloat() * (1f - value.toFloat() / maxColumnValue.toFloat()) + maxColumnValueOffsetTop
}