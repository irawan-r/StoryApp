package com.amora.storyapp.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amora.storyapp.R
import com.amora.storyapp.data.remote.model.StoryItem
import com.amora.storyapp.databinding.BtStoryBinding
import com.amora.storyapp.databinding.ItemStoryBinding
import com.amora.storyapp.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class AdapterStory(private val context: Context) :
	PagingDataAdapter<StoryItem, RecyclerView.ViewHolder>(differCallback) {

	private var headerVisible = false

	inner class ItemStoryVH(val binding: ItemStoryBinding) :
		RecyclerView.ViewHolder(binding.root)

	inner class HeaderVH(val binding: BtStoryBinding) :
		RecyclerView.ViewHolder(binding.root)

	interface OnItemClickListener {
		fun onItemClick(item: StoryItem)
		fun onPostClick()

		fun onMapClick()
	}

	private var onItemClickListener: OnItemClickListener? = null
	private var onPostClickListener: OnItemClickListener? = null
	private var onMapClickListener: OnItemClickListener? = null

	fun setOnItemClickListener(listener: OnItemClickListener) {
		onItemClickListener = listener
	}

	fun setPostItemClickListener(listener: OnItemClickListener) {
		onPostClickListener = listener
	}

	fun setMapItemClickListener(listener: OnItemClickListener) {
		onMapClickListener = listener
	}

	companion object {
		private const val VIEW_TYPE_HEADER = 0
		private const val VIEW_TYPE_ITEM = 1

		val differCallback = object : DiffUtil.ItemCallback<StoryItem>() {
			override fun areItemsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
				return oldItem.id == newItem.id
			}

			override fun areContentsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
				return oldItem == newItem
			}
		}
	}

	override fun getItemViewType(position: Int): Int {
		return if (position == 0) {
			VIEW_TYPE_HEADER
		} else {
			VIEW_TYPE_ITEM
		}
	}

	fun showHeader() {
		headerVisible = true
		notifyItemInserted(0)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return when (viewType) {
			VIEW_TYPE_HEADER -> {
				val binding = BtStoryBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				)
				HeaderVH(binding)
			}
			VIEW_TYPE_ITEM -> {
				val binding = ItemStoryBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				)
				ItemStoryVH(binding)
			}
			else -> throw IllegalArgumentException("Invalid view type: $viewType")
		}
	}


	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val viewType = getItemViewType(position)
		val gone = View.GONE
		val visible = View.VISIBLE
		when (viewType) {
			VIEW_TYPE_HEADER -> {
				val headerHolder = holder as HeaderVH
				headerHolder.binding.btPost.setOnClickListener {
					onPostClickListener?.onPostClick()
				}
				headerHolder.binding.btMap.setOnClickListener {
					onMapClickListener?.onMapClick()
				}
			}
			VIEW_TYPE_ITEM -> {
				val itemHolder = holder as ItemStoryVH
				val item = getItem(position - 1)
				itemHolder.itemView.setOnClickListener {
					item?.let { onItemClickListener?.onItemClick(it) }
				}
				itemHolder.binding.apply {
					item?.let {
						tvStoryDescription.text = it.description
						if (it.lat != null && it.lon != null) {
							tvStoryLocation.text = Utils.generateLocation(context, it.lat, it.lon)
							if (tvStoryLocation.text.toString().lowercase().contains("not found", true)) {
								tvStoryLocation.visibility = gone
							} else {
								tvStoryLocation.visibility = visible
							}
						} else {
							tvStoryLocation.visibility = gone
						}
						tvStoryTimeStamp.text = Utils.calculateTimeDifference(it.createdAt.toString())
						tvStoryUser.text = it.name

						val requestOptions = RequestOptions()
							.diskCacheStrategy(DiskCacheStrategy.ALL)
							.centerCrop()
							.error(R.drawable.broken_img)
						if (it.photoUrl.isNullOrEmpty()) {
							ivStoryImage.visibility = gone
						} else {
							ivStoryImage.visibility = visible
							Glide.with(context)
								.load(it.photoUrl)
								.apply(requestOptions)
								.apply(RequestOptions.centerCropTransform())
								.into(ivStoryImage)
						}
					}
				}
			}

		}
	}
}


