package com.github.enteraname74.project.model

/**
 * Represent a coordinate of a point.
 */
data class Coordinates(
    val latitude: Float = 0f,
    val longitude: Float = 0f
)

/**
 * Transform a Coordinates to a ListCoordinates.
 */
fun Coordinates.toListCoordinates(): ListCoordinates = ListCoordinates(
    coordinates = listOf(longitude, latitude)
)