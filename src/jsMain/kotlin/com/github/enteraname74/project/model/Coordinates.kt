package com.github.enteraname74.project.model

import kotlinx.serialization.Serializable

/**
 * Coordinates for a city.
 */
@Serializable
data class Coordinates(
    val coordinates: List<Float>
)
