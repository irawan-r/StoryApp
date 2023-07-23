package com.amora.storyapp.utils

import androidx.paging.PagingData
import com.amora.storyapp.data.remote.model.StoryItem
import com.amora.storyapp.data.remote.model.StoryRequest

object DataDummy {

	fun getToken() =
		"Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLTBhQVh5WUw4dVdwTlVldHYiLCJpYXQiOjE2ODk0ODA2NDF9.pGcwlVYsQQ-SY6UfxGNcXxEE-NvRk0HyHu3QVbl-Ifk"

	fun generateDataDummyStoriesEntity(): List<StoryItem> {
		val newList = mutableListOf<StoryItem>()

		for (i in 0..9) {
			val story = StoryItem(
				photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1689467155925_rTC6gj8j.jpg",
				createdAt = "2023-07-16T00:25:55.927Z",
				name = "Delvin $i",
				description = "DOGGO $i",
				lat = -6.155268,
				lon = 106.852234,
				id = "story-zeRZX8S8AffcZjpx$i"
			)
			newList.add(story)
		}
		return newList
	}
}