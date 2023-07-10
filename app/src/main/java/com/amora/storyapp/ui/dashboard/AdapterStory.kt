package com.amora.storyapp.ui.dashboard

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amora.storyapp.R
import com.amora.storyapp.data.remote.model.StoryItem
import com.amora.storyapp.databinding.ItemStoryBinding
import com.amora.storyapp.utils.LottieDrawable
import com.amora.storyapp.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class AdapterStory(private val context: Context) :
	RecyclerView.Adapter<AdapterStory.ItemStoryVH>() {

	inner class ItemStoryVH(val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root)

	interface OnItemClickListener {
		fun onItemClick(item: StoryItem)
	}

	private var onItemClickListener: OnItemClickListener? = null

	fun setOnItemClickListener(listener: OnItemClickListener) {
		onItemClickListener = listener
	}

	private val differCallback = object : DiffUtil.ItemCallback<StoryItem>() {
		override fun areItemsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
			return oldItem.id == newItem.id
		}

		override fun areContentsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
			return oldItem == newItem
		}

	}

	val differ = AsyncListDiffer(this, differCallback)
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemStoryVH {
		val binding = ItemStoryBinding.inflate(
			LayoutInflater.from(parent.context),
			parent, false
		)
		return ItemStoryVH(binding)
	}

	override fun getItemCount(): Int {
		return differ.currentList.size
	}

	override fun onBindViewHolder(holder: ItemStoryVH, position: Int) {
		val item = differ.currentList[position]
		val gone = View.GONE
		val visible = View.VISIBLE

		holder.itemView.setOnClickListener {
			onItemClickListener?.onItemClick(item)
		}

		holder.binding.apply {
			tvStoryDescription.text = item.description
			if (item.lat != null && item.lon != null) {
				tvStoryLocation.text = Utils.generateLocation(context, item.lat, item.lon)
				if (tvStoryLocation.text.toString().lowercase().contains("not found", true)) {
					tvStoryLocation.visibility = gone
				} else {
					tvStoryLocation.visibility = visible
				}
			} else {
				tvStoryLocation.visibility = gone
			}
			tvStoryTimeStamp.text = Utils.calculateTimeDifference(item.createdAt.toString())
			tvStoryUser.text = item.name

			val requestOptions = RequestOptions()
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.centerCrop()
				.error(R.drawable.broken_img)
			if (item.photoUrl.isNullOrEmpty()) {
				ivStoryImage.visibility = gone
			} else {
				ivStoryImage.visibility = visible
				Glide.with(context)
					.load(item.photoUrl)
					.apply(requestOptions)
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.apply(RequestOptions.centerCropTransform())
					.into(ivStoryImage)
			}
		}
	}
}
