package com.ios_styled_clock

class Cities(private var city: String, private var country: String, private var timeZone: String) {
    override fun toString(): String {
        return "$city, $country"
    }
    fun getCity(): String {
        return city
    }
    fun getCountry(): String {
        return country
    }
    fun getTimeZone(): String {
        return timeZone
    }
}