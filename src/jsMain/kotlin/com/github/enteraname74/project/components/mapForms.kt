package com.github.enteraname74.project.components

import com.github.enteraname74.project.model.FormData
import io.kvision.core.Container
import io.kvision.core.StringPair
import io.kvision.form.formPanel
import io.kvision.form.select.Select
import io.kvision.form.text.Text

/**
 * Forms used to control what to show on the map.
 */
fun Container.mapForms(
    carList: List<StringPair>
) {
    formPanel<FormData> {
        add(
            FormData::startCity, Text(
                label = "Start city",
                floating = true
            )
        )
        add(
            FormData::endCity, Text(
                label = "End city",
                floating = true
            )
        )
        val select = Select(
            label = "Car type",
            floating = true
        ) {
            options = carList
        }
        add(
            FormData::carType, select
        )
    }
}