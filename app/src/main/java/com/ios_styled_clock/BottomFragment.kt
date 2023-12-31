package com.ios_styled_clock

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BottomFragment() : Fragment() {

    // Interface to handle the search queries
    interface CityQueryListener {
        fun onCityQueryListener(searchParameter: String)
    }

    // Setting variables for listeners and adapter
    private var cityQueryListener: CityQueryListener? = null
    private var citySelectionListener: CitySelectionListener? = null
    private var citiesAdapter: CitiesAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CitySelectionListener) {
            citySelectionListener = context
        }
        if (context is CityQueryListener) {
            cityQueryListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottom, container, false)
        // Search bar
        val searchView = view.findViewById<SearchView>(R.id.search_bar)
        // Cancel button
        val cancelButton = view.findViewById<TextView>(R.id.cancel_button)

        // Recycler view for list of cities
        val citiesRecyclerView = view.findViewById<RecyclerView>(R.id.citiesRecyclerView)
        citiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Reading json file creating cities objects setting variable to hold them
        val citiesJsonSource = CitiesJsonSource(requireContext())
        val citiesList = citiesJsonSource.getCities()
        // Setting the adapter variable to hold the rv of CitiesAdapter
        citiesAdapter = CitiesAdapter(requireContext(), sortList(citiesList))
        // Assuring that the listener is not overwritten with new instance
        citySelectionListener?.let { citiesAdapter!!.setCitySelectionListener(it) }
        // Setting the rv to the rv.adapter i.e displaying the list of cities
        citiesRecyclerView.adapter = citiesAdapter

        // Listener for cancel button
        cancelButton.setOnClickListener{
            closeFragment()
            // Setting background of main recycleview back to red
            val citySelectionView = (activity as MainActivity).findViewById<RecyclerView>(R.id.citySelectionView)
            citySelectionView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.scarlet))
        }

        // Listener for search bar
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Searches in list using the submitted text string
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    cityQueryListener?.onCityQueryListener(query)
                }
                return true
            }
            // Updates the list while text string is being written
            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = filterSearch(citiesList, newText)
                citiesAdapter = CitiesAdapter(requireContext(), sortList(filteredList))
                citySelectionListener?.let { citiesAdapter!!.setCitySelectionListener(it) }
                citiesRecyclerView.adapter = citiesAdapter
                return true
            }
        })
        return view
    }

    // Function to handle closing the fragment and add exit animation
    fun closeFragment() {
        val fragmentVisible = view?.findViewById<FrameLayout>(R.id.fragmentView)
        if (fragmentVisible != null) {
            if (fragmentVisible.visibility == View.VISIBLE) {
                fragmentVisible.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fragment_animation_exit))
                fragmentVisible.visibility = View.GONE
            }
        }
    }

    fun filterSearch(unFilteredList: List<Cities>, query: String?): List<Cities> {
        // Returning if search is blank
        if (query.isNullOrBlank()){ return unFilteredList }
        // Looping the list to filter city or country by search string
        val filteredList = mutableListOf<Cities>()
        for (city in unFilteredList) {
            if (city.getCity().contains(query, ignoreCase = true) || city.getCountry().contains(query, ignoreCase = true)) {
                filteredList.add(city)
            }
        }
        // Returning the filtered list
        return filteredList
    }

    // Function to handling sorting by city
    fun sortList(citiesList: List<Cities>): List<Cities> {
        return citiesList.sortedBy { it.getCity() }
    }

    // Called from main when new instance of fragment is called, listener is set
    companion object {
        @JvmStatic
        fun newInstance(citySelectionListener: CitySelectionListener) =
            BottomFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}