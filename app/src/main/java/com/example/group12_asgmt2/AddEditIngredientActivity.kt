package com.example.group12_asgmt2

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.group12_asgmt2.databinding.ActivityAddEditIngredientBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditIngredientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditIngredientBinding
    private var editingIngredient: Ingredient? = null

    private val purchaseCalendar = Calendar.getInstance()
    private val expiryCalendar = Calendar.getInstance()

    private var quantity = 1.0
    private var selectedTag = "Fresh"
    private var photoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditIngredientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ingredientId = intent.getStringExtra(InventoryActivity.EXTRA_ID)
        if (ingredientId != null) {
            editingIngredient = Ingredient(
                id       = ingredientId,
                name     = intent.getStringExtra(InventoryActivity.EXTRA_NAME)     ?: "",
                category = intent.getStringExtra(InventoryActivity.EXTRA_CATEGORY) ?: "",
                quantity = intent.getDoubleExtra(InventoryActivity.EXTRA_QUANTITY, 1.0),
                unit     = intent.getStringExtra(InventoryActivity.EXTRA_UNIT)     ?: "",
                price    = intent.getDoubleExtra(InventoryActivity.EXTRA_PRICE, 0.0),
                expiryDate = intent.getStringExtra(InventoryActivity.EXTRA_EXPIRY) ?: "",
                purchaseDate = intent.getStringExtra(InventoryActivity.EXTRA_PURCHASE),
                alertEnabled = intent.getBooleanExtra(InventoryActivity.EXTRA_ALERT, true),
                quickTag = intent.getStringExtra(InventoryActivity.EXTRA_QUICK_TAG),
                photoUrl = intent.getStringExtra(InventoryActivity.EXTRA_PHOTO_URL)
            )
        }

        val isEditing = editingIngredient != null
        binding.tvTitle.text = if (isEditing) "Edit Item" else "Add New Item"

        setupCategorySpinner()
        setupUnitSpinner()
        setupDatePickers()
        setupQuantityButtons()
        setupQuickTags()
        setupClickListeners()

        if (isEditing) prefillForm(editingIngredient!!)
        else updateQuantityUI()
    }

    private fun setupCategorySpinner() {
        val categories = listOf("Select a category") + Category.displayNames()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupUnitSpinner() {
        val units = listOf("Select unit") + FoodUnit.symbols()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUnit.adapter = adapter
    }

    private fun setupDatePickers() {
        binding.etPurchaseDate.setOnClickListener {
            showDatePicker(binding.etPurchaseDate, purchaseCalendar)
        }

        binding.etExpiryDate.setOnClickListener {
            showDatePicker(binding.etExpiryDate, expiryCalendar)
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
        binding.btnMinus.setOnClickListener {
            if (quantity > 0.5) {
                quantity -= 1.0
                updateQuantityUI()
            }
        }

        binding.btnPlus.setOnClickListener {
            quantity += 1.0
            updateQuantityUI()
        }
    }

    private fun updateQuantityUI() {
        // Format to 1 decimal place or whole number
        val formatted = if (quantity % 1.0 == 0.0) {
            String.format(Locale.getDefault(), "%.0f", quantity)
        } else {
            String.format(Locale.getDefault(), "%.1f", quantity)
        }
        binding.tvQuantity.text = formatted
    }

    private fun setupQuickTags() {
        val tags = listOf(binding.tagFresh, binding.tagFrozen, binding.tagCanned, binding.tagOpened)

        fun resetTags() {
            for (tag in tags) {
                tag.setBackgroundResource(R.drawable.bg_tag_unselected)
                tag.setTextColor(getColor(R.color.text_hint))
            }
        }

        fun selectTag(tag: TextView, name: String) {
            selectedTag = name
            resetTags()
            tag.setBackgroundResource(R.drawable.bg_tag_selected)
            tag.setTextColor(getColor(android.R.color.white))
        }

        binding.tagFresh.setOnClickListener { selectTag(binding.tagFresh, "Fresh") }
        binding.tagFrozen.setOnClickListener { selectTag(binding.tagFrozen, "Frozen") }
        binding.tagCanned.setOnClickListener { selectTag(binding.tagCanned, "Canned") }
        binding.tagOpened.setOnClickListener { selectTag(binding.tagOpened, "Opened") }

        selectTag(binding.tagFresh, "Fresh")
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnAddPhoto.setOnClickListener {
            Toast.makeText(this, "Photo upload can be added later", Toast.LENGTH_SHORT).show()
        }

        binding.btnSave.setOnClickListener {
            saveIngredient(editingIngredient != null)
        }
    }

    private fun prefillForm(ingredient: Ingredient) {
        binding.etItemName.setText(ingredient.name)

        // Select Category
        val categories = listOf("Select a category") + Category.displayNames()
        val catIndex = categories.indexOf(ingredient.category)
        if (catIndex >= 0) binding.spinnerCategory.setSelection(catIndex)

        // Select Unit
        val units = listOf("Select unit") + FoodUnit.symbols()
        val unitIndex = units.indexOf(ingredient.unit)
        if (unitIndex >= 0) binding.spinnerUnit.setSelection(unitIndex)

        // Quantity
        quantity = ingredient.quantity
        updateQuantityUI()

        // Price
        binding.etPrice.setText(ingredient.price.toString())

        // Dates formatting (DB stores as YYYY-MM-DD, UI shows MM/DD/YYYY)
        if (ingredient.purchaseDate != null) {
            binding.etPurchaseDate.setText(toDisplayDate(ingredient.purchaseDate))
        }
        if (ingredient.expiryDate.isNotEmpty()) {
            binding.etExpiryDate.setText(toDisplayDate(ingredient.expiryDate))
        }

        // Toggle Alert
        binding.switchExpiryAlert.isChecked = ingredient.alertEnabled ?: true

        // Quick Tag
        selectedTag = ingredient.quickTag ?: "Fresh"
        when (selectedTag) {
            "Frozen" -> binding.tagFrozen.performClick()
            "Canned" -> binding.tagCanned.performClick()
            "Opened" -> binding.tagOpened.performClick()
            else -> binding.tagFresh.performClick()
        }

        // Photo URL
        photoUrl = ingredient.photoUrl
    }

    private fun saveIngredient(isEditing: Boolean) {
        val itemName = binding.etItemName.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val purchaseDateText = binding.etPurchaseDate.text.toString().trim()
        val expiryDateText = binding.etExpiryDate.text.toString().trim()
        val unit = binding.spinnerUnit.selectedItem.toString()
        val priceText = binding.etPrice.text.toString().trim()
        val expiryAlertOn = binding.switchExpiryAlert.isChecked

        if (itemName.isEmpty()) {
            Toast.makeText(this, "Item name is required", Toast.LENGTH_SHORT).show()
            binding.etItemName.requestFocus()
            return
        }
        if (category == "Select a category") {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }
        if (unit == "Select unit") {
            Toast.makeText(this, "Please select a unit", Toast.LENGTH_SHORT).show()
            return
        }
        if (expiryDateText.isEmpty()) {
            Toast.makeText(this, "Expiry date is required", Toast.LENGTH_SHORT).show()
            return
        }

        val parsedPrice = priceText.toDoubleOrNull() ?: 0.0

        val payload = IngredientPayload(
            name = itemName,
            category = category,
            quantity = quantity,
            unit = unit,
            price = parsedPrice,
            expiryDate = toDbDate(expiryDateText),
            purchaseDate = if (purchaseDateText.isEmpty()) null else toDbDate(purchaseDateText),
            alertEnabled = expiryAlertOn,
            quickTag = selectedTag,
            photoUrl = photoUrl
        )

        binding.btnSave.isEnabled = false
        binding.btnSave.text = if (isEditing) "Updating..." else "Saving..."
        binding.btnSave.alpha = 0.7f

        lifecycleScope.launch {
            try {
                if (isEditing) {
                    IngredientRepository.update(editingIngredient!!.id!!, payload)
                    Toast.makeText(this@AddEditIngredientActivity, "$itemName updated!", Toast.LENGTH_SHORT).show()
                } else {
                    IngredientRepository.insert(payload)
                    Toast.makeText(this@AddEditIngredientActivity, "$itemName added!", Toast.LENGTH_SHORT).show()
                }
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddEditIngredientActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Save to Inventory"
                binding.btnSave.alpha = 1f
            }
        }
    }

    // Converts MM/DD/YYYY from UI to YYYY-MM-DD for DB
    private fun toDbDate(input: String): String {
        try {
            val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(input) ?: return input
            return outputFormat.format(date)
        } catch (e: Exception) {
            return input
        }
    }

    // Converts YYYY-MM-DD from DB to MM/DD/YYYY for UI
    private fun toDisplayDate(input: String): String {
        try {
            val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displayFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val date = dbFormat.parse(input) ?: return input
            return displayFormat.format(date)
        } catch (e: Exception) {
            return input
        }
    }
}
