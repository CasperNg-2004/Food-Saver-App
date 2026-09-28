package com.example.group12_asgmt2

import android.app.Activity
import android.content.Intent
import androidx.core.app.ActivityOptionsCompat

object AppNavigator {
    @JvmStatic
    fun open(
        activity: Activity,
        destination: Class<out Activity>,
        finishCurrent: Boolean
    ) {
        if (activity.javaClass == destination) return

        val intent = Intent(activity, destination).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val options = ActivityOptionsCompat.makeCustomAnimation(activity, 0, 0)
        activity.startActivity(intent, options.toBundle())
        if (finishCurrent) activity.finish()
    }
}
