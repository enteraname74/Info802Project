package com.github.enteraname74.project.model.service

import com.github.enteraname74.project.model.Car

/**
 * Service for managing cars.
 */
interface CarService {
    /**
     * Retrieve a list of electric cars.
     */
    suspend fun getCars(): List<Car>
}