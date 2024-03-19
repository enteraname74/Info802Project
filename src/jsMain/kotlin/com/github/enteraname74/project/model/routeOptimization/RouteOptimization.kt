package com.github.enteraname74.project.model.routeOptimization

import com.github.enteraname74.project.model.Coordinates
import com.github.enteraname74.project.model.RouteInformation
import com.github.enteraname74.project.model.service.ChargingStationsService
import com.github.enteraname74.project.model.service.RouteService
import com.github.enteraname74.project.model.station.ChargingStation
import com.github.enteraname74.project.model.station.toCoordinates
import kotlin.math.*

/**
 * Abstract class for managing route optimization depending on car autonomy.
 */
abstract class RouteOptimization(
    protected val routeService: RouteService,
    protected val chargingStationsService: ChargingStationsService,
    protected val routeInformation: RouteInformation
) {
    abstract suspend fun buildOptimizedRoute(): OptimizationResult

    /**
     * Transform a double to a radian.
     */
    private fun toRadians(deg: Double): Double = deg / 180.0 * PI

    /**
     * Retrieve the total distance to do for a route.
     */
    fun getTotalDistanceOfRoute(route: List<Coordinates>): Float {
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
    protected fun getDistanceBetweenTwoCoordinates(
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

    /**
     * Check if a given route is accessible with the autonomy of the car.
     */
    protected fun isRoutePossibleWithoutStations(
        route: List<Coordinates>
    ): Boolean {
        return getTotalDistanceOfRoute(route = route) <= routeInformation.carInformation.autonomy
    }
}