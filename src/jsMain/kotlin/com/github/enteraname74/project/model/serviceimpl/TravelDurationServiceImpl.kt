package com.github.enteraname74.project.model.serviceimpl

import com.github.enteraname74.project.model.service.TravelDurationService
import org.w3c.dom.XMLDocument
import org.w3c.dom.parsing.DOMParser
import org.w3c.xhr.XMLHttpRequest

/**
 * Implementation of the TravelDurationService using a SOAP service.
 */
class TravelDurationServiceImpl: TravelDurationService {
    override fun getTotalDuration(
        totalDistance: Float,
        totalChargingStations: Int,
        carChargingTime: Float,
        onResult: (Float) -> Unit
    ) {
        try {
            val requestBody = buildSoapRequestBody(
                totalDistance = totalDistance,
                totalChargingStations = totalChargingStations,
                carChargingTime = carChargingTime
            )

            val request = buildRequest(body = requestBody)

            request.onreadystatechange = {
                val responseText = request.responseText
                println("RESPONSE: $responseText")
                val responseData: XMLDocument = DOMParser().parseFromString(responseText, "text/xml") as XMLDocument
                val duration = responseData.querySelector("return")?.textContent?.toFloat() ?: 0f
                onResult(duration)
            }
        } catch (e: Exception) {
            println("Error while fetching total travel duration: $e")
            onResult(0f)
        }
    }

    /**
     * Build a soap request body to send to the SOAP service.
     */
    private fun buildSoapRequestBody(totalDistance: Float, totalChargingStations: Int, carChargingTime: Float): String {
        return """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:dem="http://demo2.example.com/">
               <soapenv:Header/>
               <soapenv:Body>
                  <dem:travelDuration>
                     <arg0>$totalDistance</arg0>
                     <arg1>$totalChargingStations</arg1>
                     <arg2>$carChargingTime</arg2>
                  </dem:travelDuration>
               </soapenv:Body>
            </soapenv:Envelope>
        """.trimIndent()
    }

    /**
     * Create a XMLHttpRequest and add the necessary information to it.
     */
    private fun buildRequest(body: String): XMLHttpRequest {
        return XMLHttpRequest().apply {
            open("POST", "http://localhost:8080/demo2-1.0-SNAPSHOT/DurationService", async = true)
            setRequestHeader("Content-Type", "text/xml")
            send(body)
        }
    }
}