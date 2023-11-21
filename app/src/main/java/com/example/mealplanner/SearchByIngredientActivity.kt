package com.example.mealplanner

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SearchByIngredientActivity : AppCompatActivity() {
    private lateinit var retrieveMealsBtn: Button
    private lateinit var saveMealsBtn: Button
    private lateinit var mealDao: MealDao
    private val mealsList = mutableListOf<Meal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_by_ingredient_main)

        // Initialize the buttons
        retrieveMealsBtn = findViewById(R.id.retrieve_meals_btn)
        saveMealsBtn = findViewById(R.id.save_meals_to_db_btn)

        // Set onClickListener for the buttons
        retrieveMealsBtn.setOnClickListener { retrieveMeals() }
        saveMealsBtn.setOnClickListener { saveMealsToDB(mealsList) }

        //Initialize the MealDao
        val mealDatabase = MealDatabase.getInstance(this)
        mealDao = mealDatabase.mealDao()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save any relevant data to the outState Bundle
        outState.putString("ingredientText", findViewById<TextInputEditText>(R.id.search_meal_ingredient_edit).text.toString())
        outState.putString("searchForText", findViewById<TextView>(R.id.search_for_textView).text.toString())
        outState.putString("retrievedText", findViewById<TextView>(R.id.retrieved_meals_textView).text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore the saved data from the savedInstanceState Bundle

        val ingredientText = savedInstanceState.getString("ingredientText")
        findViewById<TextInputEditText>(R.id.search_meal_ingredient_edit).setText(ingredientText)

        val searchForText = savedInstanceState.getString("searchForText")
        findViewById<TextView>(R.id.search_for_textView).setText(searchForText)

        val retrievedText = savedInstanceState.getString("retrievedText")
        findViewById<TextView>(R.id.retrieved_meals_textView).setText(retrievedText)
    }

    private fun retrieveMeals() {
        // Launch a coroutine on IO dispatcher
        CoroutineScope(Dispatchers.IO).launch {
            // Retrieve meals from the web service
            val ingredient = findViewById<TextInputEditText>(R.id.search_meal_ingredient_edit).text.toString()
            val meals = retrieveMealsFromWebService(ingredient)

            // Switch to the Main dispatcher
            withContext(Dispatchers.Main) {
                // Update the UI components with the search results
                findViewById<TextView>(R.id.search_for_textView).text = "Meals with $ingredient"
                findViewById<TextView>(R.id.retrieved_meals_textView).text = meals
            }
        }
    }

    private suspend fun retrieveMealsFromWebService(ingredient: String): String {
        val mealsList = mutableListOf<String>() // List to store meal details

        try {
            // Make HTTP request to the web service
            val url = URL("https://www.themealdb.com/api/json/v1/1/filter.php?i=$ingredient")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            // Check the response code
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Parse JSON response
                val inputStream = connection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var inputLine: String?
                while (bufferedReader.readLine().also { inputLine = it } != null) {
                    response.append(inputLine)
                }
                bufferedReader.close()

                // Extract meal details from response JSON
                val jsonResponse = JSONObject(response.toString())
                val mealsArray = jsonResponse.getJSONArray("meals")

                for (i in 0 until mealsArray.length()) {
                    val meal = mealsArray.getJSONObject(i)
                    val mealName = meal.getString("strMeal")
                    val mealDetails = retrieveMealDetailsFromWebService(mealName)
                    mealsList.add(mealDetails)
                }
            } else {
                // Handle non-OK response code
                return "Error: Response code $responseCode"
            }
        } catch (e: Exception) {
            // Handle any exceptions that occurred during the request
            e.printStackTrace()
            return "No meals found for the given ingredient"
        }
        // Return the list of meal details as a formatted string
        return mealsList.joinToString("\n\n\n")
    }

    private fun retrieveMealDetailsFromWebService(meal: String): String {
        try {
            // Make HTTP request to the web service
            val url = URL("https://www.themealdb.com/api/json/v1/1/search.php?s=$meal")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            // Check the response code
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Parse JSON response
                val inputStream = connection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var inputLine: String?
                while (bufferedReader.readLine().also { inputLine = it } != null) {
                    response.append(inputLine)
                }
                bufferedReader.close()

                // Extract meal details from response JSON
                val jsonResponse = JSONObject(response.toString())
                val mealsArray = jsonResponse.getJSONArray("meals")
                val mealDetails = StringBuilder()

                for (i in 0 until mealsArray.length()) {
                    val meal = mealsArray.getJSONObject(i)

                    // Extract individual meal details
                    val mealName = meal.getString("strMeal")
                    val drinkAlternate = meal.getString("strDrinkAlternate")
                    val category = meal.getString("strCategory")
                    val area = meal.getString("strArea")
                    val instructions = meal.getString("strInstructions")
                    val mealThumb = meal.getString("strMealThumb")
                    val tags = meal.getString("strTags")
                    val youtube = meal.getString("strYoutube")
                    val source = meal.getString("strSource")
                    val imageSource = meal.getString("strImageSource")
                    val dateModified = meal.getString("dateModified")

                    // Adding Ingredients and Measures
                    val ingredientsList = mutableListOf<String>()
                    val measuresList = mutableListOf<String>()
                    for (j in 1..20) {
                        val ingredient = meal.getString("strIngredient$j")
                        if (!ingredient.isNullOrBlank()) {
                            ingredientsList.add(ingredient)
                            val measure = meal.getString("strMeasure$j")
                            measuresList.add(measure)
                        }
                    }

                    // Create a Meal object to store the retrieved details
                    val mealObject = Meal(
                        mealName = mealName,
                        drinkAlternate = drinkAlternate,
                        category = category,
                        area = area,
                        ingredients = ingredientsList,
                        measures = measuresList,
                        instructions = instructions,
                        mealThumb = mealThumb,
                        tags = tags,
                        youtube = youtube,
                        source = source,
                        imageSource = imageSource,
                        dateModified = dateModified
                    )
                    mealsList.add(mealObject)

                    // Display retrieved meals details
                    // Append the retrieved meal details to the mealDetails StringBuilder
                    mealDetails.append("Meal Name: $mealName\n")
                    mealDetails.append("DrinkAlternate: $drinkAlternate\n")
                    mealDetails.append("Category: $category\n")
                    mealDetails.append("Area: $area\n")
                    mealDetails.append("Instructions:\n $instructions\n")
                    mealDetails.append("Tags: $tags\n")
                    mealDetails.append("Youtube : $youtube\n")

                    // Adding Ingredients and Measures
                    mealDetails.append("Ingredients:\n")
                    for (j in 0 until ingredientsList.size) {
                        val ingredient = ingredientsList[j]
                        val measure = measuresList[j]
                        mealDetails.append("    $ingredient: $measure\n")
                    }

                    mealDetails.append("\n\n\n")
                }

                // Return meal details as a formatted string line by line
                return mealDetails.toString().trimEnd() // Trim any trailing whitespaces
            } else {
                // Handle non-OK response code
                return "Error: Response code $responseCode"
            }
        } catch (e: Exception) {
            // Handle any exceptions that occurred during the request
            e.printStackTrace()
            return "No meals found for the given ingredient"
        }
    }

    private fun saveMealsToDB(mealsList: List<Meal>) {
        try {
            // Launch a coroutine on IO dispatcher
            CoroutineScope(Dispatchers.IO).launch {
                mealDao.insertAll(mealsList)
            }
            Toast.makeText(this, "Meals Saved to DB", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Handle any exceptions that occurred during the database operation
            e.printStackTrace()
            Toast.makeText(this, "Error saving meals to DB", Toast.LENGTH_SHORT).show()
        }
    }
}