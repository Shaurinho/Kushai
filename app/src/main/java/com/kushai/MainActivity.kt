package com.kushai

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.kushai.api.models.Dish
import com.kushai.databinding.ActivityMainBinding
import com.kushai.fragments.BottomNavigationViewHelper
import com.kushai.fragments.SharedViewModel
import com.kushai.fragments.account.AccountFragment
import com.kushai.fragments.cart.CartFragment
import com.kushai.fragments.home.HomeFragment
import com.kushai.fragments.search.SearchFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {
    companion object {
        const val HOME_FRAGMENT_TAG = "home"
        const val SEARCH_FRAGMENT_TAG = "search"
        const val CART_FRAGMENT_TAG = "cart"
        const val ACCOUNT_FRAGMENT_TAG = "account"
        const val ACTIVE_FRAGMENT_TAG = "active"
    }
    private lateinit var binding: ActivityMainBinding
    private var dishFullscreenView: ConstraintLayout? = null
    private var dishImage: ImageView? = null
    private var dishTitle: TextView? = null
    private var dishPrice: TextView? = null
    private var dishWeight: TextView? = null
    private var dishDescription: TextView? = null
    private var addToCartButton: MaterialButton? = null
    private var dateTextView: TextView? = null

    private var doubleBackToExitPressedOnce = false
    private var homeFragment: HomeFragment? = null
    private var searchFragment: SearchFragment? = null
    private var cartFragment: CartFragment? = null
    private var accountFragment: AccountFragment? = null
    private var activeFragment: Fragment? = null
    private var fragmentManager: FragmentManager? = null
    private var initialized: Boolean = false
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private var locationPermissionRequest:  ActivityResultLauncher<Array<String>>? = null
    lateinit var model: SharedViewModel
    var dishesTopBar: ConstraintLayout? = null
    var backButton: MaterialButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dishFullscreenLayoutBinding = binding.dishFullscreen
        dishFullscreenView = dishFullscreenLayoutBinding.dishFullscreenView
        addToCartButton = dishFullscreenLayoutBinding.addToCart
        dishImage = dishFullscreenLayoutBinding.image
        dishTitle = dishFullscreenLayoutBinding.dishTitle
        dishPrice = dishFullscreenLayoutBinding.price
        dishWeight = dishFullscreenLayoutBinding.weight
        dishDescription = dishFullscreenLayoutBinding.dishDescription
        val userPhoto = binding.userPhoto
        userPhoto.clipToOutline = true
        val closeButton = dishFullscreenLayoutBinding.close
        closeButton.setOnClickListener { hideDishFullscreen() }
        val dishesTopBarBinding = binding.dishesTopBar
        dishesTopBar = dishesTopBarBinding.root
        backButton = dishesTopBarBinding.backButton

        val locationTextView = binding.location
        dateTextView = binding.date

        fragmentManager = supportFragmentManager
        fragmentManager!!.addFragmentOnAttachListener { _: FragmentManager?, fragment: Fragment ->
            if (fragment.tag == null) return@addFragmentOnAttachListener
            if (savedInstanceState != null && fragment.tag == savedInstanceState.getString(ACTIVE_FRAGMENT_TAG)) activeFragment = fragment
            when (fragment.tag) {
                HOME_FRAGMENT_TAG -> homeFragment = fragment as HomeFragment
                SEARCH_FRAGMENT_TAG -> searchFragment = fragment as SearchFragment
                CART_FRAGMENT_TAG -> cartFragment = fragment as CartFragment
                ACCOUNT_FRAGMENT_TAG -> accountFragment = fragment as AccountFragment
            }
        }
        val bottomNavigationView: BottomNavigationView = binding.navView
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.navigation_home;
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            bottomNavigationView.menu.setGroupCheckable(0, true, true)
            when (item.itemId) {
                R.id.navigation_home -> {
                    fragmentManager!!.beginTransaction().hide(activeFragment!!).show(homeFragment!!).commit()
                    activeFragment = homeFragment
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_search -> {
                    fragmentManager!!.beginTransaction().hide(activeFragment!!).show(searchFragment!!).commit()
                    activeFragment = searchFragment
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_cart -> {
                    fragmentManager!!.beginTransaction().hide(activeFragment!!).show(cartFragment!!).commit()
                    activeFragment = cartFragment
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_account -> {
                    fragmentManager!!.beginTransaction().hide(activeFragment!!).show(accountFragment!!).commit()
                    activeFragment = accountFragment
                    return@setOnItemSelectedListener true
                }
            }
            true
        }

        model = ViewModelProvider(this)[SharedViewModel::class.java]

        requestLocationPermission()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations){
                    val gcd = Geocoder(this@MainActivity, Locale.getDefault())
                    Thread {
                        val addresses: List<Address>? = gcd.getFromLocation(location.latitude, location.longitude, 1)
                        runOnUiThread { if (addresses!!.isNotEmpty()) locationTextView.text = addresses!![0].locality }
                    }.start()
                }
            }
        }
        setDate()

        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            initializeFragments()
        } else {
            if (homeFragment != null && searchFragment != null && cartFragment != null && accountFragment != null) initialized = true
        }
    }

    private fun initializeFragments() {
        homeFragment = HomeFragment()
        searchFragment = SearchFragment()
        cartFragment = CartFragment()
        accountFragment = AccountFragment()
        activeFragment = homeFragment

        try {
            fragmentManager!!.beginTransaction().add(R.id.fragments_container, homeFragment!!, HOME_FRAGMENT_TAG).commit()
            fragmentManager!!.beginTransaction().add(R.id.fragments_container, searchFragment!!, SEARCH_FRAGMENT_TAG).hide(searchFragment!!).commit()
            fragmentManager!!.beginTransaction().add(R.id.fragments_container, cartFragment!!, CART_FRAGMENT_TAG).hide(cartFragment!!).commit()
            fragmentManager!!.beginTransaction().add(R.id.fragments_container, accountFragment!!, ACCOUNT_FRAGMENT_TAG).hide(accountFragment!!).commit()
            initialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showDishFullscreen(dish: Dish) {
        window.statusBarColor = ContextCompat.getColor(this, R.color.gray_status_bar)
        Glide.with(this).load(dish.image_url).into(dishImage!!)
        dishTitle!!.text = dish.name
        dishPrice!!.text = "${dish.price.toString()} ₽"
        dishWeight!!.text = "· ${dish.weight.toString()}г"
        dishDescription!!.text = dish.description
        dishFullscreenView!!.visibility = View.VISIBLE
        addToCartButton!!.setOnClickListener { model.sendMessage(dish) }
    }

    private fun hideDishFullscreen() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        dishFullscreenView!!.visibility = View.INVISIBLE
    }

    private fun setDate() {
        val sdf = SimpleDateFormat("d MMMM, YYYY", Locale.getDefault())
        val date: String = sdf.format(Date())
        dateTextView!!.text = date
    }

    private fun requestLocationPermission() {
        locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {}
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {}
                else -> {
                    Snackbar.make(dishesTopBar!!, "Для правильной работы приложения необходимо предоставить доступ к местоположению", Snackbar.LENGTH_LONG).show();
                }
            }
        }
        locationPermissionRequest!!.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient!!.requestLocationUpdates(createLocationRequest(),
            locationCallback,
            Looper.getMainLooper())
    }

    private fun createLocationRequest(): LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000).apply {
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(false)
        }.build()

    private fun stopLocationUpdates() {
        fusedLocationClient!!.removeLocationUpdates(locationCallback)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        if (!initialized) {
            initializeFragments()
        }
        startLocationUpdates()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ACTIVE_FRAGMENT_TAG, activeFragment!!.tag)
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Нажмите назад дважды для выхода", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 1000)
    }
}