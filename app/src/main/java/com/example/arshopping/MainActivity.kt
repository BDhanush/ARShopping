package com.example.arshopping

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arshopping.adapter.GlassesItemAdapter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = GlassesItemAdapter(glassesArray,this)
        val recyclerView:RecyclerView = findViewById(R.id.recyclerView)
        val linearLayoutManager= LinearLayoutManager(this)
        recyclerView.layoutManager=linearLayoutManager
        recyclerView.adapter = adapter

    }
}