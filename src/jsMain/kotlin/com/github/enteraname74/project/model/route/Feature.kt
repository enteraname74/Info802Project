package com.github.enteraname74.project.model.route

import kotlinx.serialization.Serializable

/**
 * Represent a feature of a route.
 */
@Serializable
data class Feature(
    val geometry: Geometry
)
