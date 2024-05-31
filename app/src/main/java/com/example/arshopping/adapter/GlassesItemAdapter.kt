package com.example.arshopping.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arshopping.ArActivity
import com.example.arshopping.R
import com.example.arshopping.View3dActivity
import com.example.arshopping.model.Glasses
import com.google.android.material.card.MaterialCardView

class GlassesItemAdapter(private val dataSet: Array<Glasses>, private val context: Context) :
    RecyclerView.Adapter<GlassesItemAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Define click listener for the ViewHolder's View
        val title: TextView = view.findViewById(R.id.title)
        val previewImage:ImageView = view.findViewById(R.id.previewImage)
        val description:TextView = view.findViewById(R.id.description)
        val tryOnButton: Button = view.findViewById(R.id.tryOnButton)
        val card:MaterialCardView = view.findViewById(R.id.card)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.title.text = dataSet[position].title
        viewHolder.description.text = dataSet[position].description
        viewHolder.previewImage.setImageResource(dataSet[position].previewImageResource)


        fun open3d()
        {
            val intent = Intent(viewHolder.itemView.context, View3dActivity::class.java)
            intent.putExtra("curIndex",position)
            viewHolder.itemView.context.startActivity(intent)
        }
        viewHolder.card.setOnClickListener{
            open3d()
        }
        fun openFaceAr()
        {
            val intent = Intent(viewHolder.itemView.context, ArActivity::class.java)
            intent.putExtra("curIndex",position)
            viewHolder.itemView.context.startActivity(intent)
        }
        viewHolder.tryOnButton.setOnClickListener{
            openFaceAr()
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
