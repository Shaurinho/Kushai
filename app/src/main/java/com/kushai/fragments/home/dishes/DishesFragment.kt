package com.kushai.fragments.home.dishes

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.kushai.api.models.Dish
import com.kushai.api.requests.GetDishes
import com.kushai.adapters.DishesAdapter
import com.kushai.MainActivity
import com.kushai.R
import com.kushai.fragments.home.HomeFragment

private const val COOKERY_TITLE = "cookery_title"

class DishesFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            DishesFragment().apply {
                arguments = Bundle().apply {
                    putString(COOKERY_TITLE, param1)
                }
            }
    }
    private var cookeryTitle: String? = null
    private var currentFilterButton: MaterialButton? = null
    private var dishesRaw: Array<Dish>? =null
    private var dishesFiltered = ArrayList<Dish>()
    private var dishesAdapter: DishesAdapter? = null
    private var connectivityManager: ConnectivityManager? = null
    private var root: View? = null
    private var showingDish: Dish? = null
    private var mainActivity: MainActivity? = null
    private var filterSet = HashSet<String>()
    private var filters = arrayOf("Все меню", "Салаты", "С рисом", "С рыбой", "Роллы")
    private var inflater: LayoutInflater? = null
    private var linearLayout: LinearLayout? = null
    private var dishesRV: RecyclerView? = null
    private var handler: Handler? = null
    private var stopHandler: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = requireActivity() as MainActivity
        arguments?.let {
            cookeryTitle = it.getString(COOKERY_TITLE)
        }
        connectivityManager = mainActivity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_dishes, container, false)
        this.inflater = inflater

        linearLayout = root!!.findViewById(R.id.linear_layout)
        dishesRV = root!!.findViewById(R.id.dishes_grid)
        val layoutManager = GridLayoutManager(mainActivity, 3)
        dishesRV!!.layoutManager = layoutManager
        val itemDecorator = DividerItemDecoration(mainActivity, DividerItemDecoration.HORIZONTAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(mainActivity!!, R.drawable.grid_divider_horizontal)!!);
        dishesRV!!.addItemDecoration(itemDecorator);

        mainActivity!!.dishesTopBar!!.visibility = View.VISIBLE
        mainActivity!!.dishesTopBar!!.findViewById<TextView>(R.id.cookery_title).text = cookeryTitle

        handler = Handler(Looper.getMainLooper())
        handler!!.post(object : Runnable {
            override fun run() {
                Thread {
                    if (!getDishes()) {
                        mainActivity!!.runOnUiThread { Toast.makeText(mainActivity, "Проверьте подключение к интернету", Toast.LENGTH_SHORT).show() }
                        if (!stopHandler) handler!!.postDelayed(this, 4000)
                    }
                }.start()
            }
        })

        return root
    }

    //  ДЛЯ ПОЛУЧЕНИЯ ФИЛЬТРОВ ИЗ СПИСКА БЛЮД (НЕ ИСПОЛЬЗОВАЛОСЬ Т.К. В ФИГМЕ ЕСТЬ
    //  ФИЛЬТР "РОЛЛЫ", А В СПИСКЕ БЛЮД ИЗ АПИ БЛЮДА С ТАКИМ ТЕГОМ НЕТ)
    private fun getFilters(dishes: Array<Dish>) {
        dishes.forEach { dish ->
            dish.tegs!!.forEach { tag ->
                filterSet.add(tag)
            }
        }
    }

    private fun filter(button: MaterialButton) {
        currentFilterButton?.setTextColor(resources.getColor(R.color.black))
        currentFilterButton?.background?.setTint(resources.getColor(R.color.theme_gray))
        currentFilterButton = button
        button.setTextColor(resources.getColor(R.color.white))
        button.background.setTint(resources.getColor(R.color.theme_blue))

        dishesFiltered.clear()
        val tag = button.text
        for (dish: Dish in dishesRaw!!) {
            var add = false
            for (teg: String in dish.tegs!!) { if (teg == tag) add = true }
            if (add) dishesFiltered.add(dish)
        }
        dishesAdapter!!.notifyDataSetChanged()
    }

    private fun getDishes() : Boolean {
        var successful = false
        if (!isNetworkAvailable()) return false

        val getDishes = GetDishes()
        getDishes.send()

        if (getDishes.successful) {
            dishesRaw = getDishes.getDishesResponse!!.dishes

            for (i in filters.indices) {
                val buttonLayout = if (i == 0) inflater!!.inflate(R.layout.filter_button_active, linearLayout as ViewGroup, false)
                else inflater!!.inflate(R.layout.filter_button, linearLayout as ViewGroup, false)
                val button = buttonLayout.findViewById<MaterialButton>(R.id.filter_button)
                button.text = filters[i]
                button.setOnClickListener { filter(button) }
                mainActivity!!.runOnUiThread { linearLayout!!.addView(buttonLayout) }

                if (i == 0) {
                    currentFilterButton = button
                }

            }

            dishesFiltered = dishesRaw!!.toCollection(ArrayList())
            dishesAdapter = DishesAdapter(dishesFiltered!!, mainActivity!!)
            dishesAdapter!!.onItemClick = { dish ->
                (mainActivity as MainActivity).showDishFullscreen(dish)
            }
            mainActivity!!.runOnUiThread {
                dishesRV!!.adapter = dishesAdapter
            }
            successful = true
        }

        return successful
    }
    private fun isNetworkAvailable(): Boolean {
        val capability = connectivityManager?.getNetworkCapabilities(connectivityManager?.activeNetwork)
        return capability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        val visible = !hidden
        if (visible)
            mainActivity!!.dishesTopBar!!.visibility = View.VISIBLE
        else mainActivity!!.dishesTopBar!!.visibility = View.INVISIBLE
    }

    override fun onDestroy() {
        mainActivity!!.dishesTopBar!!.visibility = View.INVISIBLE
        (parentFragment as HomeFragment).childShowing = false
        super.onDestroy()
    }
}