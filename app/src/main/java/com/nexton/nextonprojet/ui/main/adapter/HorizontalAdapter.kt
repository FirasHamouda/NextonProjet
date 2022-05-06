package com.nexton.nextonprojet.ui.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.nexton.nextonprojet.R
import com.squareup.picasso.Picasso

class HorizontalAdapter (val list: ArrayList<String>) : RecyclerView.Adapter<HorizontalAdapter.MyView>(){

    class MyView(view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView = view.findViewById(R.id.film_poster)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyView {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.horizontal_recyler, parent, false)
        return MyView(itemView)
    }

    override fun onBindViewHolder(holder: MyView, position: Int) {
        val listData = list[position]

        Picasso.get().load(listData).placeholder(R.mipmap.ic_launcher).into(holder.imageView)

    }

    override fun getItemCount(): Int {
        return list.size
    }
}