package com.ios_styled_clock

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Class to manage selected cities on main frame
class CitySelectionAdapter(private val cities: List<Cities>, private var timeToUse: Date?) : RecyclerView.Adapter<CitySelectionAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cityNameTextView: TextView = itemView.findViewById(R.id.cityNameTextView)
        val cityClockView: TextView = itemView.findViewById(R.id.cityClockView)
        val cityTimeZone: TextView = itemView.findViewById(R.id.cityTimeZone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.selected_city_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Handling the displaying of object
        val city = cities[position]
        holder.cityNameTextView.text = city.getCity()
        // Handle time logic upon selection initially, getting time zone
        val timeZone = TimeZone.getTimeZone(city.getTimeZone())

        // Calculating the offset for chosen city
        val offset = timeToUse?.let { timeZone.getOffset(it.time) }
        val offsetHours = offset?.div(3600000)
        val offsetSign = if (offsetHours!! >= 0) "+" else "-"

        // Building the string to use for time zone display
        val sb = StringBuilder()
        holder.cityTimeZone.text = sb.append("GMT").append(offsetSign).append("$offsetHours").toString()

        // Altering time to use with time zone offset, original time has offset so subtracting original offset
        val zonedTime = timeToUse?.let { Date(it.time + timeZone.getOffset(it.time) - (1 * 3600 * 1000)) } ?: Date()
        // Formatting then displaying the time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.cityClockView.text = timeFormat.format(zonedTime)
    }

    // Called when time is updated (if a new city is selected to be displayed on main frame)
    fun updateTimeVariable(updatedTime: Date?) {
        timeToUse = updatedTime
    }

    override fun getItemCount(): Int {
        return cities.size
    }
}