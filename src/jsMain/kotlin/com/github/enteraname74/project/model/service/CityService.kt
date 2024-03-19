package com.github.enteraname74.project.model.service

import com.github.enteraname74.project.model.city.City


/**
 * Service for managing cities.
 */
interface CityService {

    /**
     * Tries to retrieve a list of cities matching a given name.
     */
    suspend fun getCitiesFromName(cityName: String): List<City>
}