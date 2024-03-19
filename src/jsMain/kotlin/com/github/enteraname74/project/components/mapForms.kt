package com.github.enteraname74.project.components


import com.github.enteraname74.project.model.*
import com.github.enteraname74.project.model.city.City
import com.github.enteraname74.project.model.routeOptimization.OptimizedRoute
import com.github.enteraname74.project.model.routeOptimization.RouteOptimization
import com.github.enteraname74.project.model.routeOptimization.RouteOptimizationFactory
import com.github.enteraname74.project.model.routeOptimization.UnreachableDestination
import com.github.enteraname74.project.model.service.ChargingStationsService
import com.github.enteraname74.project.model.service.CityService
import com.github.enteraname74.project.model.service.RouteService
import com.github.enteraname74.project.model.service.TravelDurationService
import com.github.enteraname74.project.model.utils.MapsManager
import io.kvision.core.Container
import io.kvision.core.Position
import io.kvision.core.StringPair
import io.kvision.form.formPanel
import io.kvision.form.select.Select
import io.kvision.form.select.TomSelectCallbacks
import io.kvision.form.text.TomTypeahead
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.panel.vPanel
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.px
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Forms used to control what to show on the map.
 */
fun Container.mapForms(
    carList: List<StringPair>,
    cityService: CityService,
    chargingStationsService: ChargingStationsService,
    travelDurationService: TravelDurationService,
    routeService: RouteService,
    mapsManager: MapsManager,
    retrieveCarMethod: (StringPair) -> Car,
) {

    val travelDuration = ObservableValue(0f)
    var startCities: List<City> = emptyList()
    var endCities: List<City> = emptyList()

    vPanel {
        margin = 20.px
        position = Position.ABSOLUTE
        bottom = 0.px
        zIndex = 1
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
            this.disabled = true
            CoroutineScope(Dispatchers.Main).launch {

                val data = formPanel.getData()

                val routeInformation = RouteInformation(
                    startCityCoordinates = startCities.find { it.nom == data.startCity }?.centre?.toCoordinates()
                        ?: startCities.firstOrNull()?.centre?.toCoordinates() ?: Coordinates(),
                    endCityCoordinates = endCities.find { it.nom == data.endCity }?.centre?.toCoordinates()
                        ?: endCities.firstOrNull()?.centre?.toCoordinates() ?: Coordinates(),
                    carInformation = retrieveCarMethod(carList.find { it.first == data.carType }!!)
                )

                val routeOptimization: RouteOptimization = RouteOptimizationFactory.buildRouteOptimization(
                    routeService = routeService,
                    chargingStationsService = chargingStationsService,
                    routeInformation = routeInformation
                )

                when (val result = routeOptimization.buildOptimizedRoute()) {
                    is UnreachableDestination -> {
                        Toast.danger(
                            "The destination is unreachable with the autonomy of your car!",
                            ToastOptions(
                                stopOnFocus = false,
                                position = ToastPosition.BOTTOMRIGHT,
                                close = true,
                                duration = 5000
                            )

                        )
                        // We reset the travel duration when no route was found:
                        travelDuration.value = 0f
                    }

                    is OptimizedRoute -> {
                        val mapRouteInformation = result.route
                        mapsManager.showRouteFromInformation(mapRouteInformation)

                        travelDurationService.getTotalDuration(
                            totalDistance = routeOptimization.getTotalDistanceOfRoute(route = mapRouteInformation.route),
                            totalChargingStations = mapRouteInformation.chargingStations.size,
                            carChargingTime = .5f
                        ) { duration ->
                            travelDuration.value = duration
                        }
                    }
                }
            }.invokeOnCompletion {
                this.disabled = false
            }
        }

        div().bind(travelDuration) {
            content = "Travel duration estimated at ${travelDuration.value} hours."
        }
    }
}