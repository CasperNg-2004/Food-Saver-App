package com.example.group12_asgmt2;

import com.google.gson.annotations.SerializedName;

public class RecipeDetails {
    // Spoonacular returns the cooking steps as one big block of text
    @SerializedName("instructions")
    private String instructions;

    public String getInstructions() {
        return instructions;
    }
}