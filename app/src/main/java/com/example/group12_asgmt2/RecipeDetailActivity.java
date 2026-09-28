package com.example.group12_asgmt2;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import com.bumptech.glide.Glide;

public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE = "RECIPE_DATA";

    private TextView instructionsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        try {
            // 1. Find the UI elements
            ImageView imageView = findViewById(R.id.imageView_detail_recipe);
            TextView titleText = findViewById(R.id.textView_detail_title);
            TextView usedIngredientsText = findViewById(R.id.textView_used_ingredients);
            TextView missedIngredientsText = findViewById(R.id.textView_missed_ingredients);
            instructionsText = findViewById(R.id.textView_instructions);

            // 2. Unpack the local recipe data sent from the adapter.
            Recipe clickedRecipe = IntentCompat.getSerializableExtra(
                    getIntent(), EXTRA_RECIPE, Recipe.class
            );

            // 3. Display the bundled data without a network request.
            if (clickedRecipe != null) {
                Object image = clickedRecipe.getImageResourceId() != 0
                        ? clickedRecipe.getImageResourceId()
                        : clickedRecipe.getImage();
                Glide.with(this).load(image).into(imageView);
                titleText.setText(clickedRecipe.getTitle());

                // Build list of ingredients you HAVE
                StringBuilder usedList = new StringBuilder();
                if (clickedRecipe.getUsedIngredients() != null) {
                    for (Recipe.Ingredient ingredient : clickedRecipe.getUsedIngredients()) {
                        if (ingredient != null) {
                            usedList.append("• ").append(ingredient.getOriginal()).append("\n");
                        }
                    }
                }
                if (usedList.length() == 0) usedList.append("None\n");
                usedIngredientsText.setText(usedList.toString());

                // Build list of ingredients you are MISSING
                StringBuilder missedList = new StringBuilder();
                if (clickedRecipe.getMissedIngredients() != null) {
                    for (Recipe.Ingredient ingredient : clickedRecipe.getMissedIngredients()) {
                        if (ingredient != null) {
                            missedList.append("• ").append(ingredient.getOriginal()).append("\n");
                        }
                    }
                }
                if (missedList.length() == 0) missedList.append("None! You have everything!\n");
                missedIngredientsText.setText(missedList.toString());

                instructionsText.setText(clickedRecipe.getInstructions());
            } else {
                Toast.makeText(this, "Recipe details are unavailable.", Toast.LENGTH_LONG).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Crash in Detail: " + e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

}
