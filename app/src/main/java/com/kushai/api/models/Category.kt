package com.kushai.api.models

import kotlinx.serialization.Serializable

@Serializable
class Category {
    var id = -1
    var name: String? = null
    var image_url: String? = null
}