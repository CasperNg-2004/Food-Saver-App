package com.example.group12_asgmt2;

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

    private int imageResourceId;
    private int readyInMinutes;
    private String instructions;

    @SerializedName("usedIngredientCount")
    private int usedIngredientCount;

    @SerializedName("missedIngredientCount")
    private int missedIngredientCount;

    @SerializedName("usedIngredients")
    private List<Ingredient> usedIngredients;

    @SerializedName("missedIngredients")
    private List<Ingredient> missedIngredients;

    public Recipe(
            int id,
            String title,
            int imageResourceId,
            int readyInMinutes,
            String instructions,
            List<Ingredient> usedIngredients,
            List<Ingredient> missedIngredients
    ) {
        this.id = id;
        this.title = title;
        this.imageResourceId = imageResourceId;
        this.readyInMinutes = readyInMinutes;
        this.instructions = instructions;
        this.usedIngredients = usedIngredients;
        this.missedIngredients = missedIngredients;
        this.usedIngredientCount = usedIngredients != null ? usedIngredients.size() : 0;
        this.missedIngredientCount = missedIngredients != null ? missedIngredients.size() : 0;
    }

    // Getters for Recipe
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getImage() { return image; }
    public int getImageResourceId() { return imageResourceId; }
    public int getReadyInMinutes() { return readyInMinutes; }
    public String getInstructions() { return instructions; }
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

        public Ingredient(int id, String name, double amount, String unit, String original) {
            this.id = id;
            this.name = name;
            this.amount = amount;
            this.unit = unit;
            this.original = original;
        }

        // Getters for Ingredient
        public int getId() { return id; }
        public String getName() { return name; }
        public double getAmount() { return amount; }
        public String getUnit() { return unit; }
        public String getOriginal() { return original; }
    }
}
