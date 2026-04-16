package com.example.mobileassg;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.io.Serializable;

public class Recipe implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("image")
    private String image;

    @SerializedName("usedIngredientCount")
    private int usedIngredientCount;

    @SerializedName("missedIngredientCount")
    private int missedIngredientCount;

    @SerializedName("usedIngredients")
    private List<Ingredient> usedIngredients;

    @SerializedName("missedIngredients")
    private List<Ingredient> missedIngredients;

    // Getters for Recipe
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getImage() { return image; }
    public int getUsedIngredientCount() { return usedIngredientCount; }
    public int getMissedIngredientCount() { return missedIngredientCount; }
    public List<Ingredient> getUsedIngredients() { return usedIngredients; }
    public List<Ingredient> getMissedIngredients() { return missedIngredients; }

    // ==========================================
    // NESTED INGREDIENT CLASS
    // Must be 'public static' for Gson to work
    // ==========================================
    public static class Ingredient implements Serializable {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("amount")
        private double amount;

        @SerializedName("unit")
        private String unit;

        @SerializedName("original")
        private String original;

        // Getters for Ingredient
        public int getId() { return id; }
        public String getName() { return name; }
        public double getAmount() { return amount; }
        public String getUnit() { return unit; }
        public String getOriginal() { return original; }
    }
}
