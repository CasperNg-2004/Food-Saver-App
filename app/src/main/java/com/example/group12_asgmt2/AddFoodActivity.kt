package com.example.group12_asgmt2

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddFoodActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnAddPhoto: LinearLayout

    private lateinit var etItemName: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etPurchaseDate: EditText
    private lateinit var etExpiryDate: EditText

    private lateinit var btnMinus: Button
    private lateinit var btnPlus: Button
    private lateinit var tvQuantity: TextView

    private lateinit var switchExpiryAlert: Switch

    private lateinit var tagFresh: TextView
    private lateinit var tagFrozen: TextView
    private lateinit var tagCanned: TextView
    private lateinit var tagOpened: TextView

    private lateinit var btnSaveInventory: Button

    private val purchaseCalendar = Calendar.getInstance()
    private val expiryCalendar = Calendar.getInstance()

    private var quantity = 1
    private var selectedTag = "Fresh"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)

        bindViews()
        setupCategorySpinner()
        setupDatePickers()
        setupQuantityButtons()
        setupQuickTags()
        setupClickListeners()
        updateQuantityUI()
    }

    private fun bindViews() {
        btnBack = findViewById(R.id.btnBack)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)

        etItemName = findViewById(R.id.etItemName)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etPurchaseDate = findViewById(R.id.etPurchaseDate)
        etExpiryDate = findViewById(R.id.etExpiryDate)

        btnMinus = findViewById(R.id.btnMinus)
        btnPlus = findViewById(R.id.btnPlus)
        tvQuantity = findViewById(R.id.tvQuantity)

        switchExpiryAlert = findViewById(R.id.switchExpiryAlert)

        tagFresh = findViewById(R.id.tagFresh)
        tagFrozen = findViewById(R.id.tagFrozen)
        tagCanned = findViewById(R.id.tagCanned)
        tagOpened = findViewById(R.id.tagOpened)

        btnSaveInventory = findViewById(R.id.btnSaveInventory)
    }

    private fun setupCategorySpinner() {
        val categories = listOf(
            "Select a category",
            "Vegetables",
            "Fruits",
            "Dairy",
            "Meat",
            "Seafood",
            "Beverages",
            "Snacks",
            "Leftovers",
            "Frozen Food",
            "Other"
        )

        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun setupDatePickers() {
        etPurchaseDate.setOnClickListener {
            showDatePicker(etPurchaseDate, purchaseCalendar)
        }

        etExpiryDate.setOnClickListener {
            showDatePicker(etExpiryDate, expiryCalendar)
        }
    }

    private fun showDatePicker(editText: EditText, calendar: Calendar) {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val format = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                editText.setText(format.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun setupQuantityButtons() {
        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQuantityUI()
            }
        }

        btnPlus.setOnClickListener {
            quantity++
            updateQuantityUI()
        }
    }

    private fun updateQuantityUI() {
        tvQuantity.text = quantity.toString()
    }

    private fun setupQuickTags() {
        val tags = listOf(tagFresh, tagFrozen, tagCanned, tagOpened)

        fun resetTags() {
            for (tag in tags) {
                tag.setBackgroundResource(R.drawable.bg_tag_unselected)
                tag.setTextColor(getColor(android.R.color.darker_gray))
            }
        }

        tagFresh.setOnClickListener {
            selectedTag = "Fresh"
            resetTags()
            tagFresh.setBackgroundResource(R.drawable.bg_tag_selected)
            tagFresh.setTextColor(getColor(android.R.color.white))
        }

        tagFrozen.setOnClickListener {
            selectedTag = "Frozen"
            resetTags()
            tagFrozen.setBackgroundResource(R.drawable.bg_tag_selected)
            tagFrozen.setTextColor(getColor(android.R.color.white))
        }

        tagCanned.setOnClickListener {
            selectedTag = "Canned"
            resetTags()
            tagCanned.setBackgroundResource(R.drawable.bg_tag_selected)
            tagCanned.setTextColor(getColor(android.R.color.white))
        }

        tagOpened.setOnClickListener {
            selectedTag = "Opened"
            resetTags()
            tagOpened.setBackgroundResource(R.drawable.bg_tag_selected)
            tagOpened.setTextColor(getColor(android.R.color.white))
        }

        tagFresh.performClick()
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnAddPhoto.setOnClickListener {
            Toast.makeText(this, "Photo upload can be added later", Toast.LENGTH_SHORT).show()
        }

        btnSaveInventory.setOnClickListener {
            saveFoodItem()
        }
    }

    private fun saveFoodItem() {
        val itemName = etItemName.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val purchaseDate = etPurchaseDate.text.toString().trim()
        val expiryDate = etExpiryDate.text.toString().trim()
        val expiryAlertOn = switchExpiryAlert.isChecked

        if (itemName.isEmpty()) {
            etItemName.error = "Item name is required"
            etItemName.requestFocus()
            return
        }

        if (category == "Select a category") {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        if (purchaseDate.isEmpty()) {
            etPurchaseDate.error = "Purchase date is required"
            return
        }

        if (expiryDate.isEmpty()) {
            etExpiryDate.error = "Expiry date is required"
            return
        }

        val payload = IngredientPayload(
            name = itemName,
            category = category,
            quantity = quantity.toDouble(),
            unit = "pcs",
            price = 0.0,
            expiryDate = toIsoDate(expiryDate),
            purchaseDate = toIsoDate(purchaseDate),
            alertEnabled = expiryAlertOn,
            quickTag = selectedTag,
            photoUrl = null
        )

        setSavingState(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                SupabaseManager.client
                    .from("ingredients")
                    .insert(payload)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddFoodActivity,
                        "Item saved successfully",
                        Toast.LENGTH_LONG
                    ).show()
                    clearForm()
                    setSavingState(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddFoodActivity,
                        "Failed to save item: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    setSavingState(false)
                }
            }
        }
    }

    private fun setSavingState(isSaving: Boolean) {
        btnSaveInventory.isEnabled = !isSaving
        btnSaveInventory.text = if (isSaving) "Saving..." else "Save to Inventory"
        btnSaveInventory.alpha = if (isSaving) 0.7f else 1f
    }

    private fun toIsoDate(input: String): String {
        val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(input)
            ?: throw IllegalArgumentException("Invalid date format: $input")
        return outputFormat.format(date)
    }

    private fun clearForm() {
        etItemName.text.clear()
        spinnerCategory.setSelection(0)
        etPurchaseDate.text.clear()
        etExpiryDate.text.clear()
        quantity = 1
        updateQuantityUI()
        switchExpiryAlert.isChecked = true
        tagFresh.performClick()
    }
}