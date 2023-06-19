package com.kushai.api.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class Dish {
    var id: Int? = null
    var name: String? = null
    var price: Int = 0
    var weight: Int? = null
    var description: String? = null
    var image_url: String? = null
    var tegs: Array<String>? = null
    @Contextual
    var quantity: Int = 1
}