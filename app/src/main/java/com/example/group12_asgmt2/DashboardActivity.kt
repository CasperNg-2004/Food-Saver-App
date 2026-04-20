package com.example.group12_asgmt2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.example.group12_asgmt2.Recipe
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvKitchenName: TextView
    private lateinit var tvTotalItems: TextView
    private lateinit var tvFreshCount: TextView
    private lateinit var tvNearExpiryCount: TextView
    private lateinit var tvExpiredCount: TextView
    private lateinit var tvUsageRate: TextView
    private lateinit var tvDiscardRate: TextView
    private lateinit var tvWastedCost: TextView
    private lateinit var tvWeeklySaved: TextView

    private lateinit var progressUsageRate: ProgressBar
    private lateinit var pieChartStatus: PieChart

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
        updateGreeting()
        loadStaticDashboardFields()
        setupRecipeList()
        setupClickListeners()
    }

    private fun updateGreeting() {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 0..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
        tvGreeting.text = greeting
    }

    override fun onResume() {
        super.onResume()
        loadDashboardDataFromSupabase()
        loadProfileFromSupabase()
    }

    private fun bindViews() {
        tvGreeting = findViewById(R.id.tvGreeting)
        tvKitchenName = findViewById(R.id.tvKitchenName)
        tvTotalItems = findViewById(R.id.tvTotalItems)
        tvFreshCount = findViewById(R.id.tvFreshCount)
        tvNearExpiryCount = findViewById(R.id.tvNearExpiryCount)
        tvExpiredCount = findViewById(R.id.tvExpiredCount)
        tvUsageRate = findViewById(R.id.tvUsageRate)
        tvDiscardRate = findViewById(R.id.tvDiscardRate)
        tvWastedCost = findViewById(R.id.tvWastedCost)
        tvWeeklySaved = findViewById(R.id.tvWeeklySaved)

        progressUsageRate = findViewById(R.id.progressUsageRate)
        pieChartStatus = findViewById(R.id.pieChartStatus)
        setupPieChart()

        btnNotification = findViewById(R.id.btnNotification)
        btnQuickAdd = findViewById(R.id.btnQuickAdd)
        btnSeeAllInventory = findViewById(R.id.btnSeeAllInventory)
        btnSeeAllRecipes = findViewById(R.id.btnSeeAllRecipes)

        navHome = findViewById(R.id.navHome)
        navPantry = findViewById(R.id.navPantry)
//        navScan = findViewById(R.id.navScan)
        navStats = findViewById(R.id.navStats)
        navRecipes = findViewById(R.id.navRecipes)

        recyclerRecipes = findViewById(R.id.recyclerRecipes)
    }

    private fun loadStaticDashboardFields() {
        // Mock historical data for now
        tvWeeklySaved.text = "$42.50 vs last week"
        tvWeeklySaved.setTextColor(android.graphics.Color.parseColor("#14F25C"))
    }

    private fun loadProfileFromSupabase() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val profileId = sharedPref.getString("current_profile_id", null)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val profile = if (profileId != null) {
                    ProfileRepository.getById(profileId)
                } else {
                    // If no ID stored, just get the first one available as a default
                    ProfileRepository.getAll().firstOrNull()
                }

                withContext(Dispatchers.Main) {
                    if (profile != null) {
                        tvKitchenName.text = "${profile.kitchenName}'s Kitchen"
                        // Save ID if it wasn't there
                        if (profileId == null) {
                            sharedPref.edit().putString("current_profile_id", profile.id).apply()
                        }
                    } else {
                        tvKitchenName.text = "Alex's Kitchen"
                    }
                }
            } catch (e: Exception) {
                // Fallback
            }
        }
    }

    private fun setupPieChart() {
        pieChartStatus.description.isEnabled = false
        pieChartStatus.legend.isEnabled = false
        pieChartStatus.isDrawHoleEnabled = true
        pieChartStatus.setHoleColor(android.graphics.Color.TRANSPARENT)
        pieChartStatus.setDrawEntryLabels(false)
        pieChartStatus.setTouchEnabled(false)
    }

    private fun updatePieChart(fresh: Int, near: Int, expired: Int) {
        val entries = ArrayList<PieEntry>()
        
        // Only add entries if count > 0 to avoid visual glitches
        if (fresh > 0) entries.add(PieEntry(fresh.toFloat(), "Fresh"))
        if (near > 0) entries.add(PieEntry(near.toFloat(), "Near Expiry"))
        if (expired > 0) entries.add(PieEntry(expired.toFloat(), "Expired"))

        val dataSet = PieDataSet(entries, "Inventory Status")
        
        // Match the dot colors in your layout
        dataSet.colors = listOf(
            android.graphics.Color.parseColor("#14F25C"), // Fresh (Green)
            android.graphics.Color.parseColor("#FFD700"), // Near Expiry (Yellow/Gold)
            android.graphics.Color.parseColor("#FF4B4B")  // Expired (Red)
        )
        
        dataSet.setDrawValues(false)
        
        val data = PieData(dataSet)
        pieChartStatus.data = data
        pieChartStatus.invalidate()
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

                val wastedCost = ingredients.filter {
                    DateUtils.getExpiryStatus(it.expiryDate) == DateUtils.ExpiryStatus.EXPIRED
                }.sumOf { it.price }

                val usagePercent = if (totalItems > 0) {
                    ((totalItems - expiredCount) * 100) / totalItems
                } else 0

                val discardPercent = if (totalItems > 0) 100 - usagePercent else 0

                withContext(Dispatchers.Main) {
                    tvTotalItems.text = totalItems.toString()
                    tvFreshCount.text = freshCount.toString()
                    tvNearExpiryCount.text = nearExpiryCount.toString()
                    tvExpiredCount.text = expiredCount.toString()
                    
                    tvUsageRate.text = "$usagePercent%"
                    progressUsageRate.progress = usagePercent
                    tvDiscardRate.text = "$discardPercent% Discarded"
                    tvWastedCost.text = String.format("$%.2f", wastedCost)
                    
                    updatePieChart(freshCount, nearExpiryCount, expiredCount)
                }

                // FETCH REAL RECIPES BASED ON INGREDIENTS
                if (ingredients.isNotEmpty()) {
                    val ingredientsListString = ingredients
                        .filter { DateUtils.getExpiryStatus(it.expiryDate) != DateUtils.ExpiryStatus.EXPIRED }
                        .take(5)
                        .joinToString(",") { it.name }
                    
                    fetchRecipesFromApi(ingredientsListString)
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

    private fun fetchRecipesFromApi(ingredients: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(SpoonacularApi::class.java)
        val apiKey = getString(R.string.api_key)

        apiService.getRecipesByIngredients(ingredients, 10, apiKey).enqueue(object : retrofit2.Callback<List<Recipe>> {
            override fun onResponse(call: retrofit2.Call<List<Recipe>>, response: retrofit2.Response<List<Recipe>>) {
                if (response.isSuccessful && response.body() != null) {
                    val recipes = response.body()!!
                    setupRecipeRecyclerView(recipes)
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Recipe>>, t: Throwable) {
                // Silently fail or log
            }
        })
    }

    private fun setupRecipeRecyclerView(recipes: List<Recipe>) {
        recyclerRecipes.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        recyclerRecipes.adapter = RecipeAdapter(recipes) { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_DATA", recipe)
            startActivity(intent)
        }
    }

    private fun setupRecipeList() {
        // Initial empty state or loading state if needed
        recyclerRecipes.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupClickListeners() {
        btnNotification.setOnClickListener {
            showExpiryNotifications()
        }

        btnQuickAdd.setOnClickListener {
            startActivity(Intent(this, AddFoodActivity::class.java))
        }

        btnSeeAllInventory.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        btnSeeAllRecipes.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        tvKitchenName.setOnClickListener {
            showEditNameDialog()
        }

        navHome.setOnClickListener {
            // already on dashboard
        }

        navPantry.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        /*
        navScan.setOnClickListener {
            Toast.makeText(this, "Scan feature can be added later", Toast.LENGTH_SHORT).show()
        }
        */

        navStats.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        navRecipes.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun showEditNameDialog() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val profileId = sharedPref.getString("current_profile_id", null)
        val currentTitle = tvKitchenName.text.toString().removeSuffix("'s Kitchen")

        val editText = android.widget.EditText(this)
        editText.setText(currentTitle)
        editText.setSelection(editText.text.length)

        val padding = (16 * resources.displayMetrics.density).toInt()
        val container = android.widget.FrameLayout(this)
        container.addView(editText)
        editText.setPadding(padding, padding, padding, padding)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Edit Kitchen Name")
            .setMessage("Enter new kitchen name:")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    updateProfileInSupabase(profileId, newName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProfileInSupabase(profileId: String?, newName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (profileId != null) {
                    // Update existing
                    ProfileRepository.update(profileId, ProfilePayload(username = "User", kitchenName = newName))
                } else {
                    // Create new if none exists
                    val newProfile = ProfileRepository.insert(ProfilePayload(username = "User", kitchenName = newName))
                    getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
                        .edit().putString("current_profile_id", newProfile.id).apply()
                }

                withContext(Dispatchers.Main) {
                    tvKitchenName.text = "$newName's Kitchen"
                    Toast.makeText(this@DashboardActivity, "Name updated in database", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Error saving to DB: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showExpiryNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ingredients = IngredientRepository.getAll()
                val criticalIngredients = ingredients.filter {
                    val status = DateUtils.getExpiryStatus(it.expiryDate)
                    status == DateUtils.ExpiryStatus.EXPIRED || status == DateUtils.ExpiryStatus.SOON
                }

                withContext(Dispatchers.Main) {
                    if (criticalIngredients.isEmpty()) {
                        Toast.makeText(this@DashboardActivity, "All items are fresh!", Toast.LENGTH_SHORT).show()
                    } else {
                        val message = StringBuilder()
                        criticalIngredients.forEach {
                            val status = DateUtils.getExpiryStatus(it.expiryDate)
                            val statusStr = if (status == DateUtils.ExpiryStatus.EXPIRED) "Expired" else "Nearly Expired"
                            message.append("• ${it.name}: $statusStr\n")
                        }

                        androidx.appcompat.app.AlertDialog.Builder(this@DashboardActivity)
                            .setTitle("Expiry Notifications")
                            .setMessage(message.toString().trim())
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}