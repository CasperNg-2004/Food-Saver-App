package com.example.group12_asgmt2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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

        holder.ivRecipe.setImageResource(item.imageResId)
        holder.tvTime.text = item.time
        holder.tvTitle.text = item.title
        holder.tvExpiryTag.text = item.expiryTag
        holder.tvIngredientTag.text = item.ingredientTag

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}