package com.github.enteraname74.project

import com.github.enteraname74.project.components.mapForms
import com.github.enteraname74.project.model.Car
import com.github.enteraname74.project.model.service.CarService
import com.github.enteraname74.project.model.service.ChargingStationsService
import com.github.enteraname74.project.model.service.CityService
import com.github.enteraname74.project.model.service.RouteService
import com.github.enteraname74.project.model.serviceimpl.CarServiceImpl
import com.github.enteraname74.project.model.serviceimpl.ChargingStationsServiceImpl
import com.github.enteraname74.project.model.serviceimpl.CityServiceImpl
import com.github.enteraname74.project.model.serviceimpl.RouteServiceImpl
import com.github.enteraname74.project.model.utils.MapsManager
import io.kvision.*
import io.kvision.core.CssSize
import io.kvision.core.Position
import io.kvision.core.StringPair
import io.kvision.html.div
import io.kvision.maps.Maps
import io.kvision.maps.maps
import io.kvision.panel.root
import io.kvision.state.bind
import io.kvision.state.observableListOf
import io.kvision.utils.pc
import io.kvision.utils.perc
import io.kvision.utils.pt
import io.kvision.utils.px
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {
    private val carList = observableListOf<Car>()
    private val carService: CarService = CarServiceImpl()
    private val cityService: CityService = CityServiceImpl()
    private val routeService: RouteService = RouteServiceImpl()
    private val chargingStationService: ChargingStationsService = ChargingStationsServiceImpl()

    private lateinit var map: Maps
    private lateinit var mapsManager: MapsManager

    init {
        CoroutineScope(Dispatchers.Main).launch {
            carList.addAll(carService.getCars())
        }
    }

    override fun start() {
        root("kvapp").bind(carList) {
            div {
                width = maxWidth
                height = maxHeight

                map = maps {
                    zIndex = 0
                    position = Position.ABSOLUTE
                    mapsManager = MapsManager(this)
                    id ="map"
                    width = 100.perc
                    height = 100.perc
                    mapsManager.initializeMapView()
                }

                mapForms(
                    carList = carList.map {
                        StringPair(
                            first = carList.indexOf(it).toString(),
                            second = "${it.make} ${it.model} ${it.version} ${it.autonomy}km of autonomy"
                        )
                    },
                    cityService = cityService,
                    routeService = routeService,
                    mapsManager = mapsManager,
                    chargingStationsService = chargingStationService,
                    retrieveCarMethod = {
                        val index = it.first.toIntOrNull() ?: 0
                        carList[index]
                    }
                )
            }
        }
    }
}

fun main() {
    startApplication(
        ::App,
        module.hot,
        BootstrapModule,
        BootstrapCssModule,
        MapsModule,
        CoreModule,
        TomSelectModule
    )
}
