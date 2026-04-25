package com.example.group12_asgmt2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecipeActivity extends AppCompatActivity {

    private ProgressBar loadingSpinner;
    private RecyclerView recipeRecyclerView;
    private SpoonacularApi apiService;

    private TextView pantryStatusTextView;
    private RecyclerView pantryRecyclerView;
    private PantryChipAdapter pantryAdapter;

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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spoonacular.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(SpoonacularApi.class);


        loadPantryIngredients();
    }

    private void loadPantryIngredients() {
        IngredientHelper.fetchAllIngredients(new IngredientCallback() {
            @Override
            public void onSuccess(List<Ingredient> ingredients) {
                List<Ingredient> validIngredients = new ArrayList<>();
                StringBuilder ingredientsListStr = new StringBuilder();

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
                    pantryStatusTextView.setText("Your pantry is empty or all items are expired.");
                } else {
                    pantryStatusTextView.setVisibility(View.GONE);
                    pantryAdapter.setIngredients(validIngredients);

                    for (int i = 0; i < validIngredients.size(); i++) {
                        ingredientsListStr.append(validIngredients.get(i).getName());
                        if (i < validIngredients.size() - 1) {
                            ingredientsListStr.append(", ");
                        }
                    }

                    // Auto-search
                    String finalIngredients = ingredientsListStr.toString();
                    searchForRecipes(finalIngredients);
                }
            }

            @Override
            public void onError(Exception e) {
                pantryStatusTextView.setText("Failed to load pantry items.");
                Log.e("RecipeActivity", "Error loading pantry", e);
            }
        });
    }

    private void searchForRecipes(String ingredientsList) {
        loadingSpinner.setVisibility(View.VISIBLE);
        String myApiKey = getString(R.string.api_key);

        Call<List<Recipe>> call = apiService.getRecipesByIngredients(ingredientsList, 10, myApiKey);
        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                loadingSpinner.setVisibility(View.GONE);

                if (!response.isSuccessful()) {
                    Toast.makeText(RecipeActivity.this, "Error finding recipes.", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Recipe> recipes = response.body();
                if (recipes != null && !recipes.isEmpty()) {
                    Log.d("RecipePage", "SUCCESS! Found " + recipes.size() + " recipes.");
                    SearchRecipeAdapter adapter = new SearchRecipeAdapter(RecipeActivity.this, recipes);
                    recipeRecyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(RecipeActivity.this, "No recipes found for those ingredients.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                Toast.makeText(RecipeActivity.this, "Check your internet connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}