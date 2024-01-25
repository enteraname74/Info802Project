package com.github.enteraname74.project.model.service

import com.github.enteraname74.project.model.Coordinates
import com.github.enteraname74.project.model.Route

/**
 * Interface for managing routes.
 */
interface RouteService {

    /**
     * Retrieve a GeoJson with the route information between two cities.
     */
    suspend fun getRouteFromCoordinates(
        startCityCoordinates: Coordinates,
        endCityCoordinates: Coordinates
    ): Route
}