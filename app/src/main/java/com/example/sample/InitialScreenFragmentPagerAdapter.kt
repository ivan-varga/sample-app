package com.example.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

private const val NUMBER_OF_ITEMS = 3

class InitialScreenFragmentPagerAdapter(private val layoutInflater: LayoutInflater) :
    RecyclerView.Adapter<InitialScreenFragmentPagerAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = layoutInflater.inflate(R.layout.fragment_initial_screen_layout, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int = NUMBER_OF_ITEMS

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.render(position)
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun render(position: Int) {

        }

    }

}