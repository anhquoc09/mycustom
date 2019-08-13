package com.example.anhquoc.mycustom

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.*
import android.support.v7.widget.LinearLayoutManager.HORIZONTAL
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    private lateinit var listAdapter: ListAdapter

    private val snapHelper = StartPagerSnapHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val lm = LinearLayoutManager(this, HORIZONTAL, false)
        val lm = GridLayoutManager(this, 3, HORIZONTAL, false)
        listAdapter = ListAdapter(3)
        recyclerView.apply {
            layoutManager = lm
            adapter = listAdapter
            setHasFixedSize(true)
        }
        snapHelper.attachToRecyclerView(recyclerView)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                recyclerView.layoutManager?.let {
                    val snapView = snapHelper.findSnapView(it)
                    tvPosition.text = snapView?.let { view ->
                        it.getPosition(view).toString()
                    } ?: RecyclerView.NO_POSITION.toString()
                }
            }
        })
        val data = mutableListOf<String>()

        for (i in 1..20) {
            data.add(i.toString())
        }
        listAdapter.setData(data)

    }
}
