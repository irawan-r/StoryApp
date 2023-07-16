package com.amora.storyapp.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object PermissionUtils {

	/**
	 * Function to check if the location permissions are granted or not
	 */
	fun isAccessLocationGranted(context: Context): Boolean {
		return ContextCompat
			.checkSelfPermission(
				context,
				Manifest.permission.ACCESS_FINE_LOCATION,
			) == PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(
					context,
					Manifest.permission.ACCESS_COARSE_LOCATION
				) == PackageManager.PERMISSION_GRANTED
	}

	/**
	 * Function to check if location of the device is enabled or not
	 */
	fun isLocationEnabled(activity: Activity): Boolean {
		val locationManager: LocationManager =
			activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
	}

	/**
	 * Function to show the "enable GPS" Dialog box
	 */
	fun showGPSNotEnabledDialog(context: Context) {
		MaterialAlertDialogBuilder(context)
			.setTitle("GPS tidak dinyalakan!")
			.setMessage("Aplikasi butuh GPS untuk mendapatkan lokasi, nyalakan GPS?")
			.setPositiveButton(
				"Ya"
			) { _, _ ->
				val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
				context.startActivity(intent)
			}
			.setNegativeButton(
				"Tidak"
			) { dialog, which ->
				dialog.cancel()
				(context as Activity).finish()
			}
			.setCancelable(false)
			.show()
	}
}