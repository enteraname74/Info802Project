package com.github.enteraname74.project.model.serviceimpl

import com.github.enteraname74.project.model.Coordinates
import com.github.enteraname74.project.model.route.Route
import com.github.enteraname74.project.model.environment.EnvironmentVariablesHandler
import com.github.enteraname74.project.model.environment.EnvironmentVariablesHandlerJsonImpl
import com.github.enteraname74.project.model.service.RouteService
import com.github.enteraname74.project.model.toListCoordinates
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**val route = routeService.getRouteFromCoordinates(
                    startCityCoordinates = startCities.find { it.nom == data.startCity }?.centre ?: Coordinates(coordinates = emptyList()),
                    endCityCoordinates = endCities.find { it.nom == data.endCity }?.centre ?: Coordinates(coordinates = emptyList())
                )
 * Implementation of the RouteService, using Open Route Service for the data source and Ktor for the HTTP client.
 */
class RouteServiceImpl: RouteService {
    private val environmentVariablesHandler: EnvironmentVariablesHandler = EnvironmentVariablesHandlerJsonImpl()
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    override suspend fun getRouteFromCoordinates(
        startCityCoordinates: Coordinates,
        endCityCoordinates: Coordinates,
        chargingStations: List<Coordinates>
    ): List<Coordinates> {
        val markers: ArrayList<List<Float>> = arrayListOf(startCityCoordinates.toListCoordinates().coordinates)
        markers.addAll(chargingStations.map { it.toListCoordinates().coordinates })
        markers.add(endCityCoordinates.toListCoordinates().coordinates)
        val body = "{\"coordinates\": $markers}"

        console.log(body)

        val request =  httpClient.post("https://api.openrouteservice.org/v2/directions/driving-car/geojson") {
            headers {
                append(HttpHeaders.ContentType, "application/json; charset=utf-8")
                append(HttpHeaders.Accept, "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8")
                append(HttpHeaders.Authorization, environmentVariablesHandler.openRouteKey)
            }
            setBody(body)
        }
        val route: Route = request.body()
        return route.features[0].geometry.coordinates.map { listCoordinates -> Coordinates(
            latitude = listCoordinates[1],
            longitude = listCoordinates[0]
        ) }
    }
}