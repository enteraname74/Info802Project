package com.github.enteraname74.project.model.utils

import com.github.enteraname74.project.model.Coordinates
import com.github.enteraname74.project.model.MapRouteInformation
import com.github.enteraname74.project.model.RouteInformation
import com.github.enteraname74.project.model.service.ChargingStationsService
import com.github.enteraname74.project.model.service.RouteService
import com.github.enteraname74.project.model.station.ChargingStation
import com.github.enteraname74.project.model.station.toCoordinates
import kotlin.math.*

/**
 * Optimize a given route with car autonomy
 */
class RouteOptimization(
    private val routeService: RouteService,
    private val chargingStationsService: ChargingStationsService,
    private val routeInformation: RouteInformation
) {

    private val neededChargingStations: ArrayList<Coordinates> = arrayListOf()

    /**
     * Build an optimized route taking into account vehicle capacities and charging stations.
     */
    suspend fun buildOptimizedRoute(): OptimizationResult {

        var canContinueBuildOptimizedRoute = true
        var currentRoute: List<Coordinates> = emptyList()

        while (canContinueBuildOptimizedRoute) {
            // We build a route from a starting point, an end point and a list of necessary charging stations to stop at.
            currentRoute = routeService.getRouteFromCoordinates(
                startCityCoordinates = routeInformation.startCityCoordinates,
                endCityCoordinates = routeInformation.endCityCoordinates,
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

    /**
     * Transform a double to a radian.
     */
    private fun toRadians(deg: Double): Double = deg / 180.0 * PI

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
     * Retrieve the total distance to do for a route.
     */
    private fun getTotalDistanceOfRoute(route: List<Coordinates>): Float {
        var totalDistance = 0f
        for (i in 0 until route.size - 1) {
            totalDistance += getDistanceBetweenTwoCoordinates(
                startPoint = route[i],
                endPoint = route[i + 1]
            )
        }
        return totalDistance
    }

    /**
     * Retrieve the distance between two coordinates, in kilometers.
     */
    private fun getDistanceBetweenTwoCoordinates(
        startPoint: Coordinates,
        endPoint: Coordinates
    ): Float {
        val earthRadius = 6371 // Radius of the earth

        val latDistance = toRadians((endPoint.latitude - startPoint.latitude).toDouble())
        val lonDistance = toRadians((endPoint.longitude - startPoint.longitude).toDouble())

        val a = sin(latDistance / 2) * sin(latDistance / 2) + cos(toRadians(startPoint.latitude.toDouble())) * cos(
            toRadians(endPoint.latitude.toDouble())
        ) * sin(lonDistance / 2) * sin(lonDistance / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        // Distance in kilometers
        val distance = earthRadius * c

        return distance.toFloat()
    }
}

/**
 * Information about the state of a vehicle at a coordinates index.
 */
private data class VehicleCoordinatesStateInformation(
    val lastChargingStationIndex: Int,
    val coordinatesIndex: Int,
    val vehicleAutonomyAtPoint: Float
)

/**
 * Define the result of an optimized request.
 */
sealed interface OptimizationResult

/**
 * Result when a destination is unreachable.
 */
data class UnreachableDestination(val route: MapRouteInformation) : OptimizationResult

/**
 * Result when a destination is reachable and has been optimized.
 */
data class OptimizedRoute(val route: MapRouteInformation) : OptimizationResult
