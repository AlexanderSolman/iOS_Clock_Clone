package com.ios_styled_clock

import android.content.Context
import org.json.JSONArray
import java.io.IOException

class CitiesJsonSource (context: Context) {
    // New List to hold cities objects
    private val citiesList = mutableListOf<Cities>()

    init {
        try {
            // Reading the given "citiesList.json", holding the data with a bufferedReader
            val jsonString = context.assets.open("citiesList.json").bufferedReader().use { it.readText() }
            val jsonArr = JSONArray(jsonString)

            // Looping and extracting the data parameters from json to variables
            for (i in 0 until jsonArr.length()) {
                val citiesObj = jsonArr.getJSONObject(i)
                val city = citiesObj.getString("city")
                val country = citiesObj.getString("country")
                val timeZoneId = citiesObj.getString("timeZoneId")
                citiesList.add(Cities(city, country, timeZoneId))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getCities(): List<Cities> {
        return citiesList
    }
}