package com.github.enteraname74.project.components


import com.github.enteraname74.project.model.*
import com.github.enteraname74.project.model.city.City
import com.github.enteraname74.project.model.service.ChargingStationsService
import com.github.enteraname74.project.model.service.CityService
import com.github.enteraname74.project.model.service.RouteService
import io.kvision.core.*
import io.kvision.form.formPanel
import io.kvision.form.select.Select
import io.kvision.form.select.TomSelectCallbacks
import io.kvision.form.text.TomTypeahead
import io.kvision.html.button
import io.kvision.panel.vPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.github.enteraname74.project.model.utils.MapsManager
import com.github.enteraname74.project.model.utils.OptimizedRoute
import com.github.enteraname74.project.model.utils.RouteOptimization
import com.github.enteraname74.project.model.utils.UnreachableDestination

/**
 * Forms used to control what to show on the map.
 */
fun Container.mapForms(
    carList: List<StringPair>,
    cityService: CityService,
    chargingStationsService: ChargingStationsService,
    routeService: RouteService,
    mapsManager: MapsManager,
    retrieveCarMethod: (StringPair) -> Car
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

                console.log("Retrieved data : $data")

                val routeInformation = RouteInformation(
                    startCityCoordinates = startCities.find { it.nom == data.startCity }?.centre?.toCoordinates() ?: Coordinates(),
                    endCityCoordinates = endCities.find { it.nom == data.endCity }?.centre?.toCoordinates() ?: Coordinates(),
                    carInformation = retrieveCarMethod(carList.find { it.first == data.carType }!!)
                )

                console.log("Retrieved data : $routeInformation")

                val routeOptimization = RouteOptimization(
                    routeService = routeService,
                    chargingStationsService = chargingStationsService,
                    routeInformation = routeInformation
                )

                when(val result = routeOptimization.buildOptimizedRoute()) {
                    is UnreachableDestination -> console.log("UNREACHABLE")
                    is OptimizedRoute -> {
                        mapsManager.showRouteFromGeoJson(result.route)
                    }
                }
            }
        }
    }
}