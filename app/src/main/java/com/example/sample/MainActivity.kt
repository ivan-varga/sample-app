package com.example.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

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

        columnGraph.setData(listOf(Pair("asd", 12), Pair("", 321), Pair("asd", 14), Pair("", 32), Pair("asd", 120), Pair("", 300)))
    }
}
