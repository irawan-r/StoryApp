package com.amora.storyapp.ui.story

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.amora.storyapp.data.remote.model.StoryRequest
import com.amora.storyapp.databinding.FragmentStoryBinding
import com.amora.storyapp.ui.base.BaseFragment
import com.amora.storyapp.utils.Constant
import com.amora.storyapp.utils.LocationManager
import com.amora.storyapp.utils.State
import com.amora.storyapp.utils.Utils
import com.amora.storyapp.utils.showSnackbarNotice
import com.amora.storyapp.utils.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentStory: BaseFragment<FragmentStoryBinding, StoryViewModel>() {
	override val inflateBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentStoryBinding
		get() = FragmentStoryBinding::inflate
	override val viewModel: StoryViewModel by viewModels()
	private var imgUri = ""
	private var locationManager: LocationManager? = null
	private var locationUser: Location? = null
	private var isCheck: Boolean = false
	private val cameraLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == Activity.RESULT_OK) {
				val imageBitmap = result.data?.extras?.get("data") as Bitmap
				val uriImg = Utils.saveBitmap(imageBitmap, requireContext(), "IMG")
				imgUri = uriImg?.path.toString()
				binding?.ivImagePost?.visibility = View.VISIBLE
				binding?.ivImagePost?.setImageBitmap(imageBitmap)
			}
		}

	private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			val uriImg =  result.data?.data
			imgUri = uriImg.toString()
			binding?.ivImagePost?.visibility = View.VISIBLE
			binding?.ivImagePost?.setImageURI(uriImg)
		}
	}

	private fun openGallery() {
		val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
		intent.addCategory(Intent.CATEGORY_OPENABLE)
		intent.type = "image/*"
		intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
		galleryLauncher.launch(intent)
	}

	private fun openCamera() {
		val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
		cameraLauncher.launch(intent)
	}

	override fun initView() {
		binding?.apply {
			btPhoto.setOnClickListener {
				MaterialAlertDialogBuilder(requireContext())
					.setTitle("Pilih gambar")
					.setPositiveButton("Camera") { dialog, _ ->
						openCamera()
						dialog.dismiss()
					}
					.setNegativeButton("Gallery") { dialog, _ ->
						openGallery()
						dialog.dismiss()
					}
					.show()
			}
			btPost.setOnClickListener {
				val story = StoryRequest().apply {
					this.description = etDescription.text.toString()
					this.photo = imgUri
					if (isCheck) {
						this.lat = locationUser?.latitude
						this.lon = locationUser?.longitude
					}
				}
				viewModel.sendIdeas(requireContext(), story)
			}
			rbLocation.setOnClickListener {
				if (locationManager == null) {
					startLocation()
				}
				isCheck = !isCheck
				rbLocation.isChecked = isCheck
				if (isCheck) {
					locationManager?.startLocationTracking()
				} else {
					locationManager?.stopLocationTracking()
				}
			}
		}
	}

	override fun onResume() {
		super.onResume()
		if (isCheck) {
			locationManager?.startLocationTracking()
		} else {
			locationManager?.stopLocationTracking()
		}
		binding?.rbLocation?.isChecked = isCheck
	}


	private fun startLocation() {
		locationManager = LocationManager(
			requireActivity(),
			Constant.normalTimeInterval,
			Constant.normalMinimalDistance
		) { location: Location ->
			locationUser = location
		}
	}

	override fun initObserver() {
		lifecycleScope.launch {
			viewModel.postResult.onEach { state ->
				when (state) {
					is State.Loading -> {
						loadingState(true)
					}

					is State.Error -> {
						val messageApi = state.data?.message
						val message = state.message
						if (message != null) {
							toast(requireContext(), message)
						} else {
							toast(requireContext(), messageApi.toString())
						}
					}

					is State.Success -> {
						findNavController().navigateUp()
						val messageApi = state.data?.message
						val message = state.message
						if (message != null) {
							binding?.root?.showSnackbarNotice(message)
						} else {
							binding?.root?.showSnackbarNotice(messageApi.toString())
						}
					}

					else -> {
						loadingState(false)
					}
				}
			}.onCompletion {
				viewModel.resetState()
			}.collect()
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		viewModel.resetState()
		locationManager?.stopLocationTracking()
	}

	private fun loadingState(toggle: Boolean) {
		binding?.apply {
			progressBar.isVisible = toggle
			etDescription.isEnabled = !toggle
			btPost.isEnabled = !toggle
			btPhoto.isEnabled = !toggle
		}
	}
}