package com.amora.storyapp.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.location.Geocoder
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Calendar
import java.util.Locale

object Utils {

	private fun generateTimestampForToday(): Long {
		val calendar = Calendar.getInstance()
		return calendar.timeInMillis
	}

	fun saveBitmap(bitmap: Bitmap, context: Context, fileName: String): Uri? {
		val wrapper = ContextWrapper(context)
		var file: File? = null
		try {
			// Create a file to save the bitmap
			val dir = wrapper.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
			file = File(dir, "${fileName}_${generateTimestampForToday()}")

			// Compress the bitmap and save it to the file
			val outputStream: OutputStream = FileOutputStream(file)
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
			outputStream.flush()
			outputStream.close()
		} catch (e: IOException) {
			e.printStackTrace()
		}

		// Get the URI using FileProvider
		return if (file != null) {
			return file.toUri() // Convert File to Uri
		} else {
			null
		}
	}

	fun materialDialog(context: Context, title: String?, message: String?, positiveTitle: String?, negativeTitle: String?, positiveAction: () -> Unit, negativeAction: () -> Unit, isCancelable: Boolean) {
		MaterialAlertDialogBuilder(context)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(positiveTitle) { dialog: DialogInterface?, which: Int ->
				positiveAction.invoke()
				dialog?.dismiss()
			}
			.setNegativeButton(negativeTitle) { dialog: DialogInterface?, which: Int ->
				negativeAction.invoke()
				dialog?.dismiss()
			}
			.setCancelable(isCancelable)
			.show()
	}

	fun generateLocation(context: Context, latitude: Double, longitude: Double): String {
		val geocoder = Geocoder(context, Locale.getDefault())
		var locationText: String
		return try {
			val addresses = geocoder.getFromLocation(latitude, longitude, 1)
			locationText = if (addresses?.isNotEmpty() == true) {
				val address = addresses[0]
				address.getAddressLine(0)
			} else {
				println("Location not found")
				"Location not found"
			}
			locationText
		} catch (e: Exception) {
			e.printStackTrace()
			locationText = "Location not found"
			locationText
		}
	}

	fun calculateTimeDifference(timestamp: String): String {
		val sourceFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
		sourceFormat.timeZone = TimeZone.getTimeZone("UTC")

		val utcDateTime = sourceFormat.parse(timestamp)

		val localCalendar = Calendar.getInstance()
		localCalendar.timeInMillis = utcDateTime.time

		val currentCalendar = Calendar.getInstance()
		val currentTime = currentCalendar.time

		val duration = (currentTime.time - localCalendar.timeInMillis) / 1000

		return when {
			duration >= 60 * 60 -> {
				val hours = duration / (60 * 60)
				"$hours jam yang lalu"
			}
			duration >= 60 -> {
				val minutes = duration / 60
				"$minutes menit yang lalu"
			}
			else -> "Baru saja"
		}
	}

}