package com.kushai.api.responses

import com.kushai.api.models.Category
import kotlinx.serialization.Serializable

@Serializable
class GetCategoriesResponse {
    var сategories: ArrayList<Category>? = null
}