package it.ter.sync.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class Utils {
    companion object {
        fun calculateDistance(
            lat1: Double, lon1: Double, // Coordinate del primo punto
            lat2: Double, lon2: Double  // Coordinate del secondo punto
        ): Double {
            val earthRadius = 6371 // Raggio medio della Terra in chilometri

            // Converti le coordinate in radianti
            val lat1Rad = Math.toRadians(lat1)
            val lon1Rad = Math.toRadians(lon1)
            val lat2Rad = Math.toRadians(lat2)
            val lon2Rad = Math.toRadians(lon2)

            // Calcola la differenza tra le latitudini e le longitudini
            val dLat = lat2Rad - lat1Rad
            val dLon = lon2Rad - lon1Rad

            // Applica la formula di Haversine
            val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            return earthRadius * c
        }

        fun generateChatId(string1: String, string2: String): String {
            val minString = if (string1 < string2) string1 else string2
            val maxString = if (minString == string1) string2 else string1

            return minString + maxString
        }
    }
}