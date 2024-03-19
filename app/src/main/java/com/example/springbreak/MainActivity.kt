package com.example.springbreak

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private val languageCitiesMap = mapOf(
        "English" to listOf("London", "Boston"),
        "French" to listOf("Paris", "Montreal"),
        "Chinese" to listOf("Beijing", "Shanghai")
    )
    private lateinit var listView: ListView
    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById<ListView>(R.id.listView)
        editText = findViewById<EditText>(R.id.editText)

        listView.adapter =        ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            languageCitiesMap.keys.toList()
        )

        listView.choiceMode = ListView.CHOICE_MODE_SINGLE

        listView.setOnItemClickListener { _, _, position, _ ->
            val language = listView.getItemAtPosition(position) as String
            val cities = languageCitiesMap[language]
            val selectedCity = cities?.get(Random.nextInt(cities.size)) ?: ""

            editText?.setText("$language - $selectedCity")
        }

    }
}