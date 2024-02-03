package com.github.enteraname74.project.model.city

import com.github.enteraname74.project.model.ListCoordinates
import kotlinx.serialization.Serializable

/**
 * Represent a city and its coordinates.
 */
@Serializable
data class City(
    val nom: String,
    val code: String,
    val centre: ListCoordinates
)
