package com.kushai.fragments.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kushai.api.models.Category
import com.kushai.api.requests.GetCategories
import com.kushai.adapters.CategoryAdapter
import com.kushai.MainActivity
import com.kushai.R
import com.kushai.databinding.FragmentHomeBinding
import com.kushai.fragments.home.dishes.DishesFragment

class HomeFragment : Fragment() {
    private var mainActivity: MainActivity? = null
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var categories: ArrayList<Category>? = null
    private var mFragmentManager: FragmentManager? = null
    private var root: View? = null
    private var cookeryList: ListView? = null
    private var connectivityManager: ConnectivityManager? = null
    var childShowing: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFragmentManager = childFragmentManager
        mainActivity = requireActivity() as MainActivity
        connectivityManager = mainActivity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        root = binding.root
        cookeryList = binding.cookeryList
        cookeryList!!.setOnItemClickListener { parent, view, position, id ->
            if (childShowing) return@setOnItemClickListener
            childShowing = true
            val dishesFragment = DishesFragment.newInstance(categories!![position].name!!)
            mFragmentManager?.beginTransaction()?.add(R.id.dishes_fragment_container, dishesFragment, null)?.addToBackStack(null)?.commit()
        }

        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                Thread {
                    if (!getCategories()) {
                        mainActivity!!.runOnUiThread { Toast.makeText(mainActivity, "Проверьте подключение к интернету", Toast.LENGTH_SHORT).show() }
                        handler.postDelayed(this, 4000)
                    }
                }.start()
            }
        })

        mainActivity!!.backButton!!.setOnClickListener {
            if (mFragmentManager!!.backStackEntryCount != 0) mFragmentManager!!.popBackStack()
        }

        return root as View
    }

    private fun getCategories() : Boolean{
        var successful = false
        if (!isNetworkAvailable()) return false

        val getCategories = GetCategories()
        getCategories.send()
        if (getCategories.successful) {
            categories = getCategories.getCategoriesResponse!!.сategories
            val adapter = CategoryAdapter(mainActivity!!, categories!!)
            mainActivity!!.runOnUiThread { cookeryList!!.adapter = adapter }
            successful = true
        }
        return successful
    }

    private fun isNetworkAvailable(): Boolean {
        val capability = connectivityManager?.getNetworkCapabilities(connectivityManager?.activeNetwork)
        return capability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}