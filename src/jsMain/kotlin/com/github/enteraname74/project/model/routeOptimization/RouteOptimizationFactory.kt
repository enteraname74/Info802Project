package com.github.enteraname74.project.model.routeOptimization

import com.github.enteraname74.project.model.RouteInformation
import com.github.enteraname74.project.model.routeOptimizationImpl.RouteOptimizationByShortestRoute
import com.github.enteraname74.project.model.routeOptimizationImpl.RouteOptimizationByStations
import com.github.enteraname74.project.model.service.ChargingStationsService
import com.github.enteraname74.project.model.service.RouteService

/**
 * Factory for retrieving a RouteOptimization implementation.
 */
object RouteOptimizationFactory {

    /**
     * Define which RouteOptimization implementation should be used.
     */
    private val OPTIMIZATION_TYPE = OptimizationType.SHORTEST

    /**
     * Retrieve a RouteOptimization implementation based on the selected type defined by the system.
     */
    fun buildRouteOptimization(
        routeService: RouteService,
        chargingStationsService: ChargingStationsService,
        routeInformation: RouteInformation
    ): RouteOptimization {
        return when (OPTIMIZATION_TYPE) {
            OptimizationType.SHORTEST -> RouteOptimizationByShortestRoute(
                routeService = routeService,
                chargingStationsService = chargingStationsService,
                routeInformation = routeInformation
            )
            OptimizationType.STATIONS -> RouteOptimizationByStations(
                routeService = routeService,
                chargingStationsService = chargingStationsService,
                routeInformation = routeInformation
            )
        }
    }
}

/**
 * Define possible types of RouteOptimization.
 */
private enum class OptimizationType {
    SHORTEST,
    STATIONS
}