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
            val intent = android.content.Intent(this, DashboardActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
        }

        findViewById<android.view.View>(R.id.navPantry).setOnClickListener {
            val intent = android.content.Intent(this, InventoryActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
        }

        findViewById<android.view.View>(R.id.navStats).setOnClickListener {
            // Already here
        }

        findViewById<android.view.View>(R.id.navRecipes).setOnClickListener {
            val intent = android.content.Intent(this, RecipeActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
        }
    }
}
