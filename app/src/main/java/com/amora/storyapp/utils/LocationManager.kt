package com.amora.storyapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.amora.storyapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber

@SuppressLint("MissingPermission")
class LocationManager(
	activity: Activity,
	private var timeInterval: Long,
	private var minimalDistance: Float,
	val location: ((Location) -> Unit)? = null
) : LocationCallback() {

	private var request: LocationRequest
	private var locationClient: FusedLocationProviderClient

	init {
		// get network provider status
		activity.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
		// getting the location client
		locationClient =
			LocationServices.getFusedLocationProviderClient(activity)
		request = createRequest()
		startGetLocation(activity)
	}

	private fun createRequest(): LocationRequest =
		// New builder
		LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, timeInterval).apply {
			setMinUpdateDistanceMeters(minimalDistance)
			setMinUpdateIntervalMillis(timeInterval)
			setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
			setWaitForAccurateLocation(true)
		}.build()

	private fun changeRequest(timeInterval: Long, minimalDistance: Float) {
		this.timeInterval = timeInterval
		this.minimalDistance = minimalDistance
		createRequest()
		stopLocationTracking()
		startLocationTracking()
	}

	fun startLocationTracking() {
		try {
			request = createRequest()
			locationClient.requestLocationUpdates(request, this, Looper.getMainLooper())
		} catch (_: Exception) {
		}
	}

	fun stopLocationTracking() {
		try {
			locationClient.removeLocationUpdates(this)
		} catch (_: Exception) {
		}
	}

	private fun startGetLocation(activity: Activity) {
		if (PermissionUtils.isLocationEnabled(activity) && PermissionUtils.isAccessLocationGranted(
				activity
			)
		) {
			getCurrentLocation(activity)
		} else {
			when {
				!PermissionUtils.isAccessLocationGranted(activity) -> {
					val builder = MaterialAlertDialogBuilder(activity)
					builder.setTitle("Permission aplikasi belum diberikan.")
					builder.setMessage(
						"Untuk menggunakan fitur lokasi, diperlukan akses GPS"
					)
					builder.setPositiveButton(
						"Ijinkan"
					) { dialog: DialogInterface?, _: Int ->
						dialog?.dismiss()
						ActivityCompat.requestPermissions(
							activity,
							arrayOf(
								android.Manifest.permission.ACCESS_FINE_LOCATION,
								android.Manifest.permission.ACCESS_COARSE_LOCATION
							),
							LOCATION_PERMISSION_REQUEST_CODE
						)
					}
					builder.setNegativeButton("Tutup") { dialog: DialogInterface?, _: Int ->
						dialog?.dismiss()
					}
					val dialog = builder.create()
					dialog.setCancelable(false)
					dialog.setCanceledOnTouchOutside(false)
					dialog.show()
				}

				(!PermissionUtils.isLocationEnabled(activity)) -> {
					PermissionUtils.showGPSNotEnabledDialog(activity)
				}
			}
		}
	}

	private fun getCurrentLocation(activity: Activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			locationClient.getCurrentLocation(
				Priority.PRIORITY_HIGH_ACCURACY,
				object : CancellationToken() {
					override fun isCancellationRequested(): Boolean {
						return false
					}

					override fun onCanceledRequested(onTokenCanceledListener: OnTokenCanceledListener): CancellationToken {
						return CancellationTokenSource().token
					}
				}).addOnSuccessListener { location: Location? ->
				try {
					if (location != null) {
						this.location?.let { it(location) }
					} else {
						startLocationTracking()
						getLastLocation(activity = activity)
					}
				} catch (e: Exception) {
					getLastLocation(activity = activity)
					Timber.tag("Error current location").e(e)
				}
			}
		} else {
			getLastLocation(activity = activity)
		}
	}

	private fun getLastLocation(activity: Activity) {
		try {
			locationClient.lastLocation
				.addOnSuccessListener(
					activity
				) { location: Location? ->
					if (location != null) {
						this.location?.let { it(location) }
					} else {
						startLocationTracking()
					}
				}
		} catch (e: Exception) {
			startLocationTracking()
			Timber.tag("Error get last location").d(e)
		}
	}

	override fun onLocationResult(location: LocationResult) {
		super.onLocationResult(location)
		val lastLocation = location.lastLocation ?: location.locations.lastOrNull()
		if (lastLocation != null) {
			this.location?.let { update ->
				update(lastLocation)
			}
		} else {
			// Handle case where no location is available
			startLocationTracking()
		}
	}

	override fun onLocationAvailability(availability: LocationAvailability) {
		if (availability.isLocationAvailable) {
			Timber.tag("Loc").d("Location updates are available.")
		} else {
			Timber.tag("Loc").d("Location updates are not available.")
		}
	}

	companion object {
		private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
	}

}