package com.github.enteraname74.project.model.routeOptimizationImpl

import com.github.enteraname74.project.model.Coordinates
import com.github.enteraname74.project.model.MapRouteInformation
import com.github.enteraname74.project.model.RouteInformation
import com.github.enteraname74.project.model.routeOptimization.*
import com.github.enteraname74.project.model.service.ChargingStationsService
import com.github.enteraname74.project.model.service.RouteService
import com.github.enteraname74.project.model.station.ChargingStation
import com.github.enteraname74.project.model.station.toCoordinates
import kotlin.math.ceil

/**
 * Optimize a given route with car autonomy and tries to found the shortest route. The mechanism behind is the following:
 * - Build an initial route between the start and the end.
 * - Check if the autonomy of the car makes it possible to do the trip without stopping at a charging station.
 * - If not, we go as far as we can go and backtrack until we found a reachable charging station.
 * - We build a new initial route with the found charging station and try to do the trip with this new charging station.
 * - We do these last steps as many times as necessary.
 */
class RouteOptimizationByShortestRoute(
    routeService: RouteService,
    chargingStationsService: ChargingStationsService,
    routeInformation: RouteInformation
) : RouteOptimization(
    routeService = routeService,
    chargingStationsService = chargingStationsService,
    routeInformation = routeInformation
) {

    private val neededChargingStations: ArrayList<Coordinates> = arrayListOf()

    /**
     * Build an optimized route taking into account vehicle capacities and charging stations.
     */
    override suspend fun buildOptimizedRoute(): OptimizationResult {

        var canContinueBuildOptimizedRoute = true
        var currentRoute: List<Coordinates> = emptyList()

        while (canContinueBuildOptimizedRoute) {
            // We build a route from a starting point, an end point and a list of necessary charging stations to stop at.
            currentRoute = routeService.getRouteFromCoordinates(
                startCoordinates = routeInformation.startCityCoordinates,
                endCoordinates = routeInformation.endCityCoordinates,
                chargingStations = neededChargingStations
            )
            console.log("Found current route with ${currentRoute.size} polylines.")
            val totalDistance = getTotalDistanceOfRoute(route = currentRoute)
            console.log("Total distance to do : $totalDistance")
            console.log("Car autonomy: ${routeInformation.carInformation.autonomy}")

            // If the car has the autonomy to do the route, we do not need to find charging stations:
            if (routeInformation.carInformation.autonomy >= totalDistance.toInt()) return OptimizedRoute(
                route = MapRouteInformation(
                    startCoordinates = routeInformation.startCityCoordinates,
                    destinationCoordinates = routeInformation.endCityCoordinates,
                    route = currentRoute,
                    chargingStations = neededChargingStations
                )
            )

            // Next we check if we need to add a new charging station to pursue the route.
            val lastReachableCoordinateState = getMaxIndexCoordinatesReachable(
                routeCoordinates = currentRoute
            )

            console.log("Last reachable coordinates with given route and infos : $lastReachableCoordinateState")

            // If we are at the end of the route, no need to continue.
            if (lastReachableCoordinateState.coordinatesIndex == currentRoute.lastIndex) return OptimizedRoute(
                route = MapRouteInformation(
                    startCoordinates = routeInformation.startCityCoordinates,
                    destinationCoordinates = routeInformation.endCityCoordinates,
                    route = currentRoute,
                    chargingStations = neededChargingStations
                )
            )

            console.log("We need to found a near station to charge the car.")

            // Else, we need to find a near charging station.
            val nearStation = backtrackUntilChargingStation(
                maxBacktrackingIndex = lastReachableCoordinateState.lastChargingStationIndex,
                currentCoordinateIndex = lastReachableCoordinateState.coordinatesIndex,
                routesCoordinates = currentRoute,
                vehicleCurrentAutonomy = lastReachableCoordinateState.vehicleAutonomyAtPoint
            )

            console.log("Found near station: $nearStation")

            // If no stations are found, we cannot continue the optimization.
            canContinueBuildOptimizedRoute =
                nearStation != null && !neededChargingStations.any { it == nearStation.geo_point_borne.toCoordinates() }

            // Else, we add the charging route to the necessary charging stations to stop at.
            nearStation?.let {
                neededChargingStations.add(it.geo_point_borne.toCoordinates())
            }
            console.log("$neededChargingStations")
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
     * Tries to retrieve the nearest charging station for a vehicle to charge itself.
     * @param coordinates the current position of the vehicle.
     * @param vehicleCurrentAutonomy the current autonomy of the vehicle when at the given coordinates.
     */
    private suspend fun getNearChargingStation(
        coordinates: Coordinates,
        vehicleCurrentAutonomy: Float
    ): ChargingStation? {
        val nearStations = chargingStationsService.retrieveCloseStationsFromRadiusAndCenter(
            radius = ceil(vehicleCurrentAutonomy).toInt(),
            center = coordinates
        )
        if (nearStations.isEmpty()) return null
        val sortedStations = nearStations.sortedBy { chargingStation ->
            getDistanceBetweenTwoCoordinates(
                startPoint = coordinates,
                endPoint = chargingStation.geo_point_borne.toCoordinates()
            )
        }

        return sortedStations.first()
    }

    /**
     * Retrieve information (index of max reachable coordinates and autonomy of vehicle at this point) about the last
     * reachable coordinates.
     */
    private fun getMaxIndexCoordinatesReachable(
        routeCoordinates: List<Coordinates>
    ): VehicleCoordinatesStateInformation {
        var currentAutonomy = routeInformation.carInformation.autonomy
        var lastChargingStationIndex = 0

        for (i in 0 until routeCoordinates.size - 1) {
            // First, we need to check if the current coordinates are corresponding to a charging station.
            if (isChargingStation(routeCoordinates[i])) {
                console.log("MAX INDEX COORDINATES REACHABLE - current coordinates is charging station")
                // If so, the current autonomy of a car can now be at max.
                currentAutonomy = routeInformation.carInformation.autonomy
                lastChargingStationIndex = i
                continue
            }

            val currentDistance = getDistanceBetweenTwoCoordinates(
                startPoint = routeCoordinates[i],
                endPoint = routeCoordinates[i + 1]
            )

            if (currentAutonomy - currentDistance <= 0) {
                return VehicleCoordinatesStateInformation(
                    lastChargingStationIndex = lastChargingStationIndex,
                    coordinatesIndex = i,
                    vehicleAutonomyAtPoint = currentAutonomy
                )
            }
            currentAutonomy -= currentDistance
        }

        return VehicleCoordinatesStateInformation(
            lastChargingStationIndex = lastChargingStationIndex,
            coordinatesIndex = routeCoordinates.lastIndex,
            vehicleAutonomyAtPoint = currentAutonomy
        )
    }

    /**
     * Backtrack until a charging station is found or return null.
     */
    private suspend fun backtrackUntilChargingStation(
        maxBacktrackingIndex: Int,
        currentCoordinateIndex: Int,
        routesCoordinates: List<Coordinates>,
        vehicleCurrentAutonomy: Float
    ): ChargingStation? {
        var vehicleAutonomy = vehicleCurrentAutonomy
        var coordinateIndex = currentCoordinateIndex
        var foundStation: ChargingStation? = null
        while (coordinateIndex >= maxBacktrackingIndex && foundStation == null) {
            console.log("BACKTRACK - need to find station with index of : $coordinateIndex, autonomy of : $vehicleAutonomy and max backtrack of $maxBacktrackingIndex")
            foundStation = getNearChargingStation(
                coordinates = routesCoordinates[coordinateIndex],
                vehicleCurrentAutonomy = vehicleAutonomy
            )

            for (i in 0 until 10) {
                coordinateIndex -= 1
                if (coordinateIndex < maxBacktrackingIndex) break
                vehicleAutonomy += getDistanceBetweenTwoCoordinates(
                    startPoint = routesCoordinates[coordinateIndex],
                    endPoint = routesCoordinates[coordinateIndex + 1]
                )
            }
        }

        return foundStation
    }

    /**
     * Check if a coordinates is corresponding to a charging station.
     * It will check if the given point is near a station (less than half a kilometer away).
     */
    private fun isChargingStation(coordinates: Coordinates): Boolean {
        return neededChargingStations.any {
            getDistanceBetweenTwoCoordinates(
                startPoint = it,
                endPoint = coordinates
            ) <= 0.5f
        }
    }
}
