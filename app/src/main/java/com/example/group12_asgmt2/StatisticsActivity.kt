package com.example.group12_asgmt2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class StatisticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.statsFragmentContainer, FragmentStatistics())
                .commit()
        }
    }
}
