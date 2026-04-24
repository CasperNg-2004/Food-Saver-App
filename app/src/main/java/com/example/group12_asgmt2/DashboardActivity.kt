package com.example.group12_asgmt2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvKitchenName: TextView
    private lateinit var tvTotalItems: TextView
    private lateinit var tvFreshCount: TextView
    private lateinit var tvNearExpiryCount: TextView
    private lateinit var tvExpiredCount: TextView
    private lateinit var tvUsageRate: TextView
    private lateinit var tvDiscardRate: TextView
    private lateinit var tvWastedCost: TextView
    private lateinit var tvWeeklySaved: TextView

    private lateinit var btnNotification: ImageView
    private lateinit var btnQuickAdd: LinearLayout
    private lateinit var btnSeeAllInventory: TextView
    private lateinit var btnSeeAllRecipes: TextView

    private lateinit var navHome: LinearLayout
    private lateinit var navPantry: LinearLayout
    private lateinit var navScan: LinearLayout
    private lateinit var navStats: LinearLayout
    private lateinit var navRecipes: LinearLayout

    private lateinit var recyclerRecipes: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        bindViews()
        loadStaticDashboardFields()
        setupRecipeList()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardDataFromSupabase()
    }

    private fun bindViews() {
        tvKitchenName = findViewById(R.id.tvKitchenName)
        tvTotalItems = findViewById(R.id.tvTotalItems)
        tvFreshCount = findViewById(R.id.tvFreshCount)
        tvNearExpiryCount = findViewById(R.id.tvNearExpiryCount)
        tvExpiredCount = findViewById(R.id.tvExpiredCount)
        tvUsageRate = findViewById(R.id.tvUsageRate)
        tvDiscardRate = findViewById(R.id.tvDiscardRate)
        tvWastedCost = findViewById(R.id.tvWastedCost)
        tvWeeklySaved = findViewById(R.id.tvWeeklySaved)

        btnNotification = findViewById(R.id.btnNotification)
        btnQuickAdd = findViewById(R.id.btnQuickAdd)
        btnSeeAllInventory = findViewById(R.id.btnSeeAllInventory)
        btnSeeAllRecipes = findViewById(R.id.btnSeeAllRecipes)

        navHome = findViewById(R.id.navHome)
        navPantry = findViewById(R.id.navPantry)
        navScan = findViewById(R.id.navScan)
        navStats = findViewById(R.id.navStats)
        navRecipes = findViewById(R.id.navRecipes)

        recyclerRecipes = findViewById(R.id.recyclerRecipes)
    }

    /**
     * Fields that are still mock because current DB does not track
     * consumed/discarded/wasted cost data yet.
     */
    private fun loadStaticDashboardFields() {
        tvKitchenName.text = "Alex's Kitchen"
        tvUsageRate.text = "85%"
        tvDiscardRate.text = "15% Discarded"
        tvWastedCost.text = "$12.50"
        tvWeeklySaved.text = "-$2.00 vs last week"
    }

    private fun loadDashboardDataFromSupabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ingredients = IngredientRepository.getAll()

                val totalItems = ingredients.size
                val freshCount = ingredients.count {
                    DateUtils.getExpiryStatus(it.expiryDate) == DateUtils.ExpiryStatus.FRESH
                }
                val nearExpiryCount = ingredients.count {
                    DateUtils.getExpiryStatus(it.expiryDate) == DateUtils.ExpiryStatus.SOON
                }
                val expiredCount = ingredients.count {
                    DateUtils.getExpiryStatus(it.expiryDate) == DateUtils.ExpiryStatus.EXPIRED
                }

                withContext(Dispatchers.Main) {
                    tvTotalItems.text = totalItems.toString()
                    tvFreshCount.text = freshCount.toString()
                    tvNearExpiryCount.text = nearExpiryCount.toString()
                    tvExpiredCount.text = expiredCount.toString()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DashboardActivity,
                        "Failed to load dashboard data: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    // fallback values
                    tvTotalItems.text = "0"
                    tvFreshCount.text = "0"
                    tvNearExpiryCount.text = "0"
                    tvExpiredCount.text = "0"
                }
            }
        }
    }

    private fun setupRecipeList() {
        val mockRecipes = listOf(
            RecipeItem(
                title = "Avocado & Spinach Salad",
                time = "15m",
                expiryTag = "Spinach (1 day left)",
                ingredientTag = "Avocado",
                imageResId = R.drawable.recipe_avocado_salad
            ),
            RecipeItem(
                title = "Spicy Chicken Stir Fry",
                time = "20m",
                expiryTag = "Peppers (2 days left)",
                ingredientTag = "Chicken",
                imageResId = R.drawable.recipe_spicy_chicken
            ),
            RecipeItem(
                title = "Vegetable Soup",
                time = "25m",
                expiryTag = "Carrot (1 day left)",
                ingredientTag = "Potato",
                imageResId = R.drawable.recipe_vegetable_soup
            )
        )

        recyclerRecipes.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        recyclerRecipes.adapter = RecipeAdapter(mockRecipes) { recipe ->
            Toast.makeText(this, "Clicked: ${recipe.title}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        btnNotification.setOnClickListener {
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
        }

        btnQuickAdd.setOnClickListener {
            startActivity(Intent(this, AddEditIngredientActivity::class.java))
        }

        btnSeeAllInventory.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        btnSeeAllRecipes.setOnClickListener {
            Toast.makeText(this, "Recipe list page can be connected later", Toast.LENGTH_SHORT).show()
        }

        navHome.setOnClickListener {
            // already on dashboard
        }

        navPantry.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        navScan.setOnClickListener {
            Toast.makeText(this, "Scan feature can be added later", Toast.LENGTH_SHORT).show()
        }

        navStats.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        navRecipes.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}