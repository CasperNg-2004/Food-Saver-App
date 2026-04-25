package com.example.group12_asgmt2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class RecipeAdapter(
    private val items: List<RecipeItem>,
    private val onClick: (RecipeItem) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivRecipe: ImageView = view.findViewById(R.id.ivRecipe)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvExpiryTag: TextView = view.findViewById(R.id.tvExpiryTag)
        val tvIngredientTag: TextView = view.findViewById(R.id.tvIngredientTag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = items[position]

        // Load image with Glide and rounded corners
        Glide.with(holder.itemView.context)
            .load(item.imageResId)
            .transform(CenterCrop(), RoundedCorners(16))
            .into(holder.ivRecipe)

        holder.tvTime.text = item.time
        holder.tvTitle.text = item.title
        holder.tvExpiryTag.text = item.expiryTag
        holder.tvIngredientTag.text = item.ingredientTag

        holder.itemView.setOnClickListener {
            try {
                onClick(item)
            } catch (e: Exception) {
                android.widget.Toast.makeText(holder.itemView.context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int = items.size
}