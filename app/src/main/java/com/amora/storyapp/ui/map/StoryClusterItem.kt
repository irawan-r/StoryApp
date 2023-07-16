package com.amora.storyapp.ui.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class StoryItemClusterItem(
	val positionUser: LatLng,
	val titleUser: String,
	val snippetUser: String,
	val idUser: String
) : ClusterItem {

	override fun getPosition(): LatLng = positionUser
	override fun getTitle(): String = titleUser
	override fun getSnippet(): String = snippetUser
}