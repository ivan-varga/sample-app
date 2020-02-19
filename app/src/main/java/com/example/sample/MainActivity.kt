package com.example.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutManager = LinearLayoutManager(baseContext, RecyclerView.HORIZONTAL, false)
        val adapter = DummyRecyclerViewAdapter(layoutInflater) { recyclerViewPositionIndicator.animateToXCoordinate(it) }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
        adapter.notifyDataSetChanged()

        recyclerViewPositionIndicator.attachToRecyclerView(recyclerView)

        setDataButton.setOnClickListener {
            val list = arrayListOf<Pair<String, Double>>()
            repeat((Math.random() * 8 + 30).roundToInt()) { list.add(Pair("asd", 255 * Math.random())) }
            columnGraph.setData(list)
        }

        sortDataButton.setOnClickListener {
            columnGraph.sortColumns(false)
        }
    }
}
