package com.example.group12_asgmt2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class StatisticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        // Hide the top action bar so users use the bottom nav bar
        supportActionBar?.hide()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.statsFragmentContainer, FragmentStatistics())
                .commit()
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        findViewById<android.view.View>(R.id.navHome).setOnClickListener {
            AppNavigator.open(this, DashboardActivity::class.java, true)
        }

        findViewById<android.view.View>(R.id.navPantry).setOnClickListener {
            AppNavigator.open(this, InventoryActivity::class.java, true)
        }

        findViewById<android.view.View>(R.id.navStats).setOnClickListener {
            // Already here
        }

        findViewById<android.view.View>(R.id.navRecipes).setOnClickListener {
            AppNavigator.open(this, RecipeActivity::class.java, true)
        }
    }
}
