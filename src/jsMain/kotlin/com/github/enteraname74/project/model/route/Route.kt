package com.github.enteraname74.project.model.route

import kotlinx.serialization.Serializable

/**
 * Represent a route and its information.
 */
@Serializable
data class Route(
    val features: List<Feature>
)
