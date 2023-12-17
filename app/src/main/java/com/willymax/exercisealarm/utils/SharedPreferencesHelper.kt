package com.willymax.exercisealarm.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.willymax.exercisealarm.alarm.AlarmItem

class SharedPreferencesHelper(
    private val context: Context,
    private val sharedPreferencesName: String
) {
    private val sharedPreferences =
        context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)

    private fun saveList(key: String, list: List<AlarmItem>) {
        val gson = Gson()
        val json = gson.toJson(list)
        sharedPreferences.edit().putString(key, json).apply()
    }

    fun retrieveList(key: String): List<AlarmItem> {
        val gson = Gson()
        val json = sharedPreferences.getString(key, null)

        return if (json != null) {
            val type = object : TypeToken<List<AlarmItem>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
    fun updateList(key: String, updateFunction: (List<AlarmItem>) -> List<AlarmItem>) {
        val currentList = retrieveList(key)
        val updateList = updateFunction(currentList)
        saveList(key, updateList)
    }

}