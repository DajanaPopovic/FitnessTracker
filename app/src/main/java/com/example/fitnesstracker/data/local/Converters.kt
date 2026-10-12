package com.example.fitnesstracker.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromLatLngPointList(points: List<LatLngPoint>?): String? {
        if (points == null) return null
        return gson.toJson(points)
    }

    @TypeConverter
    fun toLatLngPointList(json: String?): List<LatLngPoint>? {
        if (json == null) return null
        val type = object : TypeToken<List<LatLngPoint>>() {}.type
        return gson.fromJson(json, type)
    }
}