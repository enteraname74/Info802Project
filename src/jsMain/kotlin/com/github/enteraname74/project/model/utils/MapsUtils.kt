package com.github.enteraname74.project.model.utils

import io.kvision.maps.Maps
import io.kvision.maps.externals.leaflet.geo.LatLng

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