package com.github.enteraname74.project.model.routeOptimization

import com.github.enteraname74.project.model.MapRouteInformation

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