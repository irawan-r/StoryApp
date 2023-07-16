package com.amora.storyapp.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.pow

object LocationUtils {
	private const val EARTH_RADIUS = 6371 // Earth radius in kilometers

	fun distanceBetween(point1: LatLng, point2: LatLng): Double {
		val lat1 = Math.toRadians(point1.latitude)
		val lon1 = Math.toRadians(point1.longitude)
		val lat2 = Math.toRadians(point2.latitude)
		val lon2 = Math.toRadians(point2.longitude)

		val dLat = lat2 - lat1
		val dLon = lon2 - lon1

		val a = Math.sin(dLat / 2).pow(2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2).pow(2)
		val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
		val distance = EARTH_RADIUS * c

		return distance * 1000 // Convert distance to meters
	}
}






