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
    private val items: List<Recipe>,
    private val onClick: (Recipe) -> Unit
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

        // Load image URL from API with Glide and rounded corners
        Glide.with(holder.itemView.context)
            .load(item.imageResourceId.takeIf { it != 0 } ?: item.image)
            .transform(CenterCrop(), RoundedCorners(16))
            .into(holder.ivRecipe)

        // Build comma-separated list of pantry ingredients used
        val usedNames = item.usedIngredients
            ?.mapNotNull { it?.name?.takeIf { n -> n.isNotBlank() } }
            ?.joinToString(", ")
            ?.takeIf { it.isNotEmpty() }
            ?: "No pantry match"

        holder.tvTime.text = "${item.readyInMinutes}m"
        holder.tvTitle.text = item.title
        holder.tvExpiryTag.text = "Uses your: $usedNames"
        holder.tvIngredientTag.text = "Uses your: $usedNames"

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
