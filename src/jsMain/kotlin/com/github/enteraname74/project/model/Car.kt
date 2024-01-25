package com.github.enteraname74.project.model

import kotlinx.serialization.Serializable

/**
 * Represent a car.
 */
@Serializable
data class Car(
    val make: String,
    val model: String,
    val version: String,
    val autonomy: Int
)
