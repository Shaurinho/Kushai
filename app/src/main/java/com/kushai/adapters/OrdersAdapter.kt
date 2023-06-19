package com.kushai.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.kushai.api.models.Dish
import com.kushai.R
import com.kushai.fragments.cart.CartFragment

class OrdersAdapter(
    val dishes: ArrayList<Dish>,
    val context: Context,
    val fragment: CartFragment
    )
    : RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dishImage: ImageView
        val dishTitle: TextView
        val dishPrice: TextView
        val dishWeight: TextView
        val minus: MaterialButton
        val plus: MaterialButton
        val quantity: TextView

        init {
            dishImage = view.findViewById(R.id.image)
            dishTitle = view.findViewById(R.id.dish_title)
            dishPrice = view.findViewById(R.id.dish_price)
            dishWeight = view.findViewById(R.id.dish_weight)
            quantity = view.findViewById(R.id.quantity)
            minus = view.findViewById(R.id.minus)
            plus = view.findViewById(R.id.plus)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_order, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val dish = dishes[position]

        viewHolder.minus.setOnClickListener {
            if (dish.quantity > 1) dish.quantity--
            else {
                dishes.removeAt(position)
                fragment.refresh()
                return@setOnClickListener
            }
            var showingQuantity = viewHolder.quantity.text.toString().toInt()
            showingQuantity--
            viewHolder.quantity.text = showingQuantity.toString()
            fragment.refresh()
        }
        viewHolder.plus.setOnClickListener {
            dish.quantity++
            var showingQuantity = viewHolder.quantity.text.toString().toInt()
            showingQuantity++
            viewHolder.quantity.text = showingQuantity.toString()
            fragment.refresh()
        }

        Glide.with(context).load(dish.image_url).into(viewHolder.dishImage)
        viewHolder.dishTitle.text = dish.name
        viewHolder.dishPrice.text = "${dish.price.toString()} ₽"
        viewHolder.dishWeight.text = "· ${dish.weight.toString()} г"
        viewHolder.quantity.text = dish.quantity.toString()
    }

    override fun getItemCount() = dishes.size
}