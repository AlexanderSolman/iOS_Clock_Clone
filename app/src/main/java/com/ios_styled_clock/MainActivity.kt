package com.ios_styled_clock

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class MainActivity : AppCompatActivity(), CitySelectionListener {
    // Declaring variables
    private lateinit var imAddTime: ImageButton
    private lateinit var citySelectionView: RecyclerView
    private lateinit var citiesAdapter: CitiesAdapter
    private lateinit var citySelectAdapter: CitySelectionAdapter
    private var cityList: MutableList<Cities> = ArrayList()
    private var timeToUse: Date? = Date(System.currentTimeMillis())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setting the views
        setContentView(R.layout.activity_main)
        imAddTime = findViewById(R.id.addButton)
        citySelectionView = findViewById(R.id.citySelectionView)

        // Initiating the CityAdapter and listener for selection
        citiesAdapter = CitiesAdapter(this, cityList)
        citiesAdapter.setCitySelectionListener(this)

        // Initiating the CitySelectionAdapter to handle the selected cities on main frame
        citySelectAdapter = CitySelectionAdapter(cityList, timeToUse)
        citySelectionView.adapter = citySelectAdapter
        citySelectionView.layoutManager = LinearLayoutManager(this)

        // Add button listener
        imAddTime.setOnClickListener{view ->
            var fragment: Fragment? = null
            when (view.id) {
                R.id.addButton -> {
                    // Creating a new instance of fragment, passing the mainactivity to not overwrite the listener
                    fragment = BottomFragment.newInstance(this)
                }
            }
            if (fragment != null){
                // TODO Make fragment animation
                // Setting the fragment visible
                val fragmentVisible = findViewById<FragmentContainerView>(R.id.fragmentView)
                if (fragmentVisible.visibility == View.GONE) { fragmentVisible.visibility = View.VISIBLE }

                // Handling the fragment transaction
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentView, fragment)
                transaction.commit()
            }
        }
    }

    // Interface listener, adding selected cities to main frame
    override fun onCitySelected(city: Cities) {
        cityList.add(city)
        // Updating the time variable used in city selection
        citySelectAdapter.updateTimeVariable(timeToUse)
        citySelectionView.adapter?.notifyItemInserted(cityList.size - 1)
    }

    // Continuous update of clock
    private fun updateClock() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        // Going through selected list and updating each displayed clock
        cityList.forEachIndexed { index, city ->
            // Get time zone and calculate offset
            val timeZone = TimeZone.getTimeZone(city.getTimeZone())
            val zonedTime = timeToUse?.let { Date(it.time + timeZone.getOffset(it.time) - (1 * 3600 * 1000)) } ?: Date()
            // Updating the textview with the formatted time
            val viewHolder = citySelectionView.findViewHolderForAdapterPosition(index) as CitySelectionAdapter.ViewHolder?
            viewHolder?.cityClockView?.text = zonedTime.let { timeFormat.format(it) }
        }
    }
}
