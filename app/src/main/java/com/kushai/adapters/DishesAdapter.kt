package com.kushai.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kushai.api.models.Dish
import com.kushai.R

class DishesAdapter(
    val dishes: ArrayList<Dish>,
    val context: Context,
    ) : RecyclerView.Adapter<DishesAdapter.ViewHolder>() {
    var onItemClick: ((Dish) -> Unit)? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dishImage: ImageView
        val dishTitle: TextView

        init {
            dishImage = view.findViewById(R.id.dish_image)
            dishTitle = view.findViewById(R.id.dish_title)
            itemView.setOnClickListener {
                onItemClick?.invoke(dishes[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.dishes_grid_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val dish = dishes[position]

        Glide.with(context).load(dish.image_url).into(viewHolder.dishImage)
        viewHolder.dishTitle.text = dish.name
    }

    override fun getItemCount() = dishes.size
}