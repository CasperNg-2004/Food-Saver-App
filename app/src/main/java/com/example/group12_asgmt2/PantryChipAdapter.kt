package com.example.group12_asgmt2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class PantryChipAdapter(private var ingredients: List<Ingredient> = emptyList()) :
    RecyclerView.Adapter<PantryChipAdapter.ViewHolder>() {

    fun setIngredients(newIngredients: List<Ingredient>) {
        ingredients = newIngredients
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_ingredient_name)
        val tvExpiry: TextView = view.findViewById(R.id.tv_expiry_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pantry_chip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position]
        val context = holder.itemView.context

        holder.tvName.text = ingredient.name

        val days = DateUtils.daysUntilExpiry(ingredient.expiryDate)
        when (DateUtils.getExpiryStatus(ingredient.expiryDate)) {
            DateUtils.ExpiryStatus.EXPIRED -> {
                holder.tvExpiry.text = "EXPIRED"
                holder.tvExpiry.setTextColor(ContextCompat.getColor(context, R.color.expiry_expired))
            }
            DateUtils.ExpiryStatus.SOON -> {
                holder.tvExpiry.text = "Expires in $days day${if (days == 1L) "" else "s"}"
                holder.tvExpiry.setTextColor(ContextCompat.getColor(context, R.color.expiry_soon))
            }
            DateUtils.ExpiryStatus.FRESH -> {
                holder.tvExpiry.text = "Fresh ($days days left)"
                holder.tvExpiry.setTextColor(ContextCompat.getColor(context, R.color.expiry_fresh))
            }
        }
    }

    override fun getItemCount() = ingredients.size
}
