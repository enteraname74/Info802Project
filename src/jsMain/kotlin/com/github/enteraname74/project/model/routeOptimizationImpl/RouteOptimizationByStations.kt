package com.github.enteraname74.project.model.routeOptimizationImpl

import com.github.enteraname74.project.model.Coordinates
import com.github.enteraname74.project.model.MapRouteInformation
import com.github.enteraname74.project.model.RouteInformation
import com.github.enteraname74.project.model.routeOptimization.OptimizationResult
import com.github.enteraname74.project.model.routeOptimization.OptimizedRoute
import com.github.enteraname74.project.model.routeOptimization.RouteOptimization
import com.github.enteraname74.project.model.routeOptimization.UnreachableDestination
import com.github.enteraname74.project.model.service.ChargingStationsService
import com.github.enteraname74.project.model.service.RouteService
import com.github.enteraname74.project.model.station.ChargingStation
import com.github.enteraname74.project.model.station.toCoordinates
import kotlin.math.ceil

/**
 * Optimize a given route with car autonomy and tries to build the route depending only on available charging stations.
 * The mechanism behind is the following:
 * - Found all the reachable charging stations by a car depending on its autonomy.
 * - Fetch the nearest station from the end coordinates and tries to reach it. If it's not reachable, we take the second one. This step is repeated as many times as necessary.
 * - We go to this station and start again.
 */
class RouteOptimizationByStations(
    routeService: RouteService,
    chargingStationsService: ChargingStationsService,
    routeInformation: RouteInformation
) : RouteOptimization(
    routeService = routeService,
    chargingStationsService = chargingStationsService,
    routeInformation = routeInformation
) {
    override suspend fun buildOptimizedRoute(): OptimizationResult {
        var canOptimizedRoute = true
        var currentCoordinates = routeInformation.startCityCoordinates
        var currentRoute = emptyList<Coordinates>()
        val neededChargingStations = arrayListOf<Coordinates>()

        while(canOptimizedRoute) {
            // First, we check if we can do the route from the current coordinates without the need of additional charging stations.
            currentRoute = routeService.getRouteFromCoordinates(
                startCoordinates = routeInformation.startCityCoordinates,
                endCoordinates = routeInformation.endCityCoordinates,
                chargingStations = neededChargingStations
            )
            if (isRoutePossibleWithoutStations(currentRoute)) return OptimizedRoute(route = MapRouteInformation(
                startCoordinates = routeInformation.startCityCoordinates,
                destinationCoordinates = routeInformation.endCityCoordinates,
                route = currentRoute,
                chargingStations = neededChargingStations
            ))

            console.log("Current coordinates: $currentCoordinates")
            val nearStations = getCloseChargingStationsSortedByDistanceFromEnd(
                center = currentCoordinates
            )
            // We can only build an optimized route if we found near stations.
            canOptimizedRoute = nearStations.isNotEmpty()

            val station = getFirstReachableStation(
                chargingStations = nearStations,
                carPosition = currentCoordinates
            )
            // If no reachable station is found, we cannot continue.
            canOptimizedRoute = station != null

            // We add the found station to the needed charging stations.
            val stationCoordinates = station!!.toCoordinates()
            neededChargingStations.add(stationCoordinates)
            currentCoordinates = stationCoordinates
        }

        return UnreachableDestination(
            route = MapRouteInformation(
                startCoordinates = routeInformation.startCityCoordinates,
                destinationCoordinates = routeInformation.endCityCoordinates,
                route = currentRoute,
                chargingStations = neededChargingStations
            )
        )
    }

    /**
     * Retrieve close charging stations from a coordinate. It uses the autonomy of the car as the radius.
     * The result is sorted from farthest charging station to the nearest one.
     * The sort is done with a as the crow flies distance to limit API calls to the route service (thus making the code faster).
     */
    private suspend fun getCloseChargingStationsSortedByDistanceFromEnd(
        center: Coordinates
    ): List<ChargingStation> {
        val nearStations = chargingStationsService.retrieveCloseStationsFromRadiusAndCenter(
            radius = ceil(routeInformation.carInformation.autonomy).toInt(),
            center = center
        )
        console.log("NEAR STATIONS TOT: ${nearStations.size}")
        console.log("NEAR STATIONS: $nearStations")

        val sortedList = nearStations.sortedByDescending { station ->
            getDistanceBetweenTwoCoordinates(
                startPoint = station.toCoordinates(),
                endPoint = routeInformation.endCityCoordinates
            )
        }

        return sortedList
    }

    /**
     * Tries to retrieve the first reachable station for a vehicle from a given list of charging station.
     * The criteria for the reachability is if the car can do the route from its position to a charging station.
     */
    private suspend fun getFirstReachableStation(
        chargingStations: List<ChargingStation>,
        carPosition: Coordinates
    ): ChargingStation? {
        // We make sure to not take the current station if it's in the list:
        val stations = ArrayList(chargingStations)
        stations.removeAll { chargingStation ->
            chargingStation.toCoordinates() == carPosition
        }

        stations.forEach { chargingStation ->
            val routeToChargingStation = routeService.getRouteFromCoordinates(
                startCoordinates = carPosition,
                endCoordinates = chargingStation.toCoordinates()
            )
            val isRouteReachable = getTotalDistanceOfRoute(route = routeToChargingStation) <= routeInformation.carInformation.autonomy

            if (isRouteReachable) return chargingStation
        }

        return null
    }
}