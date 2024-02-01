package com.github.enteraname74.project.components

import com.github.enteraname74.project.model.City
import com.github.enteraname74.project.model.Coordinates
import com.github.enteraname74.project.model.FormData
import com.github.enteraname74.project.model.service.CityService
import com.github.enteraname74.project.model.service.RouteService
import io.kvision.core.*
import io.kvision.form.formPanel
import io.kvision.form.select.Select
import io.kvision.form.select.TomSelectCallbacks
import io.kvision.form.text.TomTypeahead
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.maps.Maps
import io.kvision.panel.vPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.github.enteraname74.project.model.utils.MapsManager

/**
 * Forms used to control what to show on the map.
 */
fun Container.mapForms(
    carList: List<StringPair>,
    cityService: CityService,
    routeService: RouteService,
    mapsManager: MapsManager
) {

    var startCities: List<City> = emptyList()
    var endCities: List<City> = emptyList()

    vPanel {
        spacing = 10
        val formPanel = formPanel<FormData> {
            add(
                FormData::startCity, TomTypeahead(
                    label = "Start city",
                    tsCallbacks = TomSelectCallbacks(
                        load = { query, callback ->
                            CoroutineScope(Dispatchers.Main).launch {
                                startCities = cityService.getCitiesFromName(query)
                                callback(startCities.map { city -> city.nom }.toTypedArray())
                            }
                        }
                    )
                )
            )
            add(
                FormData::endCity, TomTypeahead(
                    label = "Destination",
                    tsCallbacks = TomSelectCallbacks(
                        load = { query, callback ->
                            CoroutineScope(Dispatchers.Main).launch {
                                endCities = cityService.getCitiesFromName(query)
                                callback(endCities.map { city -> city.nom }.toTypedArray())
                            }
                        }
                    )
                )
            )
            add(
                FormData::carType, Select(
                    label = "Car type",
                    floating = true
                ) {
                    options = carList
                    id = "endCity"
                }
            )
        }

        button("Show route") {
        }.onClick {
            CoroutineScope(Dispatchers.Main).launch {
                val data = formPanel.getData()
                val route = routeService.getRouteFromCoordinates(
                    startCityCoordinates = startCities.find { it.nom == data.startCity }?.centre ?: Coordinates(coordinates = emptyList()),
                    endCityCoordinates = endCities.find { it.nom == data.endCity }?.centre ?: Coordinates(coordinates = emptyList())
                )
                mapsManager.showRouteFromGeoJson(route)
            }
        }
    }
}