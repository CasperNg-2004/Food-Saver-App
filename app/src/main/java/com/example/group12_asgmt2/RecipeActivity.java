package com.example.group12_asgmt2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import kotlinx.coroutines.Job;

public class RecipeActivity extends AppCompatActivity {

    private ProgressBar loadingSpinner;
    private RecyclerView recipeRecyclerView;
    private TextView pantryStatusTextView;
    private RecyclerView pantryRecyclerView;
    private PantryChipAdapter pantryAdapter;
    private Job pantryLoadJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        loadingSpinner = findViewById(R.id.progressBar_loading);
        recipeRecyclerView = findViewById(R.id.recyclerView_recipes);

        pantryStatusTextView = findViewById(R.id.tv_pantry_status);
        pantryRecyclerView = findViewById(R.id.recyclerView_pantry);

        recipeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pantryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        pantryAdapter = new PantryChipAdapter(new ArrayList<>());
        pantryRecyclerView.setAdapter(pantryAdapter);

        setupBottomNavigation();
        loadPantryIngredients();
    }

    private void setupBottomNavigation() {
        LinearLayout navHome    = findViewById(R.id.navHome);
        LinearLayout navPantry  = findViewById(R.id.navPantry);
        LinearLayout navStats   = findViewById(R.id.navStats);
        LinearLayout navRecipes = findViewById(R.id.navRecipes);

        navHome.setOnClickListener(v -> {
            AppNavigator.open(RecipeActivity.this, DashboardActivity.class, true);
        });
        navPantry.setOnClickListener(v -> {
            AppNavigator.open(RecipeActivity.this, InventoryActivity.class, true);
        });
        navStats.setOnClickListener(v -> {
            AppNavigator.open(RecipeActivity.this, StatisticsActivity.class, true);
        });
        navRecipes.setOnClickListener(v -> {
            // Already here
        });
    }

    private void loadPantryIngredients() {
        pantryLoadJob = IngredientHelper.fetchAllIngredients(new IngredientCallback() {
            @Override
            public void onSuccess(List<Ingredient> ingredients) {
                List<Ingredient> validIngredients = new ArrayList<>();
                for (Ingredient ing : ingredients) {
                    DateUtils.ExpiryStatus status = DateUtils.INSTANCE.getExpiryStatus(ing.getExpiryDate());
                    // Filter out expired food
                    if (status != DateUtils.ExpiryStatus.EXPIRED) {
                        validIngredients.add(ing);
                    }
                }

                // Sort: Near Expiry (SOON) first, then Fresh
                Collections.sort(validIngredients, new Comparator<Ingredient>() {
                    @Override
                    public int compare(Ingredient i1, Ingredient i2) {
                        long d1 = DateUtils.INSTANCE.daysUntilExpiry(i1.getExpiryDate());
                        long d2 = DateUtils.INSTANCE.daysUntilExpiry(i2.getExpiryDate());
                        return Long.compare(d1, d2);
                    }
                });

                if (validIngredients.isEmpty()) {
                    pantryStatusTextView.setText("Add pantry items for personalised matches. Showing starter ideas for now.");
                } else {
                    pantryStatusTextView.setVisibility(View.GONE);
                    pantryAdapter.setIngredients(validIngredients);
                }

                List<String> pantryNames = new ArrayList<>();
                for (Ingredient ingredient : validIngredients) {
                    pantryNames.add(ingredient.getName());
                }
                showLocalRecipes(pantryNames);
            }

            @Override
            public void onError(Exception e) {
                pantryStatusTextView.setText("Failed to load pantry items.");
                Log.e("RecipeActivity", "Error loading pantry", e);
            }
        });
    }

    private void showLocalRecipes(List<String> pantryNames) {
        loadingSpinner.setVisibility(View.GONE);
        List<Recipe> recipes = LocalRecipeRepository.findSuggestions(pantryNames, 10);
        recipeRecyclerView.setAdapter(new SearchRecipeAdapter(this, recipes));
    }

    @Override
    protected void onDestroy() {
        if (pantryLoadJob != null) pantryLoadJob.cancel(null);
        super.onDestroy();
    }
}
