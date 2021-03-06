package com.nexton.nextonprojet.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.nexton.nextonprojet.R
import com.nexton.nextonprojet.data.model.Episodes
import com.smarteist.autoimageslider.SliderViewAdapter
import com.squareup.picasso.Picasso

class MySliderImageAdapter() : SliderViewAdapter<MySliderImageAdapter.VH>() {
   private var mSliderItems = ArrayList<String>()



    fun renewItems(sliderItems: ArrayList<String>) {
        mSliderItems = sliderItems
        notifyDataSetChanged()
    }

    fun addItem(sliderItem: String) {
        mSliderItems.add(sliderItem)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup): VH {
        val inflate: View = LayoutInflater.from(parent.context).inflate(R.layout.slider_row, null)
        return VH(inflate)
    }

    override fun onBindViewHolder(viewHolder: VH, position: Int) {

        Glide.with(viewHolder.imageView.context)
            .load(mSliderItems[position])
            .into(viewHolder.imageView)
    }

    override fun getCount(): Int {
        return mSliderItems.size
    }

    inner class VH(itemView: View) : ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.film_poster)
    }

}

