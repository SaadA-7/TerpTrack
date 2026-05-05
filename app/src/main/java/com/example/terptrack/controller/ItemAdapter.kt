package com.umd.terptrack.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.terptrack.R
import com.umd.terptrack.model.LostItem
import com.bumptech.glide.Glide

class ItemAdapter(
    private var items: List<LostItem>,
    private val onItemClick: (LostItem) -> Unit // Handles clicks to pass data to detail view
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    // connects the layout variables to the XML IDs
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textDescription: TextView = view.findViewById(R.id.textDescription)
        val textBuilding: TextView = view.findViewById(R.id.textBuilding)
        val ratingCondition: RatingBar = view.findViewById(R.id.ratingItemCondition)
        val imageThumbnail: ImageView = view.findViewById(R.id.imageItemThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lost, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.textDescription.text = item.description
        holder.textBuilding.text = item.buildingName
        holder.ratingCondition.rating = item.conditionRating

        // PART 3: Load Firebase image URL using Glide
        if (item.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(holder.imageThumbnail)
        } else {
            holder.imageThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size

    // Function to update the list when Firebase data changes
    fun updateData(newItems: List<LostItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}