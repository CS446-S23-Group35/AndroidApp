package com.example.androidapp

// Recipe is a data class containing all the information about a recipe.
class Recipe(name: String, ingredients: List<String>, estimatedTime: String, imageURL: String) {



    class Ingredient {
        var name: String? = null
        var amount: IngredientAmount? = null
        var notes: String? = null
        var optional = false
    }

    class IngredientAmount {
        var unit = 0
        var unitStr: String? = null
        var value = 0.0
    }

    class Metadata {
        //var tags: Array<String>
        var minutes_to_prep = 0
        var minutes_to_cook = 0
        var minutes_total = 0
        var difficulty = 0
        var servings: ServingRange? = null
        var estimated_calories = 0
        var image_url: String? = null
        var image_alt: String? = null
        var source_url: String? = null
        var dietary: DietartyMetadata? = null
    }

    class DietartyMetadata {
        var is_vegetarian = false
        var is_vegan = false
        var is_gluten_free = false
        var is_dairy_free = false
        var is_nut_free = false
        var is_shellfish_free = false
        var is_egg_free = false
        var is_soy_free = false
        var is_fish_free = false
        var is_pork_free = false
        var is_red_meat_free = false
        var is_alcohol_free = false
        var is_kosher = false
        var is_halal = false
    }

    class ServingRange {
        var min = 0
        var max = 0
        var alternative: String? = null
    }

    var name: String? = null
    var description: String? = null




    lateinit var tomato: Ingredient
    lateinit var cilantro: Ingredient


    var ingredients: List<String>
    var steps: Array<String>
    var metadata: Metadata? = null

//    fun initializeValues(){
//        tomato.name = "tomato"
//        cilantro.name = "cilantro"
//        ingredients = arrayOf(tomato, cilantro)
//        steps = arrayOf("Add tomato", "add cilantro", "cook 45 minutes")
//
//    }

    init{
        this.name = name
        this.ingredients = ingredients
        this.steps = arrayOf("1. ", "2. ", "3. ", "4. ", "5. ")
        this.metadata?.minutes_to_cook  = estimatedTime.toInt()
        this.metadata?.image_url = imageURL
    }



}