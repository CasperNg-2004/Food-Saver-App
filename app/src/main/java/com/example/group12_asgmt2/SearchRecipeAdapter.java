package com.example.group12_asgmt2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

public class SearchRecipeAdapter extends RecyclerView.Adapter<SearchRecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipeList;
    private Context context;

    // Constructor to pass the data into the adapter
    public SearchRecipeAdapter(Context context, List<Recipe> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This tells the adapter to use the item_recipe.xml blueprint we just designed
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        // Get the specific recipe for this specific row
        Recipe recipe = recipeList.get(position);

        // Paste the title and missing ingredient count onto the screen
        holder.titleText.setText(recipe.getTitle());
        holder.missingText.setText("Missing Ingredients: " + recipe.getMissedIngredientCount());

        // Use Glide to download the picture from the internet URL and put it in the ImageView
        Glide.with(context)
                .load(recipe.getImage())
                .into(holder.recipeImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Create an Intent (a delivery truck) to go to a new screen
                    android.content.Intent intent = new android.content.Intent(context, RecipeDetailActivity.class);

                    // Put the specific recipe they clicked inside the truck
                    intent.putExtra("RECIPE_DATA", recipe);

                    // Start the drive!
                    context.startActivity(intent);
                } catch (Exception e) {
                    android.widget.Toast.makeText(context, "Error opening recipe: " + e.toString(), android.widget.Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeList != null ? recipeList.size() : 0;
    }

    // This class finds the exact IDs inside your item_recipe.xml layout
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        TextView titleText;
        TextView missingText;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.imageView_recipe);
            titleText = itemView.findViewById(R.id.textView_recipe_title);
            missingText = itemView.findViewById(R.id.textView_missing_ingredients);
        }
    }
}
