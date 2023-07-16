package com.amora.storyapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.amora.storyapp.databinding.ActivityMainBinding
import com.amora.storyapp.ui.dashboard.DashboardFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

	private lateinit var appBarConfiguration: AppBarConfiguration
	private lateinit var binding: ActivityMainBinding
	private lateinit var navController: NavController
	private val viewModel: MainViewModel by viewModels()
	private var backPressCallback: OnBackPressedCallback? = null
	private var backPressCount = 0
	private val mainCoroutine = CoroutineScope(Dispatchers.Main)
	private var backPressJob: Job? = null
	private var menuItem: MenuItem? = null
	private var dashboardFragment: DashboardFragment? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		initViews()
		initObserver()
	}

	private fun initViews() {
		WindowCompat.setDecorFitsSystemWindows(window, false)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setSupportActionBar(binding.toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(false)
		val navHostFragment =
			supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
		navController = navHostFragment.navController
		navController.addOnDestinationChangedListener(this)
		appBarConfiguration = AppBarConfiguration(navController.graph)
		setupActionBarWithNavController(navController, appBarConfiguration)
	}

	private fun initObserver() {
		lifecycleScope.launch {
			viewModel.navGraphDestination.collectLatest { destination ->
				if (destination != null) {
					navController.navigate(destination)
				}
			}
		}
	}

	fun getSession() = viewModel.getSession()

	fun getAppBarToolbar() = binding.appBarLayout

	private fun getScrollToolbar() = binding.clToolbar

	override fun onSupportNavigateUp(): Boolean {
		return navController.navigateUp() || super.onSupportNavigateUp()
	}

	fun supportActionBar(isVisible: Boolean? = false) {
		if (isVisible != null) {
			supportActionBar?.setDisplayHomeAsUpEnabled(isVisible)
		}
	}

	fun supportToolBar(isVisible: Boolean? = false) {
		if (isVisible != null) {
			binding.toolbar.isVisible = isVisible
		}
	}

	fun setDashboardFragment(fragment: DashboardFragment) {
		dashboardFragment = fragment
	}

	override fun onDestinationChanged(
		controller: NavController,
		destination: NavDestination,
		arguments: Bundle?
	) {

		when (destination.id) {
			R.id.DashboardFragment -> {
				enableBackPressExit(destination)
				getScrollToolbar().visibility = View.VISIBLE
			}

			else -> {
				menuItem?.isVisible = false
				enableBackPress()
				getScrollToolbar().visibility = View.GONE
			}
		}
	}

	private fun enableBackPress() {
		removeBackPress()
		backPressCallback = object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				navController.navigateUp()
			}
		}
	}

	private fun removeBackPress() {
		backPressCallback?.remove()
	}

	private fun enableBackPressExit(destination: NavDestination) {
		removeBackPress()
		backPressCallback = object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				if (backPressCount == 0) {
					if (destination.id == R.id.DashboardFragment) {
						dashboardFragment?.scrollToTop()
					}
					Toast.makeText(
						this@MainActivity,
						"Tekan sekali untuk keluar",
						Toast.LENGTH_SHORT
					).show()
					backPressCount++
					backPressJob = mainCoroutine.launch {
						delay(2000)
						backPressCount = 0
					}
				} else {
					this@MainActivity.finish()
				}
			}
		}

		addBackpressCallback()
	}

	private fun addBackpressCallback() {
		if (backPressCallback != null) {
			this@MainActivity.onBackPressedDispatcher.addCallback(backPressCallback!!)
		}
	}
}