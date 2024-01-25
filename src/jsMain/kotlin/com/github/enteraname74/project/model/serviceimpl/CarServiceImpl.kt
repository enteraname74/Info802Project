package com.github.enteraname74.project.model.serviceimpl

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpHeader
import com.github.enteraname74.CarsListQuery
import com.github.enteraname74.project.model.Car
import com.github.enteraname74.project.model.environment.EnvironmentVariablesHandler
import com.github.enteraname74.project.model.environment.EnvironmentVariablesHandlerJsonImpl
import com.github.enteraname74.project.model.service.CarService

/**
 * Implementation of the CarService, using Chargetrip as the data source and apollo for the GraphQl client.
 */
class CarServiceImpl: CarService {
    private val environmentVariablesHandler: EnvironmentVariablesHandler = EnvironmentVariablesHandlerJsonImpl()
    private val apolloClient = ApolloClient.Builder()
        .serverUrl("https://api.chargetrip.io/graphql'")
        .httpHeaders(
            httpHeaders = listOf(
                HttpHeader(
                    name = "x-app-id",
                    value = environmentVariablesHandler.xAppId
                ),
                HttpHeader(
                    name =  "x-client-id",
                    value = environmentVariablesHandler.xClientId
                )
            )
        )
        .build()

    override suspend fun getCars(): List<Car> {
        val response = apolloClient.query(CarsListQuery()).execute()
        println("RESPONSE FROM SERV: ${response.data}")
        return response.data?.vehicleList?.map {
            Car(
                model = it?.naming?.model ?: "",
                make = it?.naming?.make ?: "",
                version = it?.naming?.version ?: "",
                autonomy = it?.range?.chargetrip_range?.best ?: 0
            )
        } ?: emptyList()
    }
}