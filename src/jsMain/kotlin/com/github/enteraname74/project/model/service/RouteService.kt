package com.github.enteraname74.project.model.service

import com.github.enteraname74.project.model.Coordinates

/**
 * Service for managing routes.
 */
interface RouteService {

    /**
     * Retrieve a GeoJson with the route information between two cities.
     */
    suspend fun getRouteFromCoordinates(
        startCoordinates: Coordinates,
        endCoordinates: Coordinates,
        chargingStations: List<Coordinates> = emptyList()
    ): List<Coordinates>
}