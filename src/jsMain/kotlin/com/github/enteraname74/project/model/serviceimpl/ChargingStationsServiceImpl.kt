package com.github.enteraname74.project.model.serviceimpl

import com.github.enteraname74.project.model.Coordinates
import com.github.enteraname74.project.model.environment.EnvironmentVariablesHandler
import com.github.enteraname74.project.model.environment.EnvironmentVariablesHandlerJsonImpl
import com.github.enteraname74.project.model.service.ChargingStationsService
import com.github.enteraname74.project.model.station.ChargingStation
import com.github.enteraname74.project.model.station.ChargingStationsResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Implementation of the ChargingStationsService, using OpenData Réseaux-Énergies as the data source and Ktor for the
 * HTTP client.
 */
class ChargingStationsServiceImpl : ChargingStationsService {
    private val environmentVariablesHandler: EnvironmentVariablesHandler = EnvironmentVariablesHandlerJsonImpl()
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    override suspend fun retrieveCloseStationsFromRadiusAndCenter(
        radius: Int,
        center: Coordinates
    ): List<ChargingStation> {
        val point = "POINT(${center.latitude} ${center.longitude})"
        val verifiedRadius = if (radius <= 0) radius + 1 else radius
        val distance = "distance(geo_point_borne,geom'$point', ${verifiedRadius}km)"

        val request =
            httpClient.get("https://odre.opendatasoft.com/api/explore/v2.1/catalog/datasets/bornes-irve/records?limit=100&where=$distance") {
                headers {
                    append(HttpHeaders.Accept, "application/json; charset=utf-8")
                    append(HttpHeaders.Authorization, environmentVariablesHandler.odreApiKey)
                }
            }
        val result: ChargingStationsResult = request.body()
        return result.results
    }
}