package com.github.enteraname74.project.model.station

import kotlinx.serialization.Serializable

/**
 * Represent a charging station
 */
@Serializable
data class ChargingStation(
    val geo_point_borne: StationCoordinates
)
