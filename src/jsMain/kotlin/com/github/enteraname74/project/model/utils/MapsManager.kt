package com.github.enteraname74.project.model.utils

import com.github.enteraname74.project.model.Coordinates
import com.github.enteraname74.project.model.MapRouteInformation
import io.kvision.maps.Maps
import io.kvision.maps.externals.geojson.MultiLineString
import io.kvision.maps.externals.leaflet.geo.LatLng
import io.kvision.maps.externals.leaflet.layer.FeatureGroup
import io.kvision.maps.externals.leaflet.layer.Layer
import io.kvision.maps.externals.leaflet.layer.marker.Marker
import io.kvision.maps.externals.leaflet.layer.vector.Polyline

/**
 * Manage maps related elements.
 */
class MapsManager(
    private val maps: Maps
) {
    private val tileLayer = Maps.L.tileLayer(
        urlTemplate = "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
        configure = {
            maxZoom = 19
        }
    )
    private val routeManager = MapsRoutesManager(maps, tileLayer)

    /**
     * Initialize the map with tiles and an initial zoom on France.
     */
    fun initializeMapView() {
        maps.configureLeafletMap {
            this.setView(
                center = LatLng(46.71109, 1.7191036),
                zoom = 6
            )
            tileLayer.addTo(this)
        }
    }

    /**
     * Show a route in the map.
     */
    fun showRouteFromInformation(mapRouteInformation: MapRouteInformation) {
        routeManager.showRouteFromInformation(mapRouteInformation)
    }
}

/**
 * Contains all elements related to the view of the route on the maps.
 */
private class MapsRoutesManager(
    private val maps: Maps,
    private val mapLayer: Layer<*>
) {
    private var startMarker: Marker = Marker(LatLng(0,0))
    private var endMarker: Marker = Marker(LatLng(0,0))
    private var routeLines: Polyline<MultiLineString> = Polyline(latlngs = arrayOf(arrayOf()))

    private var chargingStations: FeatureGroup = FeatureGroup()

    /**
     * Remove the previous route shown on the map.
     */
    private fun removePreviousRoute() {
        maps.leafletMap {
            Maps.L.featureGroup(layers = arrayOf(mapLayer)).clearLayers().addTo(this)
            chargingStations.clearLayers()
            startMarker.remove()
            endMarker.remove()
            routeLines.remove()
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
        val markers = stations.map { Maps.L.marker(latlng = it.toLatLng()) }
        maps.leafletMap {
            markers.forEach {
                it.addTo(chargingStations)
            }
            this.addLayer(chargingStations)
        }
    }

    /**
     * Add the route lines.
     */
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
     * Show a route in the map.
     */
    fun showRouteFromInformation(mapRouteInformation: MapRouteInformation) {
        maps.leafletMap {
            chargingStations.addTo(this)
        }
        console.log("${mapRouteInformation.chargingStations}")
        removePreviousRoute()
        addStartCityMarker(cityCoordinate = mapRouteInformation.startCoordinates)
        addEndCityMarker(cityCoordinate = mapRouteInformation.destinationCoordinates)
        addChargingStationsMarker(stations = mapRouteInformation.chargingStations)
        addRoute(route = mapRouteInformation.route)
    }
}
