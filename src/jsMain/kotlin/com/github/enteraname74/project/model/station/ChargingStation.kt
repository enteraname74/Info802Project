package com.github.enteraname74.project.model.station

import com.github.enteraname74.project.model.Coordinates
import kotlinx.serialization.Serializable

/**
 * Represent a charging station
 */
@Serializable
data class ChargingStation(
    val geo_point_borne: StationCoordinates
)

/**
 * Transform a ChargingStation to a Coordinates.
 */
fun ChargingStation.toCoordinates(): Coordinates = geo_point_borne.toCoordinates()
