package com.amora.storyapp.utils

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.DrawableCompat
import com.airbnb.lottie.LottieAnimationView


class LottieDrawable(private val lottieAnimationView: LottieAnimationView) : Drawable() {
	override fun draw(canvas: Canvas) {
		lottieAnimationView.draw(canvas)
	}

	override fun setAlpha(alpha: Int) {
		// Not needed
	}

	override fun setColorFilter(colorFilter: ColorFilter?) {
		// Not needed
	}

	override fun getOpacity(): Int {
		return lottieAnimationView.alpha.toInt()
	}
}
