package com.group12.ingredientmanager.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.group12.ingredientmanager.R
import com.group12.ingredientmanager.data.Category
import com.group12.ingredientmanager.data.Ingredient
import com.group12.ingredientmanager.data.IngredientRepository
import com.group12.ingredientmanager.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: IngredientAdapter
    private var allIngredients: List<Ingredient> = emptyList()
    private var selectedCategory: String? = null

    companion object {
        const val REQUEST_CODE_ADD_EDIT = 1001

        // Extra keys for passing ingredient data to AddEditActivity
        const val EXTRA_ID       = "ingredient_id"
        const val EXTRA_NAME     = "ingredient_name"
        const val EXTRA_CATEGORY = "ingredient_category"
        const val EXTRA_QUANTITY = "ingredient_quantity"
        const val EXTRA_UNIT     = "ingredient_unit"
        const val EXTRA_PRICE    = "ingredient_price"
        const val EXTRA_EXPIRY   = "ingredient_expiry"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        setupCategoryChips()
        setupSwipeRefresh()

        binding.fabAdd.setOnClickListener {
            startActivityForResult(
                Intent(this, AddEditIngredientActivity::class.java),
                REQUEST_CODE_ADD_EDIT
            )
        }

        loadIngredients()
    }

    private fun setupRecyclerView() {
        adapter = IngredientAdapter(
            onItemClick = { ingredient ->
                // Pass all fields as plain Intent extras — no serialization needed
                val intent = Intent(this, AddEditIngredientActivity::class.java).apply {
                    putExtra(EXTRA_ID,       ingredient.id)
                    putExtra(EXTRA_NAME,     ingredient.name)
                    putExtra(EXTRA_CATEGORY, ingredient.category)
                    putExtra(EXTRA_QUANTITY, ingredient.quantity)
                    putExtra(EXTRA_UNIT,     ingredient.unit)
                    putExtra(EXTRA_PRICE,    ingredient.price)
                    putExtra(EXTRA_EXPIRY,   ingredient.expiryDate)
                }
                startActivityForResult(intent, REQUEST_CODE_ADD_EDIT)
            },
            onDeleteClick = { ingredient ->
                confirmDelete(ingredient)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupCategoryChips() {
        // Set single-selection BEFORE adding chips to avoid listener chaos
        binding.chipGroupCategory.isSingleSelection = true
        binding.chipGroupCategory.isSelectionRequired = true

        fun addChip(label: String, tag: String?) {
            val chip = Chip(this).apply {
                text = label
                isCheckable = true
                id = View.generateViewId()
            }
            binding.chipGroupCategory.addView(chip)
        }

        addChip("All", null)
        Category.entries.forEach { addChip(it.displayName, it.displayName) }

        // Check "All" by default
        (binding.chipGroupCategory.getChildAt(0) as? Chip)?.isChecked = true

        // Single listener on the group — much cleaner
        binding.chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val checkedChip = group.findViewById<Chip>(checkedIds.first())
            selectedCategory = if (checkedChip?.text == "All") null else checkedChip?.text?.toString()
            filterIngredients()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.green_primary)
        binding.swipeRefreshLayout.setOnRefreshListener { loadIngredients() }
    }

    private fun loadIngredients() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                allIngredients = IngredientRepository.getAll()
                filterIngredients()
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to load ingredients: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                showLoading(false)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun filterIngredients() {
        val filtered = if (selectedCategory == null) {
            allIngredients
        } else {
            allIngredients.filter { it.category == selectedCategory }
        }
        adapter.submitList(filtered)
        binding.emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun confirmDelete(ingredient: Ingredient) {
        AlertDialog.Builder(this)
            .setTitle("Delete Ingredient")
            .setMessage("Delete \"${ingredient.name}\"?")
            .setPositiveButton("Delete") { _, _ -> deleteIngredient(ingredient) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteIngredient(ingredient: Ingredient) {
        lifecycleScope.launch {
            try {
                IngredientRepository.delete(ingredient.id!!)
                Toast.makeText(this@MainActivity, "${ingredient.name} deleted", Toast.LENGTH_SHORT).show()
                loadIngredients()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Delete failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_EDIT && resultCode == RESULT_OK) {
            loadIngredients()
        }
    }
}
