package com.github.enteraname74.project.model.service

import com.github.enteraname74.project.model.Coordinates
import com.github.enteraname74.project.model.station.ChargingStation

/**
 * Interface for managing charging stations.
 */
interface ChargingStationsService {
    /**
     * Tries to retrieve close stations around a given radius and a center coordinates.
     */
    suspend fun retrieveCloseStationsFromRadiusAndCenter(
        radius: Int,
        center: Coordinates
    ): List<ChargingStation>
}