package com.github.enteraname74.project.model

import kotlinx.serialization.Serializable

/**
 * Coordinates for a city.
 */
@Serializable
data class ListCoordinates(
    val coordinates: List<Float>
)

/**
 * Transform a ListCoordinates to a Coordinates.
 */
fun ListCoordinates.toCoordinates() : Coordinates = Coordinates(
    latitude = coordinates.getOrElse(1) { 0f },
    longitude = coordinates.getOrElse(0) { 0f }
)