package com.github.enteraname74.project.model.service

/**
 * Interface for managing charging stations.
 */
interface ChargingStationsService {
    /**
     * Tries to retrieve close stations on the route.
     */
    fun retrieveCloseStations()
}