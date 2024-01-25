package com.github.enteraname74.project.model

import kotlinx.serialization.Serializable

/**
 * Contains all the coordinates for the route (for building markers, polylines...)
 */
@Serializable
data class Geometry(
    val coordinates: List<List<Float>>
)
