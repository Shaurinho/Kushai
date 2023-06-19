package com.kushai.fragments.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.kushai.api.models.Dish
import com.kushai.adapters.OrdersAdapter
import com.kushai.databinding.FragmentCartBinding
import com.kushai.fragments.SharedViewModel

class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private var ordersRV: RecyclerView? = null
    private var ordersAdapter: OrdersAdapter? = null
    private val dishesToOrder = arrayListOf<Dish>()
    private var payButton: MaterialButton? = null
    private var payAmount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val rootView = binding.root
        ordersRV = binding.ordersRV
        payButton = binding.payButton
        val linearLayoutManager = LinearLayoutManager(requireContext())
        ordersRV!!.layoutManager = linearLayoutManager
        ordersAdapter = OrdersAdapter(dishesToOrder, requireContext(), this)
        ordersRV!!.adapter = ordersAdapter

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val model = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        model.message.observe(viewLifecycleOwner, Observer {dish ->
            dishesToOrder.forEach {
                if (dish.id == it.id) {
                    Toast.makeText(requireContext(), "Вы уже добавили это блюдо", Toast.LENGTH_SHORT).show()
                    return@Observer
                }
            }
            dishesToOrder.add(dish)
            refresh()
        })
    }

    fun refresh() {
        ordersAdapter!!.notifyDataSetChanged()
        payAmount = 0
        dishesToOrder.forEach {
            payAmount += it.price * it.quantity
        }
        if (payAmount == 0) payButton!!.visibility = View.INVISIBLE
        else payButton!!.visibility = View.VISIBLE
        val amountFormatted = String.format("%,d", payAmount)
        payButton!!.text = "Оплатить $amountFormatted ₽"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}