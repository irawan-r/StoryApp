package com.amora.storyapp.data.local


import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.amora.storyapp.data.remote.model.User
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext

class SessionManager private constructor(context: Context) {
	private val gson = Gson()
	private var masterKey = MasterKey.Builder(context)
		.setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
		.build()

	private var preferences = EncryptedSharedPreferences.create(
		context,
		"Story_App_Pref",
		masterKey,
		EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
		EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
	)

	fun loginSession(user: User) {
		preferences.edit {
			putString(KEY_USER_LOGIN, gson.toJson(user))
		}
	}

	fun getSession(): User? {
		val user = preferences.getString(KEY_USER_LOGIN, null)
		return gson.fromJson(user, User::class.java)
	}

	fun deleteSession() {
		preferences.edit {
			remove(KEY_USER_LOGIN)
		}
	}

	companion object {
		private var INSTANCE: SessionManager? = null

		const val KEY_USER_LOGIN = "user-login"

		fun getInstance(@ApplicationContext context: Context): SessionManager {
			return INSTANCE ?: synchronized(this) {
				val manager = SessionManager(context)
				INSTANCE = manager
				manager
			}
		}
	}
}