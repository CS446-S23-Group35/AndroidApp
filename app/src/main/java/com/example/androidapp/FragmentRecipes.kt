package com.example.androidapp

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.androidapp.databinding.FragmentRecipesBinding

// To get images from url to setup in image view.
import com.squareup.picasso.Picasso



/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FragmentRecipes : Fragment() {

    private var _binding: FragmentRecipesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRecipesBinding.inflate(inflater, container, false)

        // importing the recipe view model which contains recipe search business logic
        var recipeViewModel = ViewModelProvider(requireActivity())[RecipeViewModel::class.java]

        // For now we delete the recipes in our recipeViewModel before we create new ones
        // This workflow needs to be changed later on and be more stable.
        //recipeViewModel.rec
        recipeViewModel.deleteRecipes();
        createRecipes(recipeViewModel);
        //recipeViewModel.addRecipe()

        // Observe any changes in displayed recipes to update the Recipe View
        recipeViewModel.getRecipes().observe(viewLifecycleOwner) { recipes ->
            binding.recipeScrollHost.removeAllViews();

            recipes.forEach{recipe ->
                // Setting up specific recipe overview card for each recipe
                val recipeOverviewCard = inflater.inflate(R.layout.recipe_overview_card, null);
                val recipeName = recipeOverviewCard.findViewById<TextView>(R.id.recipe_name);
                val recipeIngredients = recipeOverviewCard.findViewById<TextView>(R.id.recipe_ingredients);
                val recipeEstimatedTime = recipeOverviewCard.findViewById<TextView>(R.id.recipe_estimated_time);
                val recipeImage = recipeOverviewCard.findViewById<ImageView>(R.id.recipe_image);

                recipeName.text = recipe.name;
                //recipeIngredients.text = getIngredientsOverview(recipe.ingredients);
                recipeEstimatedTime.text = recipe.metadata?.minutes_to_cook.toString();
                Picasso.get().load(recipe.metadata?.image_url).into(recipeImage)

                // add the recipe overview card to the layout.
                binding.recipeScrollHost.addView(recipeOverviewCard)

                binding.recipeScrollHost.setOnClickListener{
                    showRecipePopup(recipe)
                }
            }
        }

        return binding.root
    }

    // Request to add recipes to the recipeViewModel.
    // The information is hardcoded which will be later provided by the search engine.
    // As such the workflow must be moved to the server/interface side. And must not be kept in client side.
    fun createRecipes(recipeViewModel: RecipeViewModel) {
        recipeViewModel.addRecipe("LAMB BIRYANI", listOf("plain yogurt", "skinless chicken pieces",  "basmati rice", "vegetable oil" ), "2 hrs 15 mins", "https://food.fnr.sndimg.com/content/dam/images/food/fullset/2022/07/27/0/YAHI_Dum-Aloo-Biryani_s4x3.jpg.rend.hgtvcom.826.620.suffix/1658954351318.jpeg");
        recipeViewModel.addRecipe("PHỞ BÒ", listOf("Beef brisket", "lb beef shank", "cooked rice noodles" ), "8 hrs 25 mins", "https://food.fnr.sndimg.com/content/dam/images/food/fullset/2018/11/30/0/FNK_Instant-Pot-Beef-Pho-H_s4x3.jpg.rend.hgtvcom.826.620.suffix/1548176890147.jpeg");
        recipeViewModel.addRecipe("TONKOTSU RAMEN", listOf("chicken carcass", "pork ribs",  "dried shiitake mushrooms"), "20 hrs 45 mins", "https://food.fnr.sndimg.com/content/dam/images/food/fullset/2018/4/3/0/LS-Library_Kimchi-and-Bacon-Ramen_s4x3.jpg.rend.hgtvcom.826.620.suffix/1522778330680.jpeg");
    }

    fun showRecipePopup(recipeSelected: Recipe) {


        println("Show recipe popup called")
        val popUpCardView = layoutInflater.inflate(R.layout.recipe_popup,
            null)
        //popUpCardView.

        val title = popUpCardView.findViewById<TextView>(R.id.title)

        title.text = recipeSelected.name

        val time = popUpCardView.findViewById<TextView>(R.id.time)

        time.text = recipeSelected.metadata?.minutes_to_cook.toString()

        val ingredientsList = popUpCardView.findViewById<TextView>(R.id.ingredients)

        ingredientsList.text = "Ingredients: \n"
        recipeSelected.ingredients.forEach{
            ingredientsList.append(it + "\n")
        }

        val directionsList = popUpCardView.findViewById<TextView>(R.id.directions)

        directionsList.text = "Directions: \n"
        recipeSelected.steps.forEach{
            directionsList.append(it + "\n")
        }


        popUpCardView.focusable = View.FOCUSABLE

        val window = PopupWindow(popUpCardView, 1000, 1500)

        window.showAtLocation(view, Gravity.CENTER, 0, 50)

        var close: Button = popUpCardView.findViewById(R.id.closeButton);
        close.setOnClickListener {

            window.dismiss()
        };

    }

    // We want to only display a number of ingredients in the recipe overview card. As such
    // the function only selects a portion of ingredients to display.
    fun getIngredientsOverview(ingredients: Array<Recipe.Ingredient>) : String {
        val ingredientsDisplayThreshold = 3;
        var ingredientsDisplay = ""
        val numOfIngredients = ingredients.size;

        var ingredientDisplaySize = 0;

        ingredientDisplaySize = if (numOfIngredients >= ingredientsDisplayThreshold) {
            ingredientsDisplayThreshold
        } else {
            numOfIngredients
        }

        val ingredientDisplayList = ingredients.take(ingredientDisplaySize)

        var isFirstIngredient = true;

        for (ingredient in ingredientDisplayList) {
            if (isFirstIngredient) {
                ingredientsDisplay = ingredient.name.toString();
                isFirstIngredient = !isFirstIngredient;
            } else {
                ingredientsDisplay += ", $ingredient"
            }
        }

        return ingredientsDisplay
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recipeViewModel = ViewModelProvider(requireActivity())[RecipeViewModel::class.java]

        // adding listener to search bar to catch events such as query submit and change.
        binding.recipeSearchField.editText?.addTextChangedListener {
            recipeViewModel.queryRecipes(it.toString());
        }





        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}