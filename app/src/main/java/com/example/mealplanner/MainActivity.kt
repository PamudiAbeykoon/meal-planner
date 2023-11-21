package com.example.mealplanner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var mealDao: MealDao
    private lateinit var addMealsBtn: Button
    private lateinit var searchMealsByIngredientBtn: Button
    private lateinit var searchForMealsBtn: Button
    private lateinit var searchBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the buttons
        addMealsBtn = findViewById(R.id.add_meals_to_db_btn)
        searchMealsByIngredientBtn = findViewById(R.id.search_meals_by_ingredient_btn)
        searchForMealsBtn = findViewById(R.id.search_for_meals_btn)
        searchBtn = findViewById(R.id.search_btn)

        // Set onClickListener for the buttons
        addMealsBtn.setOnClickListener { addMealsToDB() }
        searchMealsByIngredientBtn.setOnClickListener { searchMealsByIngredient() }
        searchForMealsBtn.setOnClickListener { searchForMeals() }
        searchBtn.setOnClickListener { searchMeals() }

        // Initialize the MealDao
        val mealDatabase = MealDatabase.getInstance(this)
        mealDao = mealDatabase.mealDao()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save any relevant data to the outState Bundle
        outState.putString("mealText", findViewById<TextInputEditText>(R.id.search_meal_edit).text.toString())
        outState.putString("mealsText", findViewById<TextView>(R.id.search_results).text.toString())
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore the saved data from the savedInstanceState Bundle
        val mealText = savedInstanceState.getString("mealText")
        findViewById<TextInputEditText>(R.id.search_meal_edit).setText(mealText)

        val mealsText = savedInstanceState.getString("mealsText")
        findViewById<TextView>(R.id.search_results).setText(mealsText)
    }


    private fun addMealsToDB() {
        // Launch a coroutine on IO dispatcher
        CoroutineScope(Dispatchers.IO).launch {
            // Create 3 meal objects
            val meal1 = Meal(
                mealName = "Sweet and Sour Pork",
                drinkAlternate = null,
                category = "Pork",
                area = "Chinese",
                ingredients = listOf("Pork(200g)","Egg(1)","Water(dash)","Salt(1/2tsp)","Sugar(1tsp)","Soy Sauce(10g)","Starch(10g)","Tomato Puree(30g)","Vinegar(10g)","Coriander(dash)"),
                measures = listOf("200g","1","dash","1/2tsp","1tsp","10g","10g","30g","10g","dash"),
                instructions = "Crack the egg into a bowl. Separate the egg white and yolk. Slice the pork tenderloin into ips. Prepare the marinade using a pinch of salt, one teaspoon of starch, two teaspoons of light soy sauce, and an egg white. Marinade the pork ips for about 20 minutes. Put the remaining starch in a bowl. Add some water and vinegar to make a starchy sauce. Pour the cooking oil into a wok and heat. Add the marinated pork ips and fry them until they turn brown. Remove the cooked pork from the wok and place on a plate. Leave some oil in the wok. Put the tomato sauce and white sugar into the wok, and heat until the oil and sauce are fully combined. Add some water to the wok and thoroughly heat the sweet and sour sauce before adding the pork ips to it. Pour in the starchy sauce. Stir-fry all the ingredients until the pork and sauce are thoroughly mixed together. Serve on a plate and add some coriander for decoration.",
                mealThumb = "https://www.themealdb.com/images/media/meals/1529442316.jpg",
                tags = "Sweet",
                youtube = "https://www.youtube.com/watch?v=mdaBIhgEAMo",
                source = null,
                imageSource = null,
                dateModified = null
            )
            val meal2 = Meal(
                mealName = "Chicken Marengo",
                drinkAlternate = null,
                category = "Chicken",
                area = "French",
                ingredients = listOf("Olive Oil(1tbs)","Mushrooms(300g)","Chicken Legs(4)","Pasta(500g)","Chicken Stoke Cube(1)","Black Olives(1)","Parsley(Chopped)","Harisa Spice(1tbs)"),
                measures = listOf("200g","1","dash","1/2tsp","1tsp","10g","10g","30g","10g","dash"),
                instructions = "Heat the oil in a large flameproof casserole dish and stir-fry the mushrooms until they start to soften. Add the chicken legs and cook briefly on each side to colour them a little. Pour in the passata, crumble in the stock cube and stir in the olives. Season with black pepper and you should not need salt. Cover and simmer for 40 mins until the chicken is tender. Sprinkle with parsley and serve with pasta and a salad, or mash and green veg, if you like." ,
                mealThumb = "https://www.themealdb.com/images/media/meals/qpxvuq1511798906.jpg",
                tags = null,
                youtube = "https://www.youtube.com/watch?v=U33HYUr-0Fw",
                source = "https://www.bbcgoodfood.com/recipes/3146682/chicken-marengo",
                imageSource = null,
                dateModified = null
            )
            val meal3 = Meal(
                mealName = "Leblebi Soup",
                drinkAlternate = null,
                category = "Vegetarian",
                area = "Tunisian",
                ingredients = listOf("Olive Oil(2tbs)","Onion(1 medium finely diced)","Chickpeas(250g)","Vegetable Stock(1.5L)","Cumin(1tsp)","Garlic(5 cloves)","Salt(1/2 tsp","Harissa Spice(1 tsp)","Pepper(Pinch)","Lime(1/2 tsp)"),
                measures = listOf("200g","1","dash","1/2tsp","1tsp","10g","10g","30g","10g","dash"),
                instructions = "Heat the oil in a large pot. Add the onion and cook until translucent.Drain the soaked chickpeas and add them to the pot together with the vegetable stock. Bring to the boil, then reduce the heat and cover. Simmer for 30 minutes.In the meantime toast the cumin in a small ungreased frying pan, then grind them in a mortar. Add the garlic and salt and pound to a fine paste. Add the paste and the harissa to the soup and simmer until the chickpeas are tender, about 30 minutes. Season to taste with salt, pepper and lemon juice and serve hot.",
                mealThumb = "https://www.themealdb.com/images/media/meals/qpxvuq1511798906.jpg",
                tags = "Soup",
                youtube = "https://www.youtube.com/watch?v=BgRifcCwinY",
                source = "http://allrecipes.co.uk/recipe/43419/leblebi--tunisian-chickpea-soup-.aspx",
                imageSource = null,
                dateModified = null
            )

            // Insert meals to meals_table
            mealDao.insert(meal1)
            mealDao.insert(meal2)
            mealDao.insert(meal3)
        }
        Toast.makeText(this, "Meals added to the database", Toast.LENGTH_SHORT).show()
    }

    private fun searchMealsByIngredient() {
        // Navigate to SearchByIngredientActivity
        val intent = Intent(applicationContext, SearchByIngredientActivity::class.java)
        startActivity(intent)
    }

    private fun searchForMeals() {
        // Navigate to SearchMealActivity
        val intent = Intent(applicationContext, SearchMealActivity::class.java)
        startActivity(intent)
    }

    private fun searchMeals() {
        // Launch a coroutine on IO dispatcher to perform network request asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            val mealText = findViewById<TextInputEditText>(R.id.search_meal_edit).text.toString()
            val meals = retrieveMealsFromWebService(mealText)

            // Switch to the Main dispatcher to update the UI with the search results
            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.search_results).text = meals.toString()
            }
        }
    }

    private fun retrieveMealsFromWebService(mealText: String): String {
        try {
            // Make HTTP request to the web service
            val url = URL("https://www.themealdb.com/api/json/v1/1/search.php?s=$mealText")
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
                    val mealName = meal.getString("strMeal")
                    mealDetails.append("$mealName\n")
                }

                // Return meal details as a formatted string line by line
                return mealDetails.toString().trimEnd() // trim any trailing whitespaces
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
}

//    private fun deleteData() {
//        // Launch a coroutine on the Main dispatcher
//        CoroutineScope(Dispatchers.IO).launch {
//            mealDao.deleteAll()
//        }
//    }
//        addMealsBtn.setOnClickListener { deleteData() }