package com.example.fitnesstracker.data.local

enum class ActivityType( val usesGps: Boolean ) {
    RUNNING(usesGps = true),
    WALKING(usesGps = true),
    CYCLING(usesGps = true),
    OTHER(usesGps = true);

    companion object {
        fun fromString(value: String): ActivityType {
            return try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                OTHER
            }
        }
    }
}