package com.example.terramaster

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class OpenStreetMapGeocoder(private val context: Context) {

    private val executor = Executors.newSingleThreadExecutor()

    fun getCoordinatesFromAddress(address: String, callback: (Coordinates?) -> Unit) {
        executor.execute {
            try {
                val encodedAddress = address.replace(" ", "+")
                val url = URL("https://nominatim.openstreetmap.org/search?format=json&q=$encodedAddress")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONObject("{data:$response}").getJSONArray("data")

                if (jsonArray.length() > 0) {
                    val firstResult = jsonArray.getJSONObject(0)
                    val lat = firstResult.getDouble("lat")
                    val lon = firstResult.getDouble("lon")
                    callback(Coordinates(lat, lon))
                } else {
                    callback(null)
                }
            } catch (e: Exception) {
                Log.e("OSM Geocoder", "Error fetching coordinates: ${e.message}")
                callback(null)
            }
        }
    }

    fun getAddressFromCoordinates(lat: Double, lon: Double, callback: (String?) -> Unit) {
        executor.execute {
            try {
                val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)

                val address = jsonObject.optString("display_name", "Unknown Address")
                callback(address)
            } catch (e: Exception) {
                Log.e("OSM Geocoder", "Error fetching address: ${e.message}")
                callback(null)
            }
        }
    }
}

data class Coordinates(val latitude: Double, val longitude: Double)
