package com.example.mealplanner

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.TextView
import android.widget.Toast


class SearchMealActivity : AppCompatActivity() {
    private lateinit var searchForMealBtn: Button
    private lateinit var mealDao: MealDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_for_meal_main)

        // Initialize the MealDao
        val mealDatabase = MealDatabase.getInstance(this)
        mealDao = mealDatabase.mealDao()

        // Set onClickListener for the button
        searchForMealBtn = findViewById(R.id.retrieve_meals_btn)
        searchForMealBtn.setOnClickListener { searchForMeal() }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save any relevant data to the outState Bundle
        outState.putString("searchText", findViewById<TextInputEditText>(R.id.search_meal_ingredient_edit).text.toString())
        outState.putString("searchResultsText", findViewById<TextView>(R.id.search_for_textView).text.toString())
        outState.putString("nameText", findViewById<TextView>(R.id.meal_name_textView).text.toString())
        outState.putString("categoryText", findViewById<TextView>(R.id.meal_category_textView).text.toString())
        outState.putString("areaText", findViewById<TextView>(R.id.meal_area_textView).text.toString())

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
//        Restore the saved data from the savedInstanceState Bundle

        val searchText = savedInstanceState.getString("searchText")
        findViewById<TextInputEditText>(R.id.search_meal_ingredient_edit).setText(searchText)

        val searchResultsText = savedInstanceState.getString("searchResultsText")
        findViewById<TextView>(R.id.search_for_textView).setText(searchResultsText)

        val mealsLayout = findViewById<LinearLayout>(R.id.meals_layout)

        val nameText = savedInstanceState.getString("nameText")
        mealsLayout.findViewById<TextView>(R.id.meal_name_textView).setText(nameText)

//        mealNameTextView?.text = nameText

        val categoryText = savedInstanceState.getString("categoryText")
        mealsLayout.findViewById<TextView>(R.id.meal_category_textView).setText(categoryText)

        val areaText = savedInstanceState.getString("areaText")
        mealsLayout.findViewById<TextView>(R.id.meal_area_textView).setText(areaText)

    }



    private fun searchForMeal() {
        try {
            val searchValue = findViewById<TextInputEditText>(R.id.search_meal_ingredient_edit).text.toString()

            // Launch a coroutine on Main dispatcher
            CoroutineScope(Dispatchers.Main).launch {
                // Collect the search results from the Flow
                mealDao.searchMeals(searchValue).collect { mealsList ->
                    val mealsLayout = findViewById<LinearLayout>(R.id.meals_layout)
                    mealsLayout.removeAllViews()

                    for (meal in mealsList) {
                        val mealLayout = LayoutInflater.from(this@SearchMealActivity).inflate(R.layout.meal_item, mealsLayout, false) as LinearLayout

                        val mealNameTextView = mealLayout.findViewById<TextView>(R.id.meal_name_textView)
                        mealNameTextView.text = meal.mealName

                        val mealCategoryTextView = mealLayout.findViewById<TextView>(R.id.meal_category_textView)
                        mealCategoryTextView.text = "Category: ${meal.category}"

                        val mealAreaTextView = mealLayout.findViewById<TextView>(R.id.meal_area_textView)
                        mealAreaTextView.text = "Area: ${meal.area}"


                        val mealImageView = mealLayout.findViewById<ImageView>(R.id.meal_imageView)
                        Glide.with(this@SearchMealActivity).load(meal.mealThumb).into(mealImageView)

                        mealsLayout.addView(mealLayout)
                    }
                    // Update the UI with the search results
                    findViewById<TextView>(R.id.search_for_textView).text = "Search Results For $searchValue"

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "No meals found for the given word", Toast.LENGTH_SHORT).show()
        }
    }
}

//                    findViewById<TextView>(R.id.search_results_textView).text = ""
//        outState.putString("mealImage", findViewById<ImageView>(R.id.meal_imageView).tag as? String)
//        val mealImage = savedInstanceState.getString("mealImage")
//        val mealImageView = findViewById<ImageView>(R.id.meal_imageView)
//        if (mealImage != null) {
//            mealImageView?.setImageURI(Uri.parse(mealImage))
//        }
