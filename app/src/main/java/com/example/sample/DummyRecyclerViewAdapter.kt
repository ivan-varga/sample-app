package com.example.sample

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class DummyRecyclerViewAdapter(private val inflater: LayoutInflater, private val itemClickListener: (Float) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DummyViewHolder(inflater.inflate(R.layout.dummy_recycler_view_item, parent, false), itemClickListener)
    }

    override fun getItemCount(): Int {
        return 100
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as DummyViewHolder).render()
    }

    private class DummyViewHolder(itemView: View, private val itemClickListener: (Float) -> Unit) : RecyclerView.ViewHolder(itemView) {

        fun render() {
            val value = (Math.random() * 255).roundToInt()
            val value1 = (Math.random() * 255).roundToInt()
            val value2 = (Math.random() * 255).roundToInt()
            itemView.setBackgroundColor(Color.argb(255, value, value1, value2))
            itemView.setOnClickListener { itemClickListener(it.x + it.width / 2f) }
        }
    }
}