package com.amora.storyapp.ui.dashboard

import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
class DashboardFragment : BaseFragment<FragmentDashboardBinding, DashboardViewModel>(), AdapterStory.OnItemClickListener {
	override val inflateBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDashboardBinding
		get() = FragmentDashboardBinding::inflate
	override val viewModel: DashboardViewModel by viewModels()
	private lateinit var adapterStories: AdapterStory

	override fun onResume() {
		super.onResume()
		lifecycleScope.launch {
			viewModel.getStories()
		}
	}

	override fun initView() {
		(requireActivity() as MainActivity).apply {
			supportActionBar()
			supportToolBar(true)

			// Enable scrolling for the toolbar
			val layoutParams = getAppBarToolbar().layoutParams as CoordinatorLayout.LayoutParams
			layoutParams.behavior = AppBarLayout.Behavior()

			// Set the updated layout params for the toolbar
			getAppBarToolbar().layoutParams = layoutParams
		}
		binding?.apply {
			btPost.setOnClickListener {
				findNavController().navigate(R.id.StoryFragment)
			}
		}
		adapterStories = AdapterStory(requireContext())
		adapterStories.setOnItemClickListener(this)
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

	}

	override fun onItemClick(item: StoryItem) {
		val action = DashboardFragmentDirections.actionDashboardFragmentToFragmentDetailStory(item.id.toString())
		findNavController().navigate(action)
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
						val messageApi = state.data?.message
						val message = state.message
						if (message != null) {
							binding?.root?.showSnackbarNotice(message)
						} else {
							binding?.root?.showSnackbarNotice(messageApi.toString())
						}
					}

					is State.Success -> {
						loadingState(false)
						adapterStories.differ.submitList(state.data?.listStory)
						showStories()
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
			rvStories.adapter = adapterStories
			rvStories.layoutManager = LinearLayoutManager(requireContext())
		}
	}
}