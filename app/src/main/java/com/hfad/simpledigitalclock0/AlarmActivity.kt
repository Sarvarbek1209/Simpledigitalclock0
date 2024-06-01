package com.hfad.simpledigitalclock0

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.text.Layout
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide


class AlarmActivity : AppCompatActivity() {


    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()

        // Проверьте, был ли запущен этот экран из-за срабатывания будильника
        if (intent.getBooleanExtra("isAlarm", false)) {
            // Если да, отобразите GIF в вашем ImageView
            Glide.with(this)
                .load(R.raw.gif_e) // Путь к вашему GIF
                .into(findViewById(R.id.imageView1)) // Укажите ваш ImageView

            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer.create(this, alarmSound).apply {
                isLooping = true // Установка зацикленного воспроизведения
                start()}
        }
        val imageView1 = findViewById<RelativeLayout>(R.id.Layout)
        imageView1.setOnClickListener {
            cancelAlarm()
            stopAlarmSound()

            val mainActivityIntent = Intent(this@AlarmActivity,MainActivity::class.java)
            mainActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            mainActivityIntent.putExtra("imageView1", true)
            startActivity(mainActivityIntent)
        }
    }

    private fun hideSystemUI() {
        window.decorView.apply {
            // Скрыть панель навигации и строку состояния
            systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        }
    }


    private fun cancelAlarm() {
        val context = applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, alarmIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun stopAlarmSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

}