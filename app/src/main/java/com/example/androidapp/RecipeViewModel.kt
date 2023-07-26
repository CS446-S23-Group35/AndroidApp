package com.example.androidapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.Normalizer


class RecipeViewModel : ViewModel() {
    // list of recipes passed by server
    var recipes =  mutableListOf<Recipe>();

    // list of recipes being displayed.
    private var liveRecipes =  MutableLiveData<List<Recipe>>()

    init {
        recipes.forEach{
            //it.initializeValues()
        }
        //liveRecipes[0]

    }

    fun getRecipes() : LiveData<List<Recipe>> {
        return liveRecipes;
    }

    // create new recipe and update the current recipes being displayed.
    fun addRecipe(name : String, ingredients : List<String>, estimatedTime: String, imageURL: String) {
        val recipe = Recipe(name, ingredients, estimatedTime, imageURL);
        recipes.add(recipe);
        liveRecipes.value = recipes.toList();
    }

    // remove all recipes
    fun deleteRecipes() {
        recipes =  mutableListOf<Recipe>();
    }

    // fetch recipes whose name matches the query.
    fun queryRecipes(query: String) {
        liveRecipes.value = recipes.filter { Normalizer.normalize(it.name, Normalizer.Form.NFD).contains(query) }
            .toList()
            .takeIf { query != "" }
            ?: recipes.toList()
    }
}