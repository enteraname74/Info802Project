package com.github.enteraname74.project.model

/**
 * Information needed to build an optimized route depending on a car capacities.
 */
data class RouteInformation(
    val startCityCoordinates: Coordinates,
    val endCityCoordinates: Coordinates,
    val carInformation: Car
)
