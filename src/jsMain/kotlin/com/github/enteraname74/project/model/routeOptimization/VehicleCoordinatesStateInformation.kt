package com.github.enteraname74.project.model.routeOptimization

/**
 * Information about the state of a vehicle at a coordinates index.
 */
data class VehicleCoordinatesStateInformation(
    val lastChargingStationIndex: Int,
    val coordinatesIndex: Int,
    val vehicleAutonomyAtPoint: Float
)
