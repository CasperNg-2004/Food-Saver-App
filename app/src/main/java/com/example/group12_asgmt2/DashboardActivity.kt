package com.example.group12_asgmt2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
        loadMockDashboardData()
        setupRecipeList()
        setupClickListeners()
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

    private fun loadMockDashboardData() {
        val summary = DashboardSummary(
            kitchenName = "Alex's Kitchen",
            totalItems = 42,
            freshCount = 28,
            nearExpiryCount = 10,
            expiredCount = 4,
            usageRate = 85,
            discardRate = 15,
            wastedCost = 12.50,
            weeklySaved = 2.00
        )

        tvKitchenName.text = summary.kitchenName
        tvTotalItems.text = summary.totalItems.toString()
        tvFreshCount.text = summary.freshCount.toString()
        tvNearExpiryCount.text = summary.nearExpiryCount.toString()
        tvExpiredCount.text = summary.expiredCount.toString()
        tvUsageRate.text = "${summary.usageRate}%"
        tvDiscardRate.text = "${summary.discardRate}% Discarded"
        tvWastedCost.text = "$${String.format("%.2f", summary.wastedCost)}"
        tvWeeklySaved.text = "-$${String.format("%.2f", summary.weeklySaved)} vs last week"
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
            Toast.makeText(this, "Go to Add Item page", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AddEditIngredientActivity::class.java))
        }

        btnSeeAllInventory.setOnClickListener {
            Toast.makeText(this, "View all inventory", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        btnSeeAllRecipes.setOnClickListener {
            Toast.makeText(this, "View all recipes", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, RecipeActivity::class.java))
        }

        navHome.setOnClickListener {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }

        navPantry.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        navScan.setOnClickListener {
            Toast.makeText(this, "Scan", Toast.LENGTH_SHORT).show()
        }

        navStats.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        navRecipes.setOnClickListener {
            startActivity(Intent(this, RecipeActivity::class.java))
        }
    }
}
