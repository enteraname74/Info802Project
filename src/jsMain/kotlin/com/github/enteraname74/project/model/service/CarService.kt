package com.github.enteraname74.project.model.service

import com.github.enteraname74.project.model.Car

/**
 * Interface for managing cars.
 */
interface CarService {
    suspend fun getCars(): List<Car>
}