package com.github.enteraname74.project.model

import kotlinx.serialization.Serializable

/**
 * Data for forms in the page.
 */
@Serializable
data class FormData(
    val startCity: String = "",
    val endCity: String = "",
    val carType: String = ""
)
