 package com.hfad.simpledigitalclock0

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hfad.simpledigitalclock0.AlarmReceiver
import com.hfad.simpledigitalclock0.R
import com.hfad.simpledigitalclock0.databinding.ActivityMainBinding
import kotlin.random.Random




class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var dateTextClock: TextClock
    private lateinit var weekTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var isDataTextClockVisible = true
    private val interval: Long = 3000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityMainBinding.inflate(layoutInflater)
        val view =binding.root
        setContentView(view)

        weekTextView = binding.week
        dateTextClock = binding.dateTextClock

        startClockToggle()

        binding.imageButton.setOnClickListener {
            showTimePickerDialog()
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()




        // Получите текущую дату
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Преобразуйте числовое значение дня недели в текстовое представление

        val dayOfWeekString = when (dayOfWeek) {
            Calendar.SUNDAY -> getString(R.string.sunday)
            Calendar.MONDAY -> getString(R.string.monday)
            Calendar.TUESDAY -> getString(R.string.tuesday)
            Calendar.WEDNESDAY -> getString(R.string.wednesday)
            Calendar.THURSDAY -> getString(R.string.thursday)
            Calendar.FRIDAY -> getString(R.string.friday)
            Calendar.SATURDAY -> getString(R.string.saturday)
            else -> "Unknown"
        }
        // Установите текст в TextView
        weekTextView.text = dayOfWeekString



        binding.microphoneButton.setOnClickListener {
            val voiceAssistantIntent = Intent(this, VoiceAssistantActivity::class.java)
            startActivity(voiceAssistantIntent)
        }


        when {
            intent.getBooleanExtra("imageView", false) -> binding.imageView.visibility = View.VISIBLE
            intent.getBooleanExtra("imageView1", false) -> binding.imageView.visibility = View.GONE
        }
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
        // Создаем случайный цвет
        val randomColor =
            Color.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

        // Устанавливаем случайный цвет текста для часов
        textClock.setTextColor(randomColor)
        binding.dateTextClock.setTextColor(randomColor)
        binding.topLine.setBackgroundColor(randomColor)
        binding.bottomLine.setBackgroundColor(randomColor)
        binding.leftLine.setBackgroundColor(randomColor)
        binding.rightLine.setBackgroundColor(randomColor)
        binding.week.setTextColor(randomColor)
    }

    private fun showTimePickerDialog() {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val isPM = selectedHour >=12
                val hourIn12Format = if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour
                setAlarmExact( hourIn12Format,selectedMinute, isPM)
                // Здесь устанавливаем видимость ImageView
                binding.imageView.visibility = View.VISIBLE
            },
            hour, // Начальное значение часов
            minute, // Начальное значение минут
            true // Использовать ли 24-часовой формат
        )

        timePickerDialog.show()
    }


    private fun setAlarmExact(hour: Int, minute: Int, isPM:Boolean) {

        val context = applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = "android.intent.action.ALARM_RECEIVER"
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        // Создание календаря для установки времени будильника
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY,if (isPM && hour < 12)hour + 12 else hour)
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
    private fun startClockToggle(){
        handler.postDelayed(object : Runnable{
            override fun run(){
                if (isDataTextClockVisible){
                    weekTextView.visibility = View.GONE
                    dateTextClock.visibility = View.VISIBLE
                }else{
                    weekTextView.visibility = View.VISIBLE
                    dateTextClock.visibility = View.GONE

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
