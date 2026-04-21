package com.umd.terptrack.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.umd.terptrack.R
import com.umd.terptrack.model.LostItem

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

        // Note: loading the actual image URL into the ImageView requires a library like Glide
        // For now, it will just show the gray placeholder box.

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