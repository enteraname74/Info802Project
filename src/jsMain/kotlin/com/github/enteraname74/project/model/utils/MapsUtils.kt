package com.github.enteraname74.project.model.utils

import com.github.enteraname74.project.model.Route
import io.kvision.maps.Maps
import io.kvision.maps.externals.leaflet.geo.LatLng
import io.kvision.maps.externals.leaflet.map.LeafletMap
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder

/**
 * Initialize the map with tiles and an initial zoom on France.
 */
fun Maps.initializeMapView() {
    this.configureLeafletMap {
        this.setView(
            center = LatLng(46.71109, 1.7191036),
            zoom = 6
        )
        Maps.L.tileLayer(
            urlTemplate = "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
            configure = {
                maxZoom = 19
            }
        ).addTo(this)
    }
}

/**
 * Show a route in the map.
 */
fun Maps.showRouteFromGeoJson(route: Route) {
    val allCoordinates = route.features[0].geometry.coordinates
    val startCoordinates = allCoordinates[0]
    val endCoordinates = allCoordinates.last()

    this.leafletMap {

        Maps.L.marker(latlng = LatLng(
            latitude = startCoordinates[1],
            longitude = startCoordinates[0]
        )).addTo(this)

        Maps.L.marker(latlng = LatLng(
            latitude = endCoordinates[1],
            longitude = endCoordinates[0]
        )).addTo(this)

        Maps.L.polyline(
            allCoordinates.map {  coordinates ->
                LatLng(
                    latitude = coordinates[1],
                    longitude = coordinates[0]
                )
            }
        ).addTo(this)
    }
}