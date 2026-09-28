package com.example.group12_asgmt2

import java.util.Locale

/** A bundled recipe catalogue that works without a network connection or API key. */
object LocalRecipeRepository {
    private data class IngredientSpec(val keyword: String, val original: String)

    private data class LocalRecipe(
        val id: Int,
        val title: String,
        val imageResourceId: Int,
        val readyInMinutes: Int,
        val ingredients: List<IngredientSpec>,
        val instructions: String,
    )

    private fun ingredient(keyword: String, original: String) = IngredientSpec(keyword, original)

    private val catalogue = listOf(
        LocalRecipe(
            1, "Quick Chicken Stir-Fry", R.drawable.recipe_spicy_chicken, 25,
            listOf(
                ingredient("chicken", "250 g chicken, sliced"),
                ingredient("pepper", "1 bell pepper, sliced"),
                ingredient("onion", "1 onion, sliced"),
                ingredient("soy sauce", "2 tbsp soy sauce"),
            ),
            "1. Slice the chicken and vegetables.\n\n2. Cook the chicken in a hot pan until golden and cooked through.\n\n3. Add the onion and pepper, then stir-fry for 4 minutes.\n\n4. Add soy sauce, toss well, and serve hot.",
        ),
        LocalRecipe(
            2, "Pantry Vegetable Fried Rice", R.drawable.recipe_vegetable_soup, 20,
            listOf(
                ingredient("rice", "2 cups cooked rice"),
                ingredient("egg", "2 eggs"),
                ingredient("carrot", "1 carrot, diced"),
                ingredient("pea", "1/2 cup peas"),
                ingredient("soy sauce", "2 tbsp soy sauce"),
            ),
            "1. Scramble the eggs in a large pan and set aside.\n\n2. Stir-fry the carrot and peas until tender.\n\n3. Add the rice and soy sauce and cook until hot.\n\n4. Fold in the eggs and serve.",
        ),
        LocalRecipe(
            3, "Creamy Tomato Pasta", R.drawable.recipe_avocado_salad, 25,
            listOf(
                ingredient("pasta", "200 g pasta"),
                ingredient("tomato", "2 tomatoes, chopped"),
                ingredient("milk", "1/2 cup milk"),
                ingredient("cheese", "1/2 cup grated cheese"),
                ingredient("onion", "1/2 onion, diced"),
            ),
            "1. Boil the pasta until tender and reserve a little cooking water.\n\n2. Sauté the onion, then add the tomatoes and cook until soft.\n\n3. Stir in the milk and cheese over low heat.\n\n4. Add the pasta, loosen with cooking water if needed, and season to taste.",
        ),
        LocalRecipe(
            4, "Everyday Vegetable Soup", R.drawable.recipe_vegetable_soup, 35,
            listOf(
                ingredient("carrot", "2 carrots, chopped"),
                ingredient("potato", "2 potatoes, cubed"),
                ingredient("onion", "1 onion, chopped"),
                ingredient("tomato", "2 tomatoes, chopped"),
                ingredient("cabbage", "1 cup shredded cabbage"),
            ),
            "1. Sauté the onion in a large pot until softened.\n\n2. Add the remaining vegetables and cover with water or stock.\n\n3. Simmer for 25 minutes or until the potatoes are tender.\n\n4. Season with salt and pepper before serving.",
        ),
        LocalRecipe(
            5, "Avocado Egg Toast", R.drawable.recipe_avocado_salad, 15,
            listOf(
                ingredient("bread", "2 slices bread"),
                ingredient("avocado", "1 ripe avocado"),
                ingredient("egg", "2 eggs"),
            ),
            "1. Toast the bread until crisp.\n\n2. Mash the avocado with a pinch of salt.\n\n3. Fry or poach the eggs to your liking.\n\n4. Spread avocado on the toast, top with eggs, and serve.",
        ),
        LocalRecipe(
            6, "Tuna Potato Patties", R.drawable.recipe_spicy_chicken, 30,
            listOf(
                ingredient("tuna", "1 can tuna, drained"),
                ingredient("potato", "2 potatoes, boiled and mashed"),
                ingredient("egg", "1 egg"),
                ingredient("onion", "1/2 onion, finely chopped"),
            ),
            "1. Mix the tuna, mashed potato, egg, and onion.\n\n2. Season and shape the mixture into small patties.\n\n3. Pan-fry for 3 to 4 minutes per side until golden.\n\n4. Drain briefly and serve warm.",
        ),
        LocalRecipe(
            7, "Banana Oat Pancakes", R.drawable.recipe_avocado_salad, 15,
            listOf(
                ingredient("banana", "1 ripe banana"),
                ingredient("oat", "1 cup oats"),
                ingredient("egg", "1 egg"),
                ingredient("milk", "1/2 cup milk"),
            ),
            "1. Mash the banana in a bowl.\n\n2. Mix in the oats, egg, and milk to make a thick batter.\n\n3. Spoon into a lightly oiled pan over medium heat.\n\n4. Cook until bubbles form, flip, and cook until golden.",
        ),
        LocalRecipe(
            8, "Chicken Rice Bowl", R.drawable.recipe_spicy_chicken, 30,
            listOf(
                ingredient("chicken", "250 g chicken"),
                ingredient("rice", "2 cups cooked rice"),
                ingredient("cucumber", "1 cucumber, sliced"),
                ingredient("carrot", "1 carrot, shredded"),
            ),
            "1. Season and pan-cook the chicken until fully cooked.\n\n2. Slice the chicken and prepare the vegetables.\n\n3. Divide warm rice between bowls.\n\n4. Add chicken and vegetables, then finish with your preferred sauce.",
        ),
        LocalRecipe(
            9, "Cheesy Vegetable Omelette", R.drawable.recipe_vegetable_soup, 15,
            listOf(
                ingredient("egg", "3 eggs"),
                ingredient("cheese", "1/3 cup grated cheese"),
                ingredient("tomato", "1 tomato, diced"),
                ingredient("spinach", "1 handful spinach"),
            ),
            "1. Beat the eggs with salt and pepper.\n\n2. Cook the tomato and spinach briefly in a non-stick pan.\n\n3. Pour in the eggs and cook over medium-low heat.\n\n4. Add cheese, fold the omelette, and cook until set.",
        ),
        LocalRecipe(
            10, "Crispy Potato Egg Hash", R.drawable.recipe_spicy_chicken, 25,
            listOf(
                ingredient("potato", "2 potatoes, diced"),
                ingredient("egg", "2 eggs"),
                ingredient("onion", "1/2 onion, chopped"),
            ),
            "1. Parboil the diced potatoes for 5 minutes and drain.\n\n2. Fry the onion and potatoes until crisp and golden.\n\n3. Make two spaces in the pan and crack in the eggs.\n\n4. Cover and cook until the egg whites are set.",
        ),
    )

    @JvmStatic
    fun findSuggestions(pantryIngredientNames: Collection<String>, limit: Int = 10): List<Recipe> {
        val pantry = pantryIngredientNames.map(::normalize).filter(String::isNotBlank)

        return catalogue
            .map { definition ->
                val (used, missed) = definition.ingredients.partition { spec ->
                    pantry.any { pantryName -> ingredientsMatch(pantryName, normalize(spec.keyword)) }
                }
                definition to Recipe(
                    definition.id,
                    definition.title,
                    definition.imageResourceId,
                    definition.readyInMinutes,
                    definition.instructions,
                    used.mapIndexed { index, spec -> spec.toRecipeIngredient(index) },
                    missed.mapIndexed { index, spec -> spec.toRecipeIngredient(index + 100) },
                )
            }
            .sortedWith(
                compareByDescending<Pair<LocalRecipe, Recipe>> { it.second.usedIngredientCount }
                    .thenBy { it.second.missedIngredientCount }
                    .thenBy { it.first.readyInMinutes },
            )
            .take(limit)
            .map { it.second }
    }

    private fun IngredientSpec.toRecipeIngredient(id: Int) =
        Recipe.Ingredient(id, keyword, 0.0, "", original)

    private fun normalize(value: String): String = value
        .lowercase(Locale.ROOT)
        .replace(Regex("[^a-z0-9 ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun ingredientsMatch(pantryName: String, keyword: String): Boolean =
        pantryName == keyword || pantryName.contains(keyword) || keyword.contains(pantryName)
}
