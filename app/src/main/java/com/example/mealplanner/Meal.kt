package com.example.mealplanner

import androidx.room.*

@Entity(tableName = "meals_table")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(name = "meal_name") val mealName: String,
    @ColumnInfo(name = "drink_alternate") val drinkAlternate: String?,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "area") val area: String,
    @TypeConverters(ListStringConverter::class)
    @ColumnInfo(name = "ingredients") val ingredients: List<String>,
    @ColumnInfo(name = "measures") val measures: List<String>,
    @ColumnInfo(name = "instructions") val instructions: String,
    @ColumnInfo(name = "meal_thumb") val mealThumb: String,
    @ColumnInfo(name = "tags") val tags: String?,
    @ColumnInfo(name = "youtube") val youtube: String?,
    @ColumnInfo(name = "source") val source: String?,
    @ColumnInfo(name = "image_source") val imageSource: String?,
    @ColumnInfo(name = "date_modified") val dateModified: String?
)

class ListStringConverter {
    @TypeConverter
    fun fromListString(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toListString(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }
    }
}