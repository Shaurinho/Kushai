package com.kushai.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kushai.api.models.Dish

class SharedViewModel : ViewModel() {
    val message = MutableLiveData<Dish>()

    fun sendMessage(dish: Dish) {
        message.value = dish
    }
}
