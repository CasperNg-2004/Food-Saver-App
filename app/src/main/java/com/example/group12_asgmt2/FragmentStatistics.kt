package com.example.group12_asgmt2

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class FragmentStatistics : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var tvEfficiencyScore: TextView
    private lateinit var tvWasteCost: TextView
    private lateinit var tvConsumedCost: TextView
    private lateinit var progressEfficiency: ProgressBar

    private lateinit var cardTabDay: MaterialCardView
    private lateinit var tabDay: TextView
    private lateinit var cardTabWeek: MaterialCardView
    private lateinit var tabWeek: TextView
    private lateinit var cardTabMonth: MaterialCardView
    private lateinit var tabMonth: TextView

    // Side panel views
    private lateinit var sidePanel: LinearLayout
    private lateinit var tvSidePanelTitle: TextView
    private lateinit var listContainer: LinearLayout

    // Insights views
    private lateinit var tvInsight1: TextView
    private lateinit var tvInsight2: TextView
    private lateinit var tvInsight3: TextView

    private var masterData: List<Ingredient> = emptyList()
    private var currentConsumed: List<Ingredient> = emptyList()
    private var currentWasted: List<Ingredient> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)

        pieChart = view.findViewById(R.id.pieChartHabits)
        tvEfficiencyScore = view.findViewById(R.id.tvEfficiencyScore)
        tvWasteCost = view.findViewById(R.id.tvWasteCost)
        tvConsumedCost = view.findViewById(R.id.tvConsumedCost)
        progressEfficiency = view.findViewById(R.id.progressEfficiency)

        cardTabDay = view.findViewById(R.id.cardTabDay)
        tabDay = view.findViewById(R.id.tabDay)
        cardTabWeek = view.findViewById(R.id.cardTabWeek)
        tabWeek = view.findViewById(R.id.tabWeek)
        cardTabMonth = view.findViewById(R.id.cardTabMonth)
        tabMonth = view.findViewById(R.id.tabMonth)

        sidePanel = view.findViewById(R.id.sidePanel)
        tvSidePanelTitle = view.findViewById(R.id.tvSidePanelTitle)
        listContainer = view.findViewById(R.id.listContainer)

        tvInsight1 = view.findViewById(R.id.tvInsight1)
        tvInsight2 = view.findViewById(R.id.tvInsight2)
        tvInsight3 = view.findViewById(R.id.tvInsight3)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            requireActivity().finish()
        }

        view.findViewById<ImageView>(R.id.infoEfficiency).setOnClickListener {
            showInfoDialog("Zero-Waste Score", "The percentage of items consumed over total items consumed and wasted.")
        }
        
        view.findViewById<ImageView>(R.id.infoConsumed)?.setOnClickListener {
            showInfoDialog("Consumed Cost", "The total financial value from items you successfully consumed during the selected period.")
        }
        
        view.findViewById<ImageView>(R.id.infoWaste)?.setOnClickListener {
            showInfoDialog("Waste Cost", "The total financial loss from items marked as wasted during the selected period.")
        }

        view.findViewById<View>(R.id.cardConsumedCost).setOnClickListener {
            openSidePanel("Consumed Items", currentConsumed)
        }

        view.findViewById<View>(R.id.cardWasteCost).setOnClickListener {
            openSidePanel("Wasted Items", currentWasted)
        }

        view.findViewById<View>(R.id.btnSidePanelBack).setOnClickListener {
            closeSidePanel()
        }

        setupPieChart()
        setupTabs()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Hide panel initially
        sidePanel.post {
            sidePanel.translationX = sidePanel.width.toFloat()
        }
        
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                masterData = IngredientRepository.getHistoricalData()
                
                withContext(Dispatchers.Main) {
                    // Weekly Insights
                    val insights = generateWeeklyInsights(masterData)
                    if (insights.isNotEmpty()) {
                        tvInsight1.text = insights.random()
                    }
                    tvInsight2.visibility = View.GONE
                    tvInsight3.visibility = View.GONE

                    // Default to 'Day'
                    selectTab("Day")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvEfficiencyScore.text = "0%"
                    tvWasteCost.text = "$0.00"
                    tvConsumedCost.text = "$0.00"
                }
            }
        }
    }

    private fun generateWeeklyInsights(historicalData: List<Ingredient>): List<String> {
        val now = LocalDate.now()
        val currentWeekStart = now.minusDays(7)
        val lastWeekStart = now.minusDays(14)

        val currentWeek = mutableListOf<Ingredient>()
        val lastWeek = mutableListOf<Ingredient>()

        for (item in historicalData) {
            val dateStr = item.resolvedDate ?: item.createdAt?.take(10) ?: ""
            if (dateStr.isNotEmpty()) {
                try {
                    val resolvedDate = LocalDate.parse(dateStr)
                    if (!resolvedDate.isBefore(currentWeekStart)) {
                        currentWeek.add(item)
                    } else if (resolvedDate.isBefore(currentWeekStart) && !resolvedDate.isBefore(lastWeekStart)) {
                        lastWeek.add(item)
                    }
                } catch (e: Exception) {
                    // Ignore parse errors
                }
            }
        }

        val currentWastedCost = currentWeek.filter { it.status == "WASTED" }.sumOf { it.price }
        val lastWastedCost = lastWeek.filter { it.status == "WASTED" }.sumOf { it.price }
        val wasteDiff = currentWastedCost - lastWastedCost

        val insight1 = if (wasteDiff <= 0) {
            String.format("Awesome! Your waste cost is down $%.2f compared to last week. Great kitchen management!", Math.abs(wasteDiff))
        } else {
            String.format("Heads up! Your waste cost increased by $%.2f this week compared to last. Be more aware next time!", wasteDiff)
        }

        val currentConsumedCost = currentWeek.filter { it.status == "CONSUMED" }.sumOf { it.price }
        val lastConsumedCost = lastWeek.filter { it.status == "CONSUMED" }.sumOf { it.price }
        val spendDiff = currentConsumedCost - lastConsumedCost

        val insight2 = if (spendDiff <= 0) {
            String.format("You spent $%.2f less on food consumed this week than last week. Nice savings!", Math.abs(spendDiff))
        } else {
            String.format("You spent $%.2f more on food consumed this week. Been cooking more?", spendDiff)
        }

        val consumedCounts = currentWeek.filter { it.status == "CONSUMED" }.groupBy { it.name }
            .mapValues { (_, items) -> items.sumOf { it.quantity } }
        val favorite = consumedCounts.maxByOrNull { it.value }

        val insight3 = if (favorite != null && favorite.value > 0) {
            String.format("Your favorite this week was %s! You successfully finished %d of them.", favorite.key, favorite.value.toInt())
        } else {
            "No items consumed this week yet to determine a favorite."
        }

        return listOf(insight1, insight2, insight3)
    }

    private fun showInfoDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun openSidePanel(title: String, items: List<Ingredient>) {
        tvSidePanelTitle.text = title
        listContainer.removeAllViews()
        
        if (items.isEmpty()) {
            val emptyTv = TextView(requireContext()).apply {
                text = "No items found."
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                setPadding(0, 32, 0, 0)
            }
            listContainer.addView(emptyTv)
        } else {
            items.forEach { item ->
                val itemView = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 16, 0, 16)
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                val nameTv = TextView(requireContext()).apply {
                    text = "${item.quantity}x ${item.name}"
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }

                val priceTv = TextView(requireContext()).apply {
                    text = String.format("$%.2f", item.price)
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    textSize = 16f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }

                itemView.addView(nameTv)
                itemView.addView(priceTv)
                
                // Add a divider
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2)
                    setBackgroundColor(ContextCompat.getColor(context, R.color.divider))
                }
                
                listContainer.addView(itemView)
                listContainer.addView(divider)
            }
        }

        sidePanel.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(sidePanel, "translationX", 0f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun closeSidePanel() {
        ObjectAnimator.ofFloat(sidePanel, "translationX", sidePanel.width.toFloat()).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun setupTabs() {
        cardTabDay.setOnClickListener { selectTab("Day") }
        cardTabWeek.setOnClickListener { selectTab("Week") }
        cardTabMonth.setOnClickListener { selectTab("Month") }
    }

    private fun selectTab(timeFrame: String) {
        val colorSurface = ContextCompat.getColor(requireContext(), R.color.surface)
        val colorSurfaceElevated = ContextCompat.getColor(requireContext(), R.color.surface_elevated)
        val colorPrimary = ContextCompat.getColor(requireContext(), R.color.text_primary)
        val colorSecondary = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        // Reset all tabs
        cardTabDay.setCardBackgroundColor(colorSurface)
        tabDay.setTextColor(colorSecondary)
        tabDay.typeface = android.graphics.Typeface.DEFAULT

        cardTabWeek.setCardBackgroundColor(colorSurface)
        tabWeek.setTextColor(colorSecondary)
        tabWeek.typeface = android.graphics.Typeface.DEFAULT

        cardTabMonth.setCardBackgroundColor(colorSurface)
        tabMonth.setTextColor(colorSecondary)
        tabMonth.typeface = android.graphics.Typeface.DEFAULT

        // Highlight selected
        when (timeFrame) {
            "Day" -> {
                cardTabDay.setCardBackgroundColor(colorSurfaceElevated)
                tabDay.setTextColor(colorPrimary)
                tabDay.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            "Week" -> {
                cardTabWeek.setCardBackgroundColor(colorSurfaceElevated)
                tabWeek.setTextColor(colorPrimary)
                tabWeek.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            "Month" -> {
                cardTabMonth.setCardBackgroundColor(colorSurfaceElevated)
                tabMonth.setTextColor(colorPrimary)
                tabMonth.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
        }

        filterAndRenderData(timeFrame)
    }

    private fun filterAndRenderData(timeFrame: String) {
        val now = LocalDate.now()
        val thresholdDate = when (timeFrame) {
            "Day" -> now
            "Week" -> now.minusDays(6)
            "Month" -> now.minusDays(29)
            else -> now.minusDays(29)
        }

        val filteredData = masterData.filter {
            val dateStr = it.resolvedDate ?: it.createdAt?.take(10) ?: ""
            if (dateStr.isNotEmpty()) {
                try {
                    val resolvedDate = LocalDate.parse(dateStr)
                    !resolvedDate.isBefore(thresholdDate)
                } catch (_: Exception) {
                    false
                }
            } else {
                false
            }
        }

        // Aggregation Logic
        val totalItems = filteredData.size
        currentConsumed = filteredData.filter { it.status == "CONSUMED" }
        currentWasted = filteredData.filter { it.status == "WASTED" }
        
        // Metric 1: Efficiency Score
        val efficiencyScore = if (totalItems > 0) {
            (currentConsumed.size.toDouble() / totalItems * 100).toInt()
        } else {
            0
        }
        
        // Metric 2: Expanded Dietary Habits
        val consumedByCategory = currentConsumed.groupBy { it.category }
        val dietaryHabits = consumedByCategory.mapValues { (_, items) ->
            items.size.toFloat()
        }
        
        // Metric 3: Single Wastage Metric
        val totalWasteCost = currentWasted.sumOf { it.price }
        
        // New Metric: Total Consumed Cost
        val totalConsumedCost = currentConsumed.sumOf { it.price }

        tvEfficiencyScore.text = "$efficiencyScore%"
        tvWasteCost.text = String.format("$%.2f", totalWasteCost)
        tvConsumedCost.text = String.format("$%.2f", totalConsumedCost)
        
        // Animate the progress bar
        ObjectAnimator.ofInt(progressEfficiency, "progress", 0, efficiencyScore).apply {
            duration = 1000
            interpolator = DecelerateInterpolator()
            start()
        }
        
        updatePieChart(dietaryHabits)
    }

    private fun setupPieChart() {
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = true
        pieChart.legend.textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setDrawEntryLabels(true)
        pieChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
    }

    private fun updatePieChart(habitsData: Map<String, Float>) {
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        
        val colorMap = mapOf(
            Category.DAIRY.displayName to ContextCompat.getColor(requireContext(), R.color.category_dairy),
            Category.PROTEIN.displayName to ContextCompat.getColor(requireContext(), R.color.category_protein),
            Category.GRAIN.displayName to ContextCompat.getColor(requireContext(), R.color.category_grain),
            Category.VITAMIN.displayName to ContextCompat.getColor(requireContext(), R.color.category_vitamin),
            Category.VEGETABLE.displayName to ContextCompat.getColor(requireContext(), R.color.category_vegetable),
            Category.FRUIT.displayName to ContextCompat.getColor(requireContext(), R.color.category_fruit),
            Category.SPICE.displayName to ContextCompat.getColor(requireContext(), R.color.category_spice),
            Category.OTHER.displayName to ContextCompat.getColor(requireContext(), R.color.category_other)
        )

        for ((category, count) in habitsData) {
            entries.add(PieEntry(count, category))
            val color = colorMap[category]
                ?: ContextCompat.getColor(requireContext(), R.color.category_other)
            colors.add(color)
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate()
    }
}
