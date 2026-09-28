package com.example.group12_asgmt2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.example.group12_asgmt2.Recipe
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DashboardActivity"
        private const val NOTIFICATION_PREFERENCES = "notification_preferences"
        private const val KEY_PERMISSION_REQUESTED = "permission_requested"
    }

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
    private lateinit var btnSettings: ImageView
    private lateinit var btnQuickAdd: LinearLayout
    private lateinit var btnSeeAllInventory: TextView
    private lateinit var btnSeeAllRecipes: TextView

    private lateinit var navHome: LinearLayout
    private lateinit var navPantry: LinearLayout

    private lateinit var navStats: LinearLayout
    private lateinit var navRecipes: LinearLayout

    private lateinit var recyclerRecipes: RecyclerView

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        ExpiryNotificationManager.setEnabled(this, granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        bindViews()
        updateGreeting()
        loadStaticDashboardFields()
        setupRecipeList()
        setupClickListeners()
        requestNotificationPermissionIfNeeded()
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
        loadDashboardData()
        loadProfile()
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
        btnSettings = findViewById(R.id.btnSettings)
        btnQuickAdd = findViewById(R.id.btnQuickAdd)
        btnSeeAllInventory = findViewById(R.id.btnSeeAllInventory)
        btnSeeAllRecipes = findViewById(R.id.btnSeeAllRecipes)

        navHome = findViewById(R.id.navHome)
        navPantry = findViewById(R.id.navPantry)

        navStats = findViewById(R.id.navStats)
        navRecipes = findViewById(R.id.navRecipes)

        recyclerRecipes = findViewById(R.id.recyclerRecipes)
    }

    private fun loadStaticDashboardFields() {
        // Mock historical data for now
        tvWeeklySaved.text = "$42.50 vs last week"
        tvWeeklySaved.setTextColor(ContextCompat.getColor(this, R.color.expiry_fresh))
    }

    private fun loadProfile() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val profileId = sharedPref.getString("current_profile_id", null)

        lifecycleScope.launch(Dispatchers.IO) {
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
                Log.e(TAG, "Unable to load the local profile", e)
                withContext(Dispatchers.Main) {
                    tvKitchenName.text = "Alex's Kitchen"
                }
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
        val colors = ArrayList<Int>()
        
        // Add each color together with its entry so colors do not shift when a count is zero.
        if (fresh > 0) {
            entries.add(PieEntry(fresh.toFloat(), "Fresh"))
            colors.add(ContextCompat.getColor(this, R.color.expiry_fresh))
        }
        if (near > 0) {
            entries.add(PieEntry(near.toFloat(), "Near Expiry"))
            colors.add(ContextCompat.getColor(this, R.color.expiry_soon))
        }
        if (expired > 0) {
            entries.add(PieEntry(expired.toFloat(), "Expired"))
            colors.add(ContextCompat.getColor(this, R.color.expiry_expired))
        }

        val dataSet = PieDataSet(entries, "Inventory Status")
        
        // Match the dot colors in your layout
        dataSet.colors = colors
        
        dataSet.setDrawValues(false)
        
        val data = PieData(dataSet)
        pieChartStatus.data = data
        pieChartStatus.invalidate()
    }

    private fun loadDashboardData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val ingredients = IngredientRepository.getAll()
                val historicalItems = IngredientRepository.getHistoricalData()

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

                val now = java.time.LocalDate.now()
                val oneWeekAgo = now.minusDays(7)
                val twoWeeksAgo = now.minusDays(14)

                var currentWeekWaste = 0.0
                var lastWeekWaste = 0.0

                historicalItems.filter { it.status == "WASTED" }.forEach {
                    val dateString = it.resolvedDate ?: return@forEach
                    try {
                        val resolvedDate = java.time.LocalDate.parse(dateString)
                        if (!resolvedDate.isBefore(oneWeekAgo)) {
                            currentWeekWaste += it.price
                        } else if (!resolvedDate.isBefore(twoWeeksAgo)) {
                            lastWeekWaste += it.price
                        }
                    } catch (_: Exception) {
                        // Ignore malformed historical dates instead of assigning them to this week.
                    }
                }

                val resolvedCount = historicalItems.count {
                    it.status == "CONSUMED" || it.status == "WASTED"
                }
                val consumedCount = historicalItems.count { it.status == "CONSUMED" }
                val usagePercent = if (resolvedCount > 0) {
                    (consumedCount * 100) / resolvedCount
                } else 0

                val discardPercent = if (resolvedCount > 0) 100 - usagePercent else 0

                withContext(Dispatchers.Main) {
                    tvTotalItems.text = totalItems.toString()
                    tvFreshCount.text = freshCount.toString()
                    tvNearExpiryCount.text = nearExpiryCount.toString()
                    tvExpiredCount.text = expiredCount.toString()
                    
                    tvUsageRate.text = "$usagePercent%"
                    
                    android.animation.ObjectAnimator.ofInt(progressUsageRate, "progress", 0, usagePercent).apply {
                        duration = 1000 // 1 second animation
                        start()
                    }
                    
                    tvDiscardRate.text = "$discardPercent% Discarded"
                    tvWastedCost.text = String.format("$%.2f", currentWeekWaste)
                    
                    val difference = currentWeekWaste - lastWeekWaste
                    if (difference <= 0) {
                        tvWeeklySaved.text = String.format("-$%.2f less waste vs last week", kotlin.math.abs(difference))
                        tvWeeklySaved.setTextColor(ContextCompat.getColor(this@DashboardActivity, R.color.expiry_fresh))
                    } else {
                        tvWeeklySaved.text = String.format("+$%.2f more waste vs last week", difference)
                        tvWeeklySaved.setTextColor(ContextCompat.getColor(this@DashboardActivity, R.color.expiry_expired))
                    }
                    
                    updatePieChart(freshCount, nearExpiryCount, expiredCount)
                }

                val pantryNames = ingredients
                    .filter { DateUtils.getExpiryStatus(it.expiryDate) != DateUtils.ExpiryStatus.EXPIRED }
                    .map { it.name }
                val recipes = LocalRecipeRepository.findSuggestions(pantryNames, 6)
                withContext(Dispatchers.Main) {
                    setupRecipeRecyclerView(recipes)
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

    private fun setupRecipeRecyclerView(recipes: List<Recipe>) {
        recyclerRecipes.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        recyclerRecipes.adapter = RecipeAdapter(recipes) { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE, recipe as java.io.Serializable)
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

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnQuickAdd.setOnClickListener {
            startActivity(Intent(this, AddEditIngredientActivity::class.java))
        }

        btnSeeAllInventory.setOnClickListener {
            AppNavigator.open(this, InventoryActivity::class.java, false)
        }

        btnSeeAllRecipes.setOnClickListener {
            AppNavigator.open(this, RecipeActivity::class.java, false)
        }

        tvKitchenName.setOnClickListener {
            showEditNameDialog()
        }

        navHome.setOnClickListener {
            // already on dashboard
        }

        navPantry.setOnClickListener {
            AppNavigator.open(this, InventoryActivity::class.java, false)
        }



        navStats.setOnClickListener {
            AppNavigator.open(this, StatisticsActivity::class.java, false)
        }

        navRecipes.setOnClickListener {
            AppNavigator.open(this, RecipeActivity::class.java, false)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (!ExpiryNotificationManager.isEnabled(this)) return

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            ExpiryNotificationManager.runNow(this)
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            ExpiryNotificationManager.runNow(this)
            return
        }

        val preferences = getSharedPreferences(
            NOTIFICATION_PREFERENCES,
            android.content.Context.MODE_PRIVATE
        )
        if (!preferences.getBoolean(KEY_PERMISSION_REQUESTED, false)) {
            preferences.edit().putBoolean(KEY_PERMISSION_REQUESTED, true).apply()
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
                    updateProfile(profileId, newName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProfile(profileId: String?, newName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
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
                    Toast.makeText(this@DashboardActivity, "Kitchen name saved", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Could not save name: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showExpiryNotifications() {
        lifecycleScope.launch(Dispatchers.IO) {
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
