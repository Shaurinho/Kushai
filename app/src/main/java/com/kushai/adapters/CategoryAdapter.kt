package com.kushai.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

import com.kushai.R
import com.kushai.api.models.Category

class CategoryAdapter(context: Context, items: ArrayList<Category>) : BaseAdapter() {
    private val context: Context
    private val categories: ArrayList<Category>

    override fun getCount(): Int {
        return categories.size
    }

    override fun getItem(position: Int): Any {
        return categories[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var cV: View? = convertView
        if (cV == null) {
            cV = LayoutInflater.from(context).inflate(R.layout.row_category, parent, false)
        }
        val currentItem = getItem(position) as Category
        val titleTexView = cV?.findViewById(R.id.title) as TextView
        val imageView = cV.findViewById(R.id.image) as ImageView
        titleTexView.text = currentItem.name
        Glide.with(context).load(currentItem.image_url).into(imageView)

        return cV
    }

    init {
        this.context = context
        this.categories = items
    }
}