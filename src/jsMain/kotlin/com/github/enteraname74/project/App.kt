package com.github.enteraname74.project

import com.github.enteraname74.project.components.mapForms
import com.github.enteraname74.project.model.Car
import com.github.enteraname74.project.model.service.CarService
import com.github.enteraname74.project.model.serviceimpl.CarServiceImpl
import com.github.enteraname74.project.model.utils.initializeMapView
import io.kvision.*
import io.kvision.core.FlexWrap
import io.kvision.core.JustifyContent
import io.kvision.core.StringPair
import io.kvision.maps.maps
import io.kvision.panel.hPanel
import io.kvision.panel.root
import io.kvision.state.bind
import io.kvision.state.observableListOf
import io.kvision.utils.px
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {
    private val carList = observableListOf<Car>()
    private val carService: CarService = CarServiceImpl()

    init {
        CoroutineScope(Dispatchers.Main).launch {
            carList.addAll(carService.getCars())
        }
    }

    override fun start() {
        root("kvapp").bind(carList) {
            margin = 32.px
            hPanel(
                wrap = FlexWrap.WRAP,
                spacing = 120,
                justify = JustifyContent.CENTER
            ) {
                width = maxWidth
                mapForms(
                    carList = carList.map {
                        StringPair(
                            first = carList.indexOf(it).toString(),
                            second = "${it.make} ${it.model} ${it.version}"
                        )
                    }
                )
                maps {
                    width = 800.px
                    height = 800.px
                    initializeMapView()
                }
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
        CoreModule
    )
}
