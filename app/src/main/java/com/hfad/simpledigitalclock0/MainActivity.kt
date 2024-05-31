package com.hfad.simpledigitalclock0

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.media.RingtoneManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var dataTextClock: TextClock
    private lateinit var weekTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var isDataTextClockVisible = true
    private val interval: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dataTextClock = findViewById(R.id.dataTextClock)
        weekTextView = findViewById(R.id.week)




        startClockToggle()

        val imageButton = findViewById<ImageButton>(R.id.imageButton)
        imageButton.setOnClickListener {
            showTimePickerDialog()
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()
    }

    private fun hideSystemUI() {
        window.decorView.apply {
            // Скрыть панель навигации и строку состояния
            systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            setOnApplyWindowInsetsListener { _, insets ->
                val newInsets = insets.consumeSystemWindowInsets()
                newInsets
            }
        }

    }

    fun changeClockColor(view: View) {
        val textClock = view as TextClock
        val textClock1 = findViewById<TextClock>(R.id.textClock)
        val dateTextClock = findViewById<TextClock>(R.id.dataTextClock)
        val week = findViewById<TextView>(R.id.week)
        val topLine = findViewById<View>(R.id.topLine)
        val bottomLine = findViewById<View>(R.id.bottomLine)
        val leftLine = findViewById<View>(R.id.leftLine)
        val rightLine = findViewById<View>(R.id.rightLine)



        // Создаем случайный цвет
        val randomColor =
            Color.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

        // Устанавливаем случайный цвет текста для часов
        textClock.setTextColor(randomColor)
        textClock1.setTextColor(randomColor)
        dateTextClock.setTextColor(randomColor)
        week.setTextColor(randomColor)
        topLine.setBackgroundColor(randomColor)
        bottomLine.setBackgroundColor(randomColor)
        leftLine.setBackgroundColor(randomColor)
        rightLine.setBackgroundColor(randomColor)
    }

    private fun setAlarmExact(hour: Int, minute: Int) {

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
                add(Calendar.DATE, 1)}
        }

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            alarmIntent
        )


        Toast.makeText(
            context,
            "Будильник установлен на $hour:$minute",
            Toast.LENGTH_SHORT
        ).show()

    }
    private fun showTimePickerDialog() {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                // Вызываем метод установки будильника при выборе времени
                setAlarmExact(selectedHour, selectedMinute)
            },
            hour,
            minute,
            true,
        )
        timePickerDialog.show()
    }
    private fun startClockToggle(){
        handler.postDelayed(object : Runnable{
            override fun run(){
                if (isDataTextClockVisible){
                    weekTextView.visibility = View.GONE
                    dataTextClock.visibility = View.VISIBLE
                }else{
                    weekTextView.visibility = View.VISIBLE
                    dataTextClock.visibility = View.GONE

                }
                isDataTextClockVisible = !isDataTextClockVisible
                handler.postDelayed(this,interval)
            }
        },interval)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }


}









