package com.example.springbreak

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.LANG_AVAILABLE
import android.widget.Toast
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener, TextToSpeech.OnInitListener {

    private val languageCitiesMap = mapOf(
        "English" to listOf("London", "Boston"),
        "French" to listOf("Paris", "Montreal"),
        "Chinese" to listOf("Beijing", "Shanghai")
    )

    private val languageCodeMap = mapOf(
        "English" to "en",
        "French" to "fr",
        "Chinese" to "zh"
    )
    private lateinit var listView: ListView
    private lateinit var editText: EditText

    private val speechRequestCode = 0

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var lastUpdate: Long = 0
    private var lastX: Float = 0.0f
    private var lastY: Float = 0.0f
    private var lastZ: Float = 0.0f
    private val shakeThreshold = 800

    private var currentLanguage: String = "English"
    private lateinit var textToSpeech: TextToSpeech
    private var greetingSpoken: Boolean = false
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
            val langCode = languageCodeMap[language] ?: Locale.getDefault().toLanguageTag()
            val cities = languageCitiesMap[language]
            val selectedCity = cities?.get(Random.nextInt(cities.size)) ?: ""

            greetingSpoken = false

            currentLanguage = language
            startSpeechToText(langCode)

            //editText.setText("$language - $selectedCity")
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Register the sensor listener
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }

        textToSpeech = TextToSpeech(this, this)


    }

    private fun startSpeechToText(selectedLanguage: String) {

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak now")
        }
        try {
            startActivityForResult(intent, speechRequestCode)
        } catch (e: Exception) {
            Toast.makeText(this, "Your device does not support Speech Input", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == speechRequestCode && resultCode == RESULT_OK) {
            val spokenText: String? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                    results[0]
                }

            spokenText?.let {
                editText.setText(it)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastUpdate) > 100) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = sqrt((x - lastX).pow(2) + (y - lastY).pow(2) + (z - lastZ).pow(2)) / (currentTime - lastUpdate) * 10000
            if (speed > shakeThreshold) {
                Toast.makeText(this, "shake!!!", Toast.LENGTH_SHORT).show()
                sayHello()
                launchGoogleMapsToCity(currentLanguage)
            }

            lastUpdate = currentTime
            lastX = x
            lastY = y
            lastZ = z
        }
    }

    private fun launchGoogleMapsToCity(selectedLanguage: String) {
        val cities = languageCitiesMap[selectedLanguage]
        val selectedCity = cities?.randomOrNull() ?: return

        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(selectedCity)}")

        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        startActivity(mapIntent)

    }

    private fun sayHello() {
        if (::textToSpeech.isInitialized) { // This checks if textToSpeech has been initialized
            currentLanguage.let { languageCode ->
                val greeting = when (languageCode) {
                    "English" -> "Hello"
                    "French" -> "Bonjour"
                    "Chinese" -> "你好"
                    else -> "Hello"
                }

                if (!greetingSpoken) {
                    textToSpeech.speak(greeting, TextToSpeech.QUEUE_FLUSH, null, "")
                    greetingSpoken = true
                }
            }
        } else {
            Toast.makeText(this, "Text to Speech not initialized", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //??
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locale = Locale(currentLanguage)
            textToSpeech.setLanguage(locale)
        }
    }
}
