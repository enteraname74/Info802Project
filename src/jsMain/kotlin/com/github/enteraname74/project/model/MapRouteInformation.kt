package com.github.enteraname74.project.model

/**
 * Holds UI elements to show on the map for the route.
 */
data class MapRouteInformation(
    val startCoordinates: Coordinates,
    val destinationCoordinates: Coordinates,
    val route: List<Coordinates>,
    val chargingStations: List<Coordinates>
)
