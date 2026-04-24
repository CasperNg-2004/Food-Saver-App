package com.example.group12_asgmt2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.group12_asgmt2.databinding.ItemIngredientBinding

class IngredientAdapter(
    private val onItemClick: (Ingredient) -> Unit,
    private val onDeleteClick: (Ingredient) -> Unit
) : ListAdapter<Ingredient, IngredientAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Ingredient>() {
            override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient) =
                oldItem == newItem
        }

        private val CATEGORY_COLORS = mapOf(
            "Protein"   to R.color.category_protein,
            "Vitamin"   to R.color.category_vitamin,
            "Dairy"     to R.color.category_dairy,
            "Grain"     to R.color.category_grain,
            "Vegetable" to R.color.category_vegetable,
            "Fruit"     to R.color.category_fruit,
            "Spice"     to R.color.category_spice,
            "Other"     to R.color.category_other
        )
    }

    inner class ViewHolder(val binding: ItemIngredientBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIngredientBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = getItem(position)
        val ctx = holder.itemView.context

        holder.binding.apply {
            // Basic info
            tvName.text = ingredient.name
            tvCategory.text = ingredient.category
            tvQuantity.text = "${ingredient.quantity} ${ingredient.unit}"
            tvPrice.text = "RM ${String.format("%.2f", ingredient.price)}"
            tvExpiryDate.text = DateUtils.formatForDisplay(ingredient.expiryDate)

            // Photo
            if (!ingredient.photoUrl.isNullOrEmpty()) {
                ivFoodPhoto.visibility = View.VISIBLE
                Glide.with(ctx)
                    .load(ingredient.photoUrl)
                    .centerCrop()
                    .into(ivFoodPhoto)
            } else {
                ivFoodPhoto.visibility = View.GONE
            }

            // Category badge color
            val colorRes = CATEGORY_COLORS[ingredient.category] ?: R.color.category_other
            tvCategory.backgroundTintList =
                ContextCompat.getColorStateList(ctx, colorRes)

            // Expiry status color strip + label
            val days = DateUtils.daysUntilExpiry(ingredient.expiryDate)
            when (DateUtils.getExpiryStatus(ingredient.expiryDate)) {
                DateUtils.ExpiryStatus.EXPIRED -> {
                    expiryStatusStrip.setBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.expiry_expired)
                    )
                    tvExpiryLabel.text = "EXPIRED"
                    tvExpiryLabel.setTextColor(ContextCompat.getColor(ctx, R.color.expiry_expired))
                }
                DateUtils.ExpiryStatus.SOON -> {
                    expiryStatusStrip.setBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.expiry_soon)
                    )
                    tvExpiryLabel.text = "Expires in $days day${if (days == 1L) "" else "s"}"
                    tvExpiryLabel.setTextColor(ContextCompat.getColor(ctx, R.color.expiry_soon))
                }
                DateUtils.ExpiryStatus.FRESH -> {
                    expiryStatusStrip.setBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.expiry_fresh)
                    )
                    tvExpiryLabel.text = "Fresh ($days days left)"
                    tvExpiryLabel.setTextColor(ContextCompat.getColor(ctx, R.color.expiry_fresh))
                }
            }

            // Click listeners
            root.setOnClickListener { onItemClick(ingredient) }
            btnDelete.setOnClickListener { onDeleteClick(ingredient) }
        }
    }
}
