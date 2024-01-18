package com.github.enteraname74.project

import io.kvision.*
import io.kvision.html.div
import io.kvision.maps.maps
import io.kvision.panel.root

class App : Application() {
    override fun start() {
        root("kvapp") {
            maps(

            )
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
