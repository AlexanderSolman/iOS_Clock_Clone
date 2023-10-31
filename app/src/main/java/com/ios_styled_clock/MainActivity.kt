package com.ios_styled_clock

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instacart.library.truetime.TrueTime
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.Timer
import java.util.TimerTask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity(), CitySelectionListener {
    // Class variables
    private lateinit var imAddTime: TextView
    private lateinit var isSynced: TextView
    private lateinit var citySelectionView: RecyclerView
    private lateinit var citiesAdapter: CitiesAdapter
    private lateinit var citySelectAdapter: CitySelectionAdapter

    // List and booleans
    private var cityList: MutableList<Cities> = ArrayList()
    private var isInternetAvailable: Boolean = false

    // Time variables
    private var timeToUse: Date? = Date(System.currentTimeMillis())
    private var trueTime: Date? = null
    private var timeDifference: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Checking if there is a saved state (if == null, citylist does not have to load)
        if (savedInstanceState != null) {
            timeDifference = savedInstanceState.getLong("timeDifference", timeDifference)
            cityList = getCityListSP()
        }

        // Setting the views
        setContentView(R.layout.activity_main)
        imAddTime = findViewById(R.id.addButton)
        isSynced = findViewById(R.id.syncedNtp)
        citySelectionView = findViewById(R.id.citySelectionView)

        val swipeToDeleteCallback = SwipeRemoveCities(this)
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(citySelectionView)

        //Checking network
        checkInternet()

        // NTP call task running each new minute
        val ntpCallTimer = Timer()
        ntpCallTimer.scheduleAtFixedRate(object : TimerTask() { override fun run() { runTaskOne() } }, initialDelay(), 60 * 1000)

        // Keeping the clock up to date continuously
        val clockUpdateTimer = Timer()
        clockUpdateTimer.scheduleAtFixedRate(object : TimerTask() { override fun run() { runTaskTwo() } },0, 1000)

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

    // Saving instance states through bundle
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("timeDifference", timeDifference)
    }
    // Using onPause to save the list
    override fun onPause() {
        super.onPause()
        setCityListSP(cityList)
    }

    // "Setter" for saving the list of cities using shared preferences
    private fun setCityListSP(cityList: MutableList<Cities>) {
        // Setting up storage and data modifier using gson to serialize into json
        val sp = getSharedPreferences("Cities_List", Context.MODE_PRIVATE)
        val editor = sp.edit()
        val gson = Gson()
        val json = gson.toJson(cityList)
        editor.putString("cityList", json)
        editor.apply()
    }

    // "Getter" for loading the list of cities using shared preferences
    private fun getCityListSP(): MutableList<Cities> {
        // Reading and deserializing json
        val sp = getSharedPreferences("Cities_List", Context.MODE_PRIVATE)
        val json = sp.getString("cityList", null)
        val gson = Gson()
        return gson.fromJson(json, object : TypeToken<MutableList<Cities>>() {}.type) ?: ArrayList()
    }

    // Necessary annotation for coroutine below
    @OptIn(DelicateCoroutinesApi::class)
    private fun runTaskOne() {
        if (isInternetAvailable) {

            // Running the NTP call in a coroutine to not block the
            // program and make sure variables are set before use
            GlobalScope.launch(Dispatchers.IO) {

                // Calculating NTP call time i.e roundtrip (t4-t1)
                // Division with 2 would be as close to server time as possible
                val startTime = System.currentTimeMillis()
                trueTimeNtp()
                val endTime = System.currentTimeMillis()
                val calculateRoundTripTime = (endTime - startTime) / 2

                // Time difference holds the true ntp time against system time
                if(trueTime != null) { timeDifference = trueTime!!.time - System.currentTimeMillis() + calculateRoundTripTime }
            }
        }else {
            timeDifference = 0
        }
    }

    // Continuously keeps the ui updated
    private fun runTaskTwo() {
        val systemTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // If a connection exists and NTP time has been received, use NTP time else default system time
        if (isInternetAvailable && trueTime != null){
            timeToUse = Date(systemTime + timeDifference)
            runOnUiThread { isSynced.text = getString(R.string.ntp_synced) }
        } else {
            timeToUse = Date(systemTime)
            runOnUiThread { isSynced.text = getString(R.string.ntp_not_synced) }
        }
        // In case running on ntp time assuring calendar has time difference
        calendar.time = timeToUse!!
        // Updating the clock every new minute on the ui thread
        if (calendar[Calendar.SECOND] == 0) { runOnUiThread { updateClock() } }
    }

    // Interface listener, adding selected cities to main frame
    override fun onCitySelected(city: Cities) {
        cityList.add(city)
        // Updating the time variable used in city selection
        citySelectAdapter.updateTimeVariable(timeToUse)
        citySelectionView.adapter?.notifyItemInserted(cityList.size - 1)
    }

    fun removeSelectedCity(position: Int) {
        if (position >= 0 && position < cityList.size) {
            cityList.removeAt(position)
            citySelectAdapter.notifyItemRemoved(position)
        }
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

    private fun checkInternet() {
        // Setting up cm for checking connection and setting a request
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder().build()

        // Callback with parameters request, and result of avail/lost
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { isInternetAvailable = true }
            override fun onLost(network: Network) { isInternetAvailable = false }
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    // Calculating the initial delay until next minute
    private fun initialDelay(): Long {
        val calendar = Calendar.getInstance()
        val differenceInSeconds = 60 - calendar[Calendar.SECOND]
        return (differenceInSeconds * 1000).toLong()
    }

    private suspend fun trueTimeNtp() {
        try {
            // Setting up truetime to connect to NTP host '1.se.pool.ntp.org'
            TrueTime.build()
                .withNtpHost("1.se.pool.ntp.org")
                .withConnectionTimeout(31428)
                .withSharedPreferencesCache(this@MainActivity)
                .withLoggingEnabled(true)
                .initialize()
            // Getting the time from ntp server and setting it to trueTime
            if (TrueTime.isInitialized()) { trueTime = Date(TrueTime.now()!!.time) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
