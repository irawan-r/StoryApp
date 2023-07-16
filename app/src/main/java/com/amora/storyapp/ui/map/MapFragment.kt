package com.amora.storyapp.ui.map

import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amora.storyapp.R
import com.amora.storyapp.data.remote.model.StoryItem
import com.amora.storyapp.databinding.FragmentMapBinding
import com.amora.storyapp.ui.base.BaseFragment
import com.amora.storyapp.utils.State
import com.amora.storyapp.utils.toast
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ktx.awaitMap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding, MapViewModel>() {
	override val inflateBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMapBinding
		get() = FragmentMapBinding::inflate
	override val viewModel: MapViewModel by viewModels()
	private lateinit var mMap: GoogleMap
	private lateinit var mapFragment: SupportMapFragment
	private lateinit var clusterManager: ClusterManager<StoryItemClusterItem>
	private lateinit var clusterRenderer: CustomClusterRenderer


	override fun onResume() {
		super.onResume()
		getMap()
	}
	override fun initView() { }

	override fun initObserver() {

		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.mapState.onEach { state ->
					when(state) {
						is State.Loading -> {}

						is State.Error -> {
							val messageApi = state.data.toString()
							val message = state.message
							if (message != null) {
								toast(requireContext(), message)
							} else {
								toast(requireContext(), messageApi)
							}
						}

						is State.Success -> {
							addLocationBounds(state.data?.listStory ?: emptyList())
						}
						else -> {}
					}
				}.collect()
			}
		}
	}

	private fun addLocationBounds(list: List<StoryItem>) {
		clusterManager = ClusterManager<StoryItemClusterItem>(requireContext(), mMap)
		clusterRenderer = CustomClusterRenderer(requireContext(), mMap, clusterManager)

		mMap.setOnCameraIdleListener {
			clusterManager.onCameraIdle()
		}
		val clusterItems = createClusterItems(list)
		clusterManager.addItems(clusterItems)
		clusterManager.setOnClusterItemClickListener { clusterItem ->
			val clickedMarker = clusterManager.markerCollection.markers.find { it.snippet == clusterItem.snippetUser }
			clickedMarker?.showInfoWindow()
			true // Return true to consume the event
		}
		val latLngBounds = calculateLatLngBounds(clusterItems)
		clusterManager.renderer = clusterRenderer
		val cameraUpdates = createCameraUpdates(latLngBounds)
		mMap.moveCamera(cameraUpdates)
	}

	private fun createClusterItems(list: List<StoryItem>): List<StoryItemClusterItem> {
		return list.map { storyItem ->
			val latLng = LatLng(storyItem.lat!!, storyItem.lon!!)
			StoryItemClusterItem(latLng, storyItem.name.toString(), storyItem.description.toString(), storyItem.id)
		}
	}

	private fun calculateLatLngBounds(markers: Collection<StoryItemClusterItem>): LatLngBounds {
		val boundsBuilder = LatLngBounds.builder()
		for (marker in markers) {
			boundsBuilder.include(marker.position)
		}
		return boundsBuilder.build()
	}

	private fun createCameraUpdates(latLngBounds: LatLngBounds): CameraUpdate {
		val padding = 300
		return CameraUpdateFactory.newLatLngBounds(
			latLngBounds,
			padding
		)
	}

	private fun getMap() {
		viewLifecycleOwner.lifecycleScope.launch {
			try {
				mapFragment = childFragmentManager.findFragmentById(R.id.customMap) as SupportMapFragment
				mMap = mapFragment.awaitMap()

				mMap.apply {
					uiSettings.apply {
						isMyLocationButtonEnabled = false
						isZoomControlsEnabled = true
						isMapToolbarEnabled = true
					}
				}

			} catch (e: Exception) {
				Timber.e(e)
			}
		}
	}

	private inner class CustomClusterRenderer(
		context: Context,
		map: GoogleMap,
		clusterManager: ClusterManager<StoryItemClusterItem>
	) : DefaultClusterRenderer<StoryItemClusterItem>(context, map, clusterManager) {

		override fun onClusterItemRendered(clusterItem: StoryItemClusterItem, marker: Marker) {
			super.onClusterItemRendered(clusterItem, marker)

			// Set the marker's title, snippet, and other properties
			marker.title = clusterItem.titleUser
			marker.snippet = clusterItem.snippetUser
			marker.tag = clusterItem.idUser
		}
	}
}