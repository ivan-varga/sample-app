package com.example.sample

import android.content.Context
import android.graphics.Color
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*

private const val DEFAULT_COLUMN_WIDTH = 100
private const val DEFAULT_MAX_COLUMN_VALUE_OFFSET_TOP = 20
private const val DEFAULT_COLUMN_SPACING = 10

class Histogram : ConstraintLayout {

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
        maxColumnValue = dataSet.maxWith(Comparator { first, second ->
            val firstValue = first.second.toDouble()
            val secondValue = second.second.toDouble()

            when {
                firstValue == secondValue -> 0
                firstValue < secondValue -> -1
                else -> 1
            }
        })?.second ?: 0

        createColumns(dataSet)

        applyConstraints()
    }

    /**
     * Top property grows larger as we move down the screen. This results in columns with lower values having larger top values.
     * This is why we use sortByDescending to sort ascending and sortBy to sort descending.
     */
    fun sortColumns(descending: Boolean = false) {
        if (descending) columnList.sortBy { it.second.top } else columnList.sortByDescending { it.second.top }
        applyConstraints()
    }

    private fun createColumns(dataSet: List<Pair<String, Number>>) {
        dataSet.forEachIndexed { index, pair ->
            val top = getColumnTop(pair.second)

            val column = getColumn(index).apply {
                setColumnTop(top)
            }

            if (columnList.size <= index) {
                columnList.add(Pair(pair.first, column))
                addView(column)
            }
        }
        removeUnusedColumns(dataSet.size)
    }

    private fun applyConstraints() {
        val set = ConstraintSet()
        set.clone(this)

        var previousColumn = columnList.firstOrNull()?.second ?: return

        //startToStartOf=parent, bottomToBottomOf=parent
        set.connect(previousColumn.id, START, PARENT_ID, START, 0)
        set.connect(previousColumn.id, BOTTOM, PARENT_ID, BOTTOM, 0)

        for (i in 1 until columnList.size) {
            val currentColumn = columnList[i].second

            //startToEndOf=previousColumn, bottomToBottomOf=parent
            set.connect(currentColumn.id, START, previousColumn.id, END, columnSpacing)
            set.connect(currentColumn.id, BOTTOM, PARENT_ID, BOTTOM, 0)

            previousColumn = currentColumn
        }

        applyWithAnimation(set)
    }

    private fun applyWithAnimation(endConstraintSet: ConstraintSet) {
        TransitionManager.beginDelayedTransition(this)
        endConstraintSet.applyTo(this)
    }

    /**
     * If there is already an existing column, reuse it.
     * Else create a new column
     */
    private fun getColumn(index: Int): Column =
        if (columnList.size > index) columnList[index].second
        else instantiateColumn()

    private fun instantiateColumn() = Column(context).apply {
        setColumnColor(columnColor)
        id = View.generateViewId()
        layoutParams = LayoutParams(columnWidth, height)
    }

    /**
     * If the new data set contains a smaller number of columns than the current number of columns, remove unused columns.
     */
    private fun removeUnusedColumns(index: Int) {
        columnList.filter { columnList.indexOf(it) > index }.forEach { removeView(it.second) }
        columnList.removeAll { columnList.indexOf(it) > index }
    }

    private fun getColumnTop(value: Number): Float =
        getAvailableHeight().toFloat() * (1f - getValueRatio(value)) + maxColumnValueOffsetTop

    private fun getAvailableHeight(): Int = height - paddingBottom - paddingTop

    private fun getValueRatio(value: Number) = value.toFloat() / maxColumnValue.toFloat()
}