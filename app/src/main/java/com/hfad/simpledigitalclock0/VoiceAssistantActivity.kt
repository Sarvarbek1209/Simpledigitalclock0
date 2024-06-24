 package com.hfad.simpledigitalclock0

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.regex.Pattern
import android.icu.util.Calendar
import com.hfad.simpledigitalclock0.AlarmReceiver
import com.hfad.simpledigitalclock0.R


class VoiceAssistantActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var speechRecohnizer: SpeechRecognizer
    private lateinit var textToSpeech: TextToSpeech
    private val alarmRegex = Pattern.compile("(\\d{1,2}):(\\d{2})", Pattern.CASE_INSENSITIVE)
    private val RECORD_AUDIO_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_assistant)

        initializeSpeechRecognizer()
        initializeTextToSpeech()
        initialize()


    }
    //Инициализируем SpeechRecognizer
    private fun initializeSpeechRecognizer() {
        speechRecohnizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecohnizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onResults(results: Bundle?) {
                runOnUiThread {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.let {
                        for (result in it) {
                            if (result.equals("Привет", ignoreCase = true)) {
                                respond(getString(R.string.hello_response))
                                break

                            }else{processVoiceCommand(result)}
                        }
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}

        })

        startListening()
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(this, this)
    }

    private fun startListening(){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecohnizer.startListening(intent)
    }
    private fun respond(message: String) {
        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }


    override fun onInit(status:Int){
        if (status == TextToSpeech.SUCCESS) {
            // Устанавливаем язык синтеза речи такой же, как и язык устройства
            val result = textToSpeech.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Язык не поддерживается или отсутствуют данные")
                Toast.makeText(this, "Язык не поддерживается", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("TTS", "Язык не поддерживается или отсутствуют данные")
            Toast.makeText(this, "Не удалось инициализировать TextToSpeech", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun processVoiceCommand(command: String) {
        Toast.makeText(this, "command received: $command", Toast.LENGTH_SHORT).show()
        // Обработка голосовых команд

        if (command.lowercase(Locale.getDefault()) == "hello!") {
            respond(getString(R.string.hello_response))
        }

        val normalizedCommand = command.lowercase(Locale.getDefault())


        if (normalizedCommand.contains("установи будильник на") || normalizedCommand.contains("set the alarm for")) {

            val timeRegex = Regex("""(\d{1,2}):(\d{2})""")
            val matchResult = timeRegex.find(normalizedCommand)
            if (matchResult != null) {
                val (hourStr, minuteStr) = matchResult.destructured
                val hour = hourStr.toIntOrNull() ?: 0
                val minute = minuteStr.toIntOrNull() ?: 0
                setAlarmExact(hour, minute)
                val alarmSetMessage = getString(R.string.alarm_set_message, hour, minute)
                respond(alarmSetMessage)

                // Переход в MainActivity после установки будильника
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("imageView", true)
                startActivity(intent)


            } else {
                respond(getString(R.string.unknown_command))
            }
        }
    }


    private fun setAlarmExact(hour:Int, minute:Int) {
        val context = applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = "android.intent.action.ALARM_RECEIVER"
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }


        // Создание календаря для установки времени будильника
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            // Проверяем, если выбранное время уже прошло сегодня,
            // то устанавливаем будильник на следующий день
            if (this.before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            alarmIntent
        )

        val message = getString(R.string.alarm_set_message, hour, minute)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_AUDIO_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Разрешение на запись аудио получено, можно начать прослушивание голосовых команд
                    startListening()
                } else {
                    // Разрешение не получено, вы можете обработать это здесь или уведомить пользователя
                    Toast.makeText(this, "Разрешение на запись аудио не было предоставлено", Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun initialize() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this as Activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_REQUEST_CODE
            )
        } else {
            // Разрешение уже предоставлено, можно начинать прослушивание голосовых команд
            startListening()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        startListening()
        speechRecohnizer.destroy()
        // Освобождаем ресурсы TextToSpeech при уничтожении активити
        textToSpeech.stop()
        textToSpeech.shutdown()
    }


}
