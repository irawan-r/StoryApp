package com.amora.storyapp.ui.dashboard

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.input.key.Key.Companion.Copy
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.amora.storyapp.MainActivity
import com.amora.storyapp.R
import com.amora.storyapp.data.remote.model.StoryItem
import com.amora.storyapp.databinding.FragmentDashboardBinding
import com.amora.storyapp.ui.base.BaseFragment
import com.amora.storyapp.utils.State
import com.amora.storyapp.utils.Utils
import com.amora.storyapp.utils.showSnackbarNotice
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : BaseFragment<FragmentDashboardBinding, DashboardViewModel>(),
	AdapterStory.OnItemClickListener {
	override val inflateBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDashboardBinding
		get() = FragmentDashboardBinding::inflate
	override val viewModel: DashboardViewModel by viewModels()
	private lateinit var adapterStories: AdapterStory
	private lateinit var mlayoutManager: LinearLayoutManager
	private lateinit var scrollToolbar: AppBarLayout

	override fun onResume() {
		super.onResume()
		lifecycleScope.launch {
			viewModel.getStories()
			scrollToTop()
		}
	}


	override fun initView() {
		(requireActivity() as MainActivity).apply {
			setDashboardFragment(this@DashboardFragment)
			supportActionBar()
			supportToolBar(true)

			// Enable scrolling for the toolbar
			val layoutParams = getAppBarToolbar().layoutParams as CoordinatorLayout.LayoutParams
			layoutParams.behavior = AppBarLayout.Behavior()

			// Set the updated layout params for the toolbar
			getAppBarToolbar().layoutParams = layoutParams
			scrollToolbar = getAppBarToolbar()
		}

		adapterStories = AdapterStory(requireContext())
		adapterStories.setOnItemClickListener(this)
		adapterStories.setPostItemClickListener(this)
		adapterStories.setMapItemClickListener(this)
		showStories()

		val menuHost: MenuHost = requireActivity()
		menuHost.addMenuProvider(object : MenuProvider {
			override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
				menuInflater.inflate(R.menu.menu_main, menu)
			}

			override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
				return when (menuItem.itemId) {
					R.id.action_exit -> {
						Utils.materialDialog(
							requireContext(),
							"Keluar Akun",
							"Apakah yakin ingin keluar akun?",
							"Ya",
							"Tidak",
							{
								findNavController().navigateUp()
								viewModel.deleteSession()
								(requireActivity() as MainActivity).getSession()
							},
							{},
							false
						)
						true
					}

					else -> false
				}
			}
		}, viewLifecycleOwner, Lifecycle.State.STARTED)

		var isAppBarVisible = true
		var scrollDistance = 0

		binding?.rvStories?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				scrollDistance += dy

				if (dy > 0 && isAppBarVisible) {
					// Scrolling downwards
					if (scrollDistance > 2 * scrollToolbar.totalScrollRange) {
						// Hide the AppBarLayout
						scrollToolbar.setExpanded(false, true)
						isAppBarVisible = false
						scrollDistance = 0
					}
				} else if (dy < 0 && !isAppBarVisible) {
					// Scrolling upwards
					if (scrollDistance < -2 * scrollToolbar.totalScrollRange) {
						// Show the AppBarLayout
						scrollToolbar.setExpanded(true, true)
						isAppBarVisible = true
						scrollDistance = 0
					}
				}
			}
		})
	}

	fun scrollToTop() {
		val smoothScroller = object : LinearSmoothScroller(context) {
			override fun getVerticalSnapPreference(): Int {
				return SNAP_TO_START
			}
		}
		smoothScroller.targetPosition = 0
		binding?.rvStories?.layoutManager?.startSmoothScroll(smoothScroller)
	}

	override fun onItemClick(item: StoryItem) {
		val action =
			DashboardFragmentDirections.actionDashboardFragmentToFragmentDetailStory(item.id)
		val navOptions = NavOptions.Builder()
			.setEnterAnim(R.anim.slide_from_right)
			.setExitAnim(R.anim.slide_to_left)
			.build()
		findNavController().navigate(action, navOptions)
	}

	override fun onPostClick() {
		val navOptions = NavOptions.Builder()
			.setEnterAnim(R.anim.slide_from_right)
			.setExitAnim(R.anim.slide_to_left)
			.build()
		findNavController().navigate(R.id.StoryFragment, null, navOptions)
	}

	override fun onMapClick() {
		val navOptions = NavOptions.Builder()
			.setEnterAnim(R.anim.slide_from_right)
			.setExitAnim(R.anim.slide_to_left)
			.build()
		findNavController().navigate(R.id.MapFragment, null, navOptions)
	}


	private fun loadingState(toggle: Boolean) {
		binding?.apply {
			progressBar.isVisible = toggle
		}
	}

	override fun initObserver() {
		lifecycleScope.launch {
			viewModel.dashboardState.onEach { state ->
				when (state) {
					is State.Loading -> {
						loadingState(true)
					}

					is State.Error -> {
						loadingState(false)
						val messageApi = state.data.toString()
						val message = state.message
						if (message != null) {
							binding?.root?.showSnackbarNotice(message)
						} else {
							binding?.root?.showSnackbarNotice(messageApi)
						}
					}

					is State.Success -> {
						loadingState(false)
						adapterStories.submitData(lifecycle, state.data ?: PagingData.empty())
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
	}

	private fun showStories() {
		binding?.apply {
			rvStories.adapter = adapterStories.withLoadStateFooter(
				footer = LoadingStateAdapter { adapterStories.retry() }
			)
			rvStories.layoutManager = LinearLayoutManager(requireContext())
			mlayoutManager = rvStories.layoutManager as LinearLayoutManager
		}
	}
}