package com.amora.storyapp.ui.splashscreen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.amora.storyapp.MainActivity
import com.amora.storyapp.databinding.SplashcreenBinding

class SplashScreen: ComponentActivity() {

	private lateinit var binding: SplashcreenBinding
	private val viewModel: SplashViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = SplashcreenBinding.inflate(layoutInflater)
		installSplashScreen().apply {
			// Keep the splash screen visible for this Activity
			setKeepOnScreenCondition {
				viewModel.isLoading.value
			}
		}

		Intent(this@SplashScreen, MainActivity::class.java).also {
			startActivity(it)
			finish()
		}
		setContentView(binding.root)
	}
}