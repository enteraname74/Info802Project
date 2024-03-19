package com.github.enteraname74.project.model.station

import com.github.enteraname74.project.model.Coordinates
import kotlinx.serialization.Serializable

/**
 * Represent the coordinates of a ChargingStation.
 */
@Serializable
data class StationCoordinates(
    val lat: Float,
    val lon: Float
)

/**
 * Transform a StationCoordinates to a Coordinates.
 */
fun StationCoordinates.toCoordinates(): Coordinates = Coordinates(
    latitude = lon,
    longitude = lat
)
