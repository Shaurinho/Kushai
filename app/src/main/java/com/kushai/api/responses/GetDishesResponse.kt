package com.kushai.api.responses

import com.kushai.api.models.Dish
import kotlinx.serialization.Serializable

@Serializable
class GetDishesResponse {
    var dishes: Array<Dish>? = null
}