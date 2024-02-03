package com.github.enteraname74.project.model.utils

import com.github.enteraname74.project.model.Coordinates
import com.github.enteraname74.project.model.MapRouteInformation
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
    fun showRouteFromGeoJson(mapRouteInformation: MapRouteInformation) {
        routeManager.showRouteFromGeoJson(mapRouteInformation)
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
    private var chargingStations: ArrayList<Marker> = arrayListOf()

    /**
     * Remove the previous route shown on the map.
     */
    private fun removePreviousRoute() {
        maps.leafletMap {
            startMarker.remove()
            endMarker.remove()
            routeLines.remove()
            chargingStations.forEach {
                it.remove()
            }
        }
    }

    private fun Coordinates.toLatLng(): LatLng = LatLng(
        latitude = latitude,
        longitude = longitude
    )

    /**
     * Add a start city marker on the map.
     */
    private fun addStartCityMarker(cityCoordinate: Coordinates) {
        startMarker = Maps.L.marker(latlng = cityCoordinate.toLatLng())
        maps.leafletMap {
            startMarker.addTo(this)
        }
    }

    /**
     * Add an end city marker on the map.
     */
    private fun addEndCityMarker(cityCoordinate: Coordinates) {
        endMarker = Maps.L.marker(latlng = cityCoordinate.toLatLng())
        maps.leafletMap {
            endMarker.addTo(this)
        }
    }

    /**
     * Add charging stations markers on the map.
     */
    private fun addChargingStationsMarker(stations: List<Coordinates>) {
        chargingStations.addAll(
            stations.map { Maps.L.marker(latlng = it.toLatLng())}
        )
        maps.leafletMap {
            chargingStations.forEach {
                it.addTo(this)
            }
        }
    }

    private fun addRoute(route: List<Coordinates>) {
        routeLines = Maps.L.multiPolyline(
            latLngs = listOf(
                route.map { it.toLatLng() }
            )
        )
        maps.leafletMap {
            routeLines.addTo(this)
        }
    }

    /**
     * Build new markers to be used for the view of the route on the map.
     */
    private fun buildNewMarkers(route: List<Coordinates>) {
        val startCoordinates = route[0]
        val endCoordinates = route.last()

        maps.leafletMap {
            startMarker = Maps.L.marker(latlng = startCoordinates.toLatLng())

            endMarker = Maps.L.marker(latlng = endCoordinates.toLatLng())

            routeLines = Maps.L.multiPolyline(
                latLngs = listOf(
                    route.map { it.toLatLng() }
                )
            )
        }
    }

    /**
     * Show a route in the map.
     */
    fun showRouteFromGeoJson(mapRouteInformation: MapRouteInformation) {
        console.log("${mapRouteInformation.chargingStations}")
        removePreviousRoute()
        addStartCityMarker(cityCoordinate = mapRouteInformation.startCoordinates)
        addEndCityMarker(cityCoordinate = mapRouteInformation.destinationCoordinates)
        addChargingStationsMarker(stations = mapRouteInformation.chargingStations)
        addRoute(route = mapRouteInformation.route)
    }
}
