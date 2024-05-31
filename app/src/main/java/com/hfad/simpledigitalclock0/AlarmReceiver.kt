package com.hfad.simpledigitalclock0

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.widget.Toast


class AlarmReceiver : BroadcastReceiver() {

         override fun onReceive(context: Context?, intent: Intent?) {
    if ( intent?.action == "android.intent.action.ALARM_RECEIVER") {
        val alarmSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val rigntone = RingtoneManager.getRingtone(context,alarmSound)
        rigntone.play()
    }  else
        if (intent?.action == "android.intent.action.DISMISS_ALARM") {
        // В этом блоке вы можете определить действия, которые нужно выполнить при отключении будильника
        // Например, отобразить сообщение
        //Toast.makeText(context, "Будильник отключен!", Toast.LENGTH_SHORT).show()
    }
}
}