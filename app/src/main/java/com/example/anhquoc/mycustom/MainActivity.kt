package com.example.anhquoc.mycustom

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MainAdapter.OnClickListener {

    companion object {
        const val TAG: String = "MainActivity"
    }

    private lateinit var mAdapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        provideData()
    }

    private fun initView() {
        mAdapter = MainAdapter()
        val layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        mainRecyclerView.layoutManager = layoutManager
        mainRecyclerView.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
        mainRecyclerView.adapter = mAdapter
        mAdapter.setOnClickListener(this)
    }

    private fun provideData() {
        val profile = Profile()
        val list = mutableListOf<Profile>()
        list.add(Profile())
        list.add(Profile(name = "Anh Quốc"))
        list.add(profile.copy(email = "anhquoc.haq09@gmail.com"))
        list.add(profile.copy(email = "anhquoc.haq09@gmail.com", name = "Anh Quốc"))
        mAdapter.setList(list)
    }

    override fun onItemClick(item: Profile) {
        Toast.makeText(this, item.name + " " + item.email, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
