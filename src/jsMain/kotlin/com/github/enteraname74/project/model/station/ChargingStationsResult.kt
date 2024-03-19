package com.github.enteraname74.project.model.station

import kotlinx.serialization.Serializable

/**
 * Represent the result given from the request fetching charging stations.
 */
@Serializable
data class ChargingStationsResult(
    val total_count: Int,
    val results: List<ChargingStation>
)
