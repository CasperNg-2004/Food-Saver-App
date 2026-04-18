package com.group12.ingredientmanager.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.group12.ingredientmanager.data.Category
import com.group12.ingredientmanager.data.FoodUnit
import com.group12.ingredientmanager.data.Ingredient
import com.group12.ingredientmanager.data.IngredientPayload
import com.group12.ingredientmanager.data.IngredientRepository
import com.group12.ingredientmanager.databinding.ActivityAddEditIngredientBinding
import com.group12.ingredientmanager.util.DateUtils
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddEditIngredientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditIngredientBinding
    private var editingIngredient: Ingredient? = null
    private var selectedDate: LocalDate = LocalDate.now().plusDays(7)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditIngredientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Reconstruct ingredient from individual Intent extras (no serialization needed)
        val ingredientId = intent.getStringExtra(MainActivity.EXTRA_ID)
        if (ingredientId != null) {
            editingIngredient = Ingredient(
                id       = ingredientId,
                name     = intent.getStringExtra(MainActivity.EXTRA_NAME)     ?: "",
                category = intent.getStringExtra(MainActivity.EXTRA_CATEGORY) ?: "",
                quantity = intent.getDoubleExtra(MainActivity.EXTRA_QUANTITY, 0.0),
                unit     = intent.getStringExtra(MainActivity.EXTRA_UNIT)     ?: "",
                price    = intent.getDoubleExtra(MainActivity.EXTRA_PRICE, 0.0),
                expiryDate = intent.getStringExtra(MainActivity.EXTRA_EXPIRY) ?: ""
            )
        }

        val isEditing = editingIngredient != null
        binding.toolbar.title = if (isEditing) "Edit Ingredient" else "Add Ingredient"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupCategoryDropdown()
        setupUnitDropdown()
        setupDatePicker()
        setupSaveButton(isEditing)

        if (isEditing) prefillForm(editingIngredient!!)
    }

    private fun setupCategoryDropdown() {
        val categories = Category.displayNames()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.autoCompleteCategory.setAdapter(adapter)
    }

    private fun setupUnitDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, FoodUnit.symbols())
        binding.autoCompleteUnit.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        updateDateDisplay()
        binding.btnPickDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    updateDateDisplay()
                },
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth
            ).show()
        }
    }

    private fun updateDateDisplay() {
        binding.tvSelectedDate.text = DateUtils.formatForDisplay(DateUtils.formatForDb(selectedDate))
    }

    private fun setupSaveButton(isEditing: Boolean) {
        binding.btnSave.text = if (isEditing) "Update Ingredient" else "Add Ingredient"
        binding.btnSave.setOnClickListener {
            if (validateForm()) saveIngredient(isEditing)
        }
    }

    private fun prefillForm(ingredient: Ingredient) {
        binding.etName.setText(ingredient.name)
        binding.autoCompleteCategory.setText(ingredient.category, false)
        binding.etQuantity.setText(ingredient.quantity.toString())
        binding.autoCompleteUnit.setText(ingredient.unit, false)
        binding.etPrice.setText(ingredient.price.toString())
        if (ingredient.expiryDate.isNotEmpty()) {
            selectedDate = LocalDate.parse(ingredient.expiryDate)
            updateDateDisplay()
        }
    }

    private fun validateForm(): Boolean {
        var valid = true

        if (binding.etName.text.isNullOrBlank()) {
            binding.tilName.error = "Name is required"
            valid = false
        } else binding.tilName.error = null

        if (binding.autoCompleteCategory.text.isNullOrBlank()) {
            binding.tilCategory.error = "Category is required"
            valid = false
        } else binding.tilCategory.error = null

        if (binding.etQuantity.text.isNullOrBlank()) {
            binding.tilQuantity.error = "Quantity is required"
            valid = false
        } else binding.tilQuantity.error = null

        if (binding.autoCompleteUnit.text.isNullOrBlank()) {
            binding.tilUnit.error = "Unit is required"
            valid = false
        } else binding.tilUnit.error = null

        if (binding.etPrice.text.isNullOrBlank()) {
            binding.tilPrice.error = "Price is required"
            valid = false
        } else binding.tilPrice.error = null

        return valid
    }

    private fun saveIngredient(isEditing: Boolean) {
        val payload = IngredientPayload(
            name      = binding.etName.text.toString().trim(),
            category  = binding.autoCompleteCategory.text.toString().trim(),
            quantity  = binding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0,
            unit      = binding.autoCompleteUnit.text.toString().trim(),
            price     = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0,
            expiryDate = DateUtils.formatForDb(selectedDate)
        )

        binding.btnSave.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                if (isEditing) {
                    IngredientRepository.update(editingIngredient!!.id!!, payload)
                    Toast.makeText(
                        this@AddEditIngredientActivity,
                        "${payload.name} updated!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    IngredientRepository.insert(payload)
                    Toast.makeText(
                        this@AddEditIngredientActivity,
                        "${payload.name} added!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@AddEditIngredientActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.btnSave.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
