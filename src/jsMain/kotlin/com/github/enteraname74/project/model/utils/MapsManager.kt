package com.github.enteraname74.project.model.utils

import com.github.enteraname74.project.model.Route
import io.kvision.maps.Maps
import io.kvision.maps.externals.geojson.MultiLineString
import io.kvision.maps.externals.leaflet.geo.LatLng
import io.kvision.maps.externals.leaflet.layer.marker.Marker
import io.kvision.maps.externals.leaflet.layer.vector.Polyline

/**
 * Manage maps related elements.
 */
class MapsManager(
    private val maps: Maps
) {
    private val routeManager = MapsRoutesManager(maps)

    /**
     * Initialize the map with tiles and an initial zoom on France.
     */
    fun initializeMapView() {
        maps.configureLeafletMap {
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
     * Remove the previous shown route.
     */
    private fun removePreviousRoute() {
        maps.leafletMap {

        }
    }

    /**
     * Show a route in the map.
     */
    fun showRouteFromGeoJson(route: Route) {
        routeManager.showRouteFromGeoJson(route)
    }
}

/**
 * Contains all elements related to the view of the route on the maps.
 */
private class MapsRoutesManager(
    private val maps: Maps
) {
    private var startMarker: Marker = Marker(LatLng(0,0))
    private var endMarker: Marker = Marker(LatLng(0,0))
    private var routeLines: Polyline<MultiLineString> = Polyline(latlngs = arrayOf(arrayOf()))

    /**
     * Remove the previous route shown on the map.
     */
    private fun removePreviousRoute() {
        maps.leafletMap {
            startMarker.remove()
            endMarker.remove()
            routeLines.remove()
        }
    }

    /**
     * Build new markers to be used for the view of the route on the map.
     */
    private fun buildNewMarkers(route: Route) {
        val allCoordinates = route.features[0].geometry.coordinates
        val startCoordinates = allCoordinates[0]
        val endCoordinates = allCoordinates.last()

        maps.leafletMap {
            startMarker = Maps.L.marker(latlng = LatLng(
                latitude = startCoordinates[1],
                longitude = startCoordinates[0]
            ))

            endMarker = Maps.L.marker(latlng = LatLng(
                latitude = endCoordinates[1],
                longitude = endCoordinates[0]
            ))

            routeLines = Maps.L.multiPolyline(
                latLngs = listOf(
                    allCoordinates.map {  coordinates ->
                        LatLng(
                            latitude = coordinates[1],
                            longitude = coordinates[0]
                        )
                    }
                )
            )
        }
    }

    /**
     * Show a route in the map.
     */
    fun showRouteFromGeoJson(route: Route) {
        removePreviousRoute()
        buildNewMarkers(route)
        maps.leafletMap {
            startMarker.addTo(this)
            endMarker.addTo(this)
            routeLines.addTo(this)
        }
    }
}
