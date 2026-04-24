package com.example.group12_asgmt2;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecipeDetailActivity extends AppCompatActivity {

    private TextView instructionsText;
    private SpoonacularApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // 1. Find the UI elements
        ImageView imageView = findViewById(R.id.imageView_detail_recipe);
        TextView titleText = findViewById(R.id.textView_detail_title);
        TextView usedIngredientsText = findViewById(R.id.textView_used_ingredients);
        TextView missedIngredientsText = findViewById(R.id.textView_missed_ingredients);
        instructionsText = findViewById(R.id.textView_instructions);

        // 2. Initialize Retrofit for this screen
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spoonacular.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(SpoonacularApi.class);

        // 3. Unpack the Recipe data sent from the Adapter
        Recipe clickedRecipe = (Recipe) getIntent().getSerializableExtra("RECIPE_DATA");

        // 4. Display the data and fetch instructions
        if (clickedRecipe != null) {
            Glide.with(this).load(clickedRecipe.getImage()).into(imageView);
            titleText.setText(clickedRecipe.getTitle());

            // Build list of ingredients you HAVE
            StringBuilder usedList = new StringBuilder();
            for (Recipe.Ingredient ingredient : clickedRecipe.getUsedIngredients()) {
                usedList.append("• ").append(ingredient.getOriginal()).append("\n");
            }
            if (usedList.length() == 0) usedList.append("None\n");
            usedIngredientsText.setText(usedList.toString());

            // Build list of ingredients you are MISSING
            StringBuilder missedList = new StringBuilder();
            for (Recipe.Ingredient ingredient : clickedRecipe.getMissedIngredients()) {
                missedList.append("• ").append(ingredient.getOriginal()).append("\n");
            }
            if (missedList.length() == 0) missedList.append("None! You have everything!\n");
            missedIngredientsText.setText(missedList.toString());

            // 👉 NEW: Trigger the network call to get the cooking steps!
            fetchRecipeInstructions(clickedRecipe.getId());
        }
    }

    private void fetchRecipeInstructions(int recipeId) {
        String myApiKey = getString(R.string.api_key);

        Call<RecipeDetails> call = apiService.getRecipeInformation(recipeId, myApiKey);

        call.enqueue(new Callback<RecipeDetails>() {
            @Override
            public void onResponse(Call<RecipeDetails> call, Response<RecipeDetails> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Check if Spoonacular actually has instructions for this specific dish
                    String steps = response.body().getInstructions();
                    if (steps != null && !steps.isEmpty()) {
                        instructionsText.setText(formatInstructions(steps));
                    } else {
                        instructionsText.setText("No detailed instructions provided by the chef for this dish.");
                    }
                } else {
                    instructionsText.setText("Failed to load instructions.");
                }
            }

            @Override
            public void onFailure(Call<RecipeDetails> call, Throwable t) {
                instructionsText.setText("Check your internet connection.");
            }
        });
    }

    private CharSequence formatInstructions(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "";

        // 1. Replace HTML list tags with a unique delimiter before stripping HTML
        String text = raw.replace("<li>", "|||").replace("</li>", "|||")
                         .replace("<ol>", "").replace("</ol>", "")
                         .replace("<ul>", "").replace("</ul>", "");

        // 2. Strip all remaining HTML (like <br> becomes \n, <b> is removed)
        String plainText = androidx.core.text.HtmlCompat.fromHtml(text, androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim();

        // 3. Decide how to split the text
        String[] stepsArray;
        if (plainText.contains("|||")) {
            // It had HTML list items, split by our delimiter
            stepsArray = plainText.split("\\|\\|\\|");
        } else if (plainText.contains("\n")) {
            // It had <br> or raw newlines
            stepsArray = plainText.split("\n+");
        } else {
            // It's a solid block of text, split by sentences (period, exclamation, question mark)
            stepsArray = plainText.split("(?<=[.!?])\\s+");
        }

        // 4. Build the final numbered string
        StringBuilder formatted = new StringBuilder();
        int stepNum = 1;
        for (String step : stepsArray) {
            step = step.trim();
            
            // Clean up existing numbering or bullets to avoid "1. 1. Do this"
            step = step.replaceFirst("^\\d+\\.\\s*", ""); // Removes "1. ", "2. ", etc.
            step = step.replaceFirst("^[•\\-\\*]\\s*", ""); // Removes bullet points

            if (!step.isEmpty() && step.length() > 2) {
                formatted.append(stepNum).append(". ").append(step).append("\n\n");
                stepNum++;
            }
        }
        return formatted.toString().trim();
    }
}