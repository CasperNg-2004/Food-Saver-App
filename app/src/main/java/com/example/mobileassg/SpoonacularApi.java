package com.example.mobileassg;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Path;

public interface SpoonacularApi {
    @GET("recipes/findByIngredients")
    Call<List<Recipe>> getRecipesByIngredients(
            @Query("ingredients") String ingredients, // e.g., "apples,flour,sugar"
            @Query("number") int numberOfRecipes,     // e.g., 5 or 10
            @Query("apiKey") String apiKey            // Spoonacular API Key
    );

    @GET("recipes/{id}/information")
    Call<RecipeDetails> getRecipeInformation(
            @Path("id") int recipeId, // This replaces the {id} in the URL
            @Query("apiKey") String apiKey
    );
}

