package com.example.group12_asgmt2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    // 1. Declare your UI elements and API Service
    private EditText ingredientInputBox;
    private Button searchButton;
    private ProgressBar loadingSpinner;
    private RecyclerView recipeRecyclerView;
    private SpoonacularApi apiService;

    private LinearLayout navHome;
    private LinearLayout navPantry;
    private LinearLayout navScan;
    private LinearLayout navStats;
    private LinearLayout navRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure this matches the name of your XML layout file!
        setContentView(R.layout.activity_main);

        // 2. Connect your Java variables to your XML IDs
        ingredientInputBox = findViewById(R.id.editText_ingredients);
        searchButton = findViewById(R.id.button_search_recipes);
        loadingSpinner = findViewById(R.id.progressBar_loading);
        recipeRecyclerView = findViewById(R.id.recyclerView_recipes);

        navHome = findViewById(R.id.navHome);
        navPantry = findViewById(R.id.navPantry);
        navScan = findViewById(R.id.navScan);
        navStats = findViewById(R.id.navStats);
        navRecipes = findViewById(R.id.navRecipes);

        recipeRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        // 3. Initialize Retrofit ONCE when the page opens
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spoonacular.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(SpoonacularApi.class);

        // 4. Tell the button what to do when clicked
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get whatever the user typed in the box
                String userInput = ingredientInputBox.getText().toString().trim();

                if (!userInput.isEmpty()) {
                    // Trigger the network call!
                    searchForRecipes(userInput);
                } else {
                    // FIXED: Changed to MainActivity.this
                    Toast.makeText(MainActivity.this, "Please enter some ingredients!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 5. Navigation Bar Logic
        navHome.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MainActivity.this, DashboardActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        navPantry.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MainActivity.this, InventoryActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        navScan.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Scan feature can be added later", Toast.LENGTH_SHORT).show();
        });
        navStats.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MainActivity.this, StatisticsActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        navRecipes.setOnClickListener(v -> {
            // Already here
        });
    }

    // ==========================================
    // THE NETWORK CALL METHOD
    // ==========================================
    private void searchForRecipes(String ingredientsList) {
        // Show the loading spinner while waiting for the internet
        loadingSpinner.setVisibility(View.VISIBLE);

        String myApiKey = getString(R.string.api_key);

        // Make the call using the parameter passed into the method
        Call<List<Recipe>> call = apiService.getRecipesByIngredients(ingredientsList, 10, myApiKey);

        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                // Hide the loading spinner
                loadingSpinner.setVisibility(View.GONE);

                if (!response.isSuccessful()) {
                    // FIXED: Changed to MainActivity.this
                    Toast.makeText(MainActivity.this, "Error finding recipes.", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Recipe> recipes = response.body();
                if (recipes != null && !recipes.isEmpty()) {
                    Log.d("RecipePage", "SUCCESS! Found " + recipes.size() + " recipes.");
                    SearchRecipeAdapter adapter = new SearchRecipeAdapter(MainActivity.this, recipes);
                    recipeRecyclerView.setAdapter(adapter);
                    // PHASE 4 TODO:
                    // Send this 'recipes' list to your RecyclerView Adapter here!

                } else {
                    // FIXED: Changed to MainActivity.this
                    Toast.makeText(MainActivity.this, "No recipes found for those ingredients.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                // Hide the loading spinner
                loadingSpinner.setVisibility(View.GONE);
                // FIXED: Changed to MainActivity.this
                Toast.makeText(MainActivity.this, "Check your internet connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}