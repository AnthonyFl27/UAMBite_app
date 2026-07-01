package com.uambite.app.data.api

import com.uambite.app.BuildConfig

object ImageUrlBuilder {

    fun local(id: String): String = "${BuildConfig.API_BASE_URL}localcomida/$id/imagen"

    fun producto(id: String): String = "${BuildConfig.API_BASE_URL}producto/$id/imagen"

    fun ingrediente(id: String): String = "${BuildConfig.API_BASE_URL}ingredienteextra/$id/imagen"
}
