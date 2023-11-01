package com.ios_styled_clock

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView

class CitiesAdapter(private val context: Context, private val citiesList: List<Cities>): RecyclerView.Adapter<CitiesAdapter.ViewHolder>() {

    // Setting the listener through main
    private var citySelectionListener: CitySelectionListener? = null
    fun setCitySelectionListener(listener: CitySelectionListener) {
        citySelectionListener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cityTextView: TextView = itemView.findViewById(R.id.citiesTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_in_city, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Setting the view in the list menu
        val city = citiesList[position]
        holder.cityTextView.text = city.toString()

        // Listening for city selection choices and sending it back to main
        holder.itemView.setOnClickListener {
            val selectedCity = citiesList[position]
            citySelectionListener?.onCitySelected(selectedCity)
        }
    }

    override fun getItemCount(): Int {
        return citiesList.size
    }
}