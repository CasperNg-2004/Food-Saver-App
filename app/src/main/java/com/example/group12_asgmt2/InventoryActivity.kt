package com.example.group12_asgmt2

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.example.group12_asgmt2.databinding.ActivityInventoryBinding
import kotlinx.coroutines.launch
//Hello World
class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
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
        const val EXTRA_PURCHASE = "ingredient_purchase"
        const val EXTRA_ALERT    = "ingredient_alert"
        const val EXTRA_QUICK_TAG= "ingredient_quick_tag"
        const val EXTRA_PHOTO_URL= "ingredient_photo_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Header Title (optional dynamic setting, but already set in XML)
        // binding.tvGreeting.text = "Inventory"
        // binding.tvKitchenName.text = "My Pantry"

        setupRecyclerView()
        setupCategoryChips()
        setupSwipeRefresh()
        setupBottomNavigation()

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
                    putExtra(EXTRA_PURCHASE, ingredient.purchaseDate)
                    putExtra(EXTRA_ALERT,    ingredient.alertEnabled ?: true)
                    putExtra(EXTRA_QUICK_TAG,ingredient.quickTag)
                    putExtra(EXTRA_PHOTO_URL,ingredient.photoUrl)
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
                    this@InventoryActivity,
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
                Toast.makeText(this@InventoryActivity, "${ingredient.name} deleted", Toast.LENGTH_SHORT).show()
                loadIngredients()
            } catch (e: Exception) {
                Toast.makeText(this@InventoryActivity, "Delete failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun setupBottomNavigation() {
        binding.navHome.setOnClickListener {
            // Dashboard is likely the main entry, so we just finish this to go back or start Dashboard
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        binding.navPantry.setOnClickListener {
            // Already here
        }

        binding.navScan.setOnClickListener {
            Toast.makeText(this, "Scan feature can be added later", Toast.LENGTH_SHORT).show()
        }

        binding.navStats.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
            finish()
        }

        binding.navRecipes.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        
        binding.btnNotification.setOnClickListener {
             Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_EDIT && resultCode == RESULT_OK) {
            loadIngredients()
        }
    }
}
