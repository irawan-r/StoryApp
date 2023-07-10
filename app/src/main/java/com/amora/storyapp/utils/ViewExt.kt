package com.amora.storyapp.utils

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar

fun View.showSnackbarNotice(message: String, anchorView: View? = null) {
	Snackbar.make(this, message, Snackbar.LENGTH_LONG).also { snackbar ->
		val snackbarView = snackbar.view
		val textView =
			snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
		textView.maxLines = 5
		anchorView?.let {
			snackbar.anchorView = it
		}
	}.show()
}

fun toast(context: Context, message: String) {
	Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}