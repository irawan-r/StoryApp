package com.amora.storyapp.utils

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

object ApiUtils {
	inline fun <reified T> T.toRequestBodyPart(): RequestBody {
		val mediaType = "multipart/form-data".toMediaTypeOrNull()
		val requestBody = this.toString().toRequestBody(mediaType)
		return requestBody
	}

	fun RequestBody.toMultiPartBody(file: File, title: String): MultipartBody.Part {
		return MultipartBody.Part.createFormData(title, file.name, this)
	}

	fun Uri.toRequestBodyPart(contentResolver: ContentResolver): RequestBody? {
		val contentType = contentResolver.getType(this)?.toMediaTypeOrNull()
		val file = this.path?.let { File(it) }
		return file?.asRequestBody(contentType)
	}
}