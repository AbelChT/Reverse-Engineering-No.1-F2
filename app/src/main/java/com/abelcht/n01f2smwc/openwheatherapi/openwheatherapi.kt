package com.abelcht.n01f2smwc.openwheatherapi

import android.content.Context
import android.util.Log
import com.abelcht.n01f2smwc.config.openWeatherApiKey
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.concurrent.ExecutionException


fun getTemperaturePressureUV(
    latitude: Double,
    longitude: Double,
    context: Context
): Triple<Double, Double, Double>? {
    val TAG = "getTemperatureOpenWeatherAPITest"

    // API call config
    val parameters = "?lat=${latitude}&lon=${longitude}&appid=${openWeatherApiKey}"
    val temperatureAndBarometerRequestURL =
        "https://api.openweathermap.org/data/2.5/weather$parameters"
    val uVRequestURL = "https://api.openweathermap.org/data/2.5/uvi$parameters"

    // Request queue
    val requestQueue = Volley.newRequestQueue(context)

    // Temperature and barometer
    val futureTemperatureAndBarometer = RequestFuture.newFuture<JSONObject>()
    val requestTemperatureAndBarometer = JsonObjectRequest(
        Request.Method.GET,
        temperatureAndBarometerRequestURL,
        JSONObject(),
        futureTemperatureAndBarometer,
        futureTemperatureAndBarometer
    )
    requestQueue.add(requestTemperatureAndBarometer)

    // UV
    val futureUV = RequestFuture.newFuture<JSONObject>()
    val requestUV = JsonObjectRequest(
        Request.Method.GET, uVRequestURL,
        JSONObject(),
        futureUV,
        futureUV
    )
    requestQueue.add(requestUV)

    try {
        // Obtain responses
        val responseTemperatureAndBarometer = futureTemperatureAndBarometer.get() // this will block
        val responseUV = futureUV.get() // this will block

        val temperature =
            (responseTemperatureAndBarometer.get("main") as JSONObject).get("temp") as Double

        val pressure =
            (responseTemperatureAndBarometer.get("main") as JSONObject).get("pressure") as Double

        val uv = responseUV.get("value") as Double

        // Display responses
        Log.i(TAG, "Response is: $temperature $pressure $uv")
        return Triple(temperature, pressure, uv)
    } catch (e: InterruptedException) {
        // exception handling
        Log.i(TAG, "InterruptedException have arrived")
        return null
    } catch (e: ExecutionException) {
        // exception handling
        Log.i(TAG, "ExecutionException have arrived")
        return null
    }
}