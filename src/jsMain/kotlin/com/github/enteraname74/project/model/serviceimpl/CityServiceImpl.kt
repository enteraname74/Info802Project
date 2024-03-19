package com.github.enteraname74.project.model.serviceimpl

import com.github.enteraname74.project.model.city.City
import com.github.enteraname74.project.model.service.CityService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Implementation of the CityService, using an API from the French government for the data source and Ktor for the
 * HTTP client.
 */
class CityServiceImpl: CityService {
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    override suspend fun getCitiesFromName(cityName: String): List<City> {
        return httpClient.get("https://geo.api.gouv.fr/communes?nom=$cityName&fields=centre&limit=5").body()
    }
}