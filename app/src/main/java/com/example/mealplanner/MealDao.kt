package com.example.mealplanner

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao // To mark the interface as a Data Access Object
interface MealDao {
    @Insert
    fun insert(meal: Meal)

    @Insert
    fun insertAll(mealsList: List<Meal>)

    @Query("SELECT *, image_source FROM meals_table WHERE meal_name LIKE '%' || :query || '%' COLLATE NOCASE OR ingredients LIKE '%' || :query || '%' COLLATE NOCASE")
    fun searchMeals(query: String): Flow<List<Meal>>

    @Query("DELETE FROM meals_table")
    fun deleteAll()


}