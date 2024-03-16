package com.github.enteraname74.project.model.service

/**
 * Service for managing the duration of a travel.
 */
interface TravelDurationService {

    /**
     * Retrieve the total duration of a travel.
     */
    fun getTotalDuration(
        totalDistance: Float,
        totalChargingStations: Int,
        carChargingTime: Float,
        onResult: (Float) -> Unit
    )
}