package com.abelcht.n01f2smwc.openwheatherapi

import android.content.Context
import android.util.Log
import com.abelcht.n01f2smwc.config.openWeatherApiKey
import com.abelcht.n01f2smwc.network.HTTPJSONRequester
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

private val logTag = "getTemperatureOpenWeatherAPITest"

fun getTemperaturePressure(
    latitude: Double,
    longitude: Double,
    context: Context,
    callback: (Pair<Double, Double>?) -> Unit
) {
    // API call config
    val temperatureAndBarometerRequestURL =
        "https://api.openweathermap.org/data/2.5/weather?lat=${latitude}&lon=${longitude}&appid=${openWeatherApiKey}"

    // Request object
    val jsonObjectRequest =
        JsonObjectRequest(Request.Method.GET, temperatureAndBarometerRequestURL, null,
            Response.Listener { responseTemperatureAndBarometer ->
                val temperature = when (val temperatureAny =
                    (responseTemperatureAndBarometer.get("main") as JSONObject).get("temp")) {
                    is Int -> temperatureAny.toDouble()
                    is Double -> temperatureAny
                    else -> 0.0
                }

                val pressure = when (val pressureAny =
                    (responseTemperatureAndBarometer.get("main") as JSONObject).get("pressure")) {
                    is Int -> pressureAny.toDouble()
                    is Double -> pressureAny
                    else -> 0.0
                }
                Log.i(logTag, "TemperaturePressure response is: $temperature $pressure")

                callback(Pair(temperature, pressure))
            },
            Response.ErrorListener {
                Log.i(logTag, "Null have arrived TemperaturePressure")
                callback(null)
            }
        )

    // Add request
    HTTPJSONRequester.getInstance(context).addToRequestQueue(jsonObjectRequest)
}

fun getUV(
    latitude: Double,
    longitude: Double,
    context: Context,
    callback: (Double?) -> Unit
) {
    // API call config
    val uVRequestURL =
        "https://api.openweathermap.org/data/2.5/uvi?lat=${latitude}&lon=${longitude}&appid=${openWeatherApiKey}"

    // Request object
    val jsonObjectRequest =
        JsonObjectRequest(Request.Method.GET, uVRequestURL, null,
            Response.Listener { responseUV ->
                val uv = when (val uvAny = responseUV.get("value")) {
                    is Int -> uvAny.toDouble()
                    is Double -> uvAny
                    else -> 0.0
                }
                Log.i(logTag, "UV response is: $uv")

                callback(uv)
            },
            Response.ErrorListener {
                Log.i(logTag, "Null have arrived UV")
                callback(null)
            }
        )

    // Add request
    HTTPJSONRequester.getInstance(context).addToRequestQueue(jsonObjectRequest)
}