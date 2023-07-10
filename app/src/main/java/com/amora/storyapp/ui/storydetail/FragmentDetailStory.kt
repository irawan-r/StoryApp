package com.amora.storyapp.ui.storydetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.amora.storyapp.R
import com.amora.storyapp.data.remote.model.Story
import com.amora.storyapp.databinding.FragmentDetailStoryBinding
import com.amora.storyapp.ui.base.BaseFragment
import com.amora.storyapp.utils.State
import com.amora.storyapp.utils.Utils
import com.amora.storyapp.utils.showSnackbarNotice
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.color.utilities.MaterialDynamicColors.onError
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentDetailStory: BaseFragment<FragmentDetailStoryBinding, DetailStoryViewModel>() {
	override val inflateBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDetailStoryBinding
		get() = FragmentDetailStoryBinding::inflate
	override val viewModel: DetailStoryViewModel by viewModels()

	override fun initView() {
		val itemId = arguments?.getString("arg")
		viewModel.getStory(itemId.toString())
	}

	override fun initObserver() {
		lifecycleScope.launch {
			viewModel.detailStory.onEach { state ->
				when (state) {
					is State.Loading -> {
						loadingState(true)
					}

					is State.Error -> {
						hideStory()
						val messageApi = state.data?.message
						val message = state.message
						if (message != null) {
							binding?.root?.showSnackbarNotice(message)
						} else {
							binding?.root?.showSnackbarNotice(messageApi.toString())
						}
						loadingState(false)
					}

					is State.Success -> {
						loadingState(false)
						showStory(state.data?.story!!)
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

	private fun hideStory() {
		val gone = View.GONE
		binding?.apply {
			ivStoryImage.visibility = gone
			tvStoryUser.visibility = gone
			tvStoryLocation.visibility = gone
			tvStoryDescription.visibility = gone
			tvStoryTimeStamp.visibility = gone
			tvStoryLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
		}
	}

	private fun showStory(item: Story) {
		val gone = View.GONE
		val visible = View.VISIBLE
		binding?.apply {
			tvStoryDescription.text = item.description
			tvStoryUser.text = item.name
			if (item.lat != null && item.lon != null) {
				tvStoryLocation.text = Utils.generateLocation(requireContext(), item.lat, item.lon)
				if (tvStoryLocation.text.toString().lowercase().contains("not found", true)) {
					tvStoryLocation.visibility = gone
				} else {
					tvStoryLocation.visibility = visible
				}
			} else {
				tvStoryLocation.visibility = gone
			}
			val requestOptions = RequestOptions()
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.centerCrop()
				.error(R.drawable.broken_img)
			Glide.with(requireActivity())
				.load(item.photoUrl)
				.apply(requestOptions)
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.apply(RequestOptions.centerCropTransform())
				.into(ivStoryImage)
		}
	}

	private fun loadingState(toggle: Boolean) {
		binding?.apply {
			progressBar.isVisible = toggle
			tvStoryDescription.isVisible = !toggle
			tvStoryLocation.isVisible = !toggle
			tvStoryUser.isVisible = !toggle
			tvStoryTimeStamp.isVisible = !toggle
			ivStoryImage.isVisible = !toggle
		}
	}
}