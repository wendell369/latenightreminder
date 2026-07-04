package com.baicizhan.latenightreminder.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.baicizhan.latenightreminder.receiver.ReminderReceiver;

import java.util.Calendar;

public class AlarmScheduler {

    private static final int REQUEST_CODE_ALARM = 1001;

    public long scheduleExactRepeatingDaily(Context context, int hour, int minute) {
        android.util.Log.d("AlarmScheduler", "scheduleExactRepeatingDaily called");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerTime = getNextTriggerTime(hour, minute);

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_ALARM, intent,
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT);

        cancelAlarm(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }

        return triggerTime;
    }

    public void cancelAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_ALARM, intent,
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private long getNextTriggerTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return calendar.getTimeInMillis();
    }

    public boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    /**
     * 使用默认时间（23:00）安排每日提醒
     */
    public void scheduleDailyReminder(Context context) {
        PrefsHelper helper = new PrefsHelper(context);
        int hour = helper.getAlarmHour();
        int minute = helper.getAlarmMinute();
        android.util.Log.d("AlarmScheduler default", "scheduleDailyReminder: default" + hour + ":" + minute);
        scheduleExactRepeatingDaily(context, hour, minute);
    }

    /**
     * 允许自定义时间（方便测试）
     */
    public void scheduleDailyReminder(Context context, int hour, int minute) {
        android.util.Log.d("AlarmScheduler", "scheduleDailyReminder: " + hour + ":" + minute);

        PrefsHelper helper = new PrefsHelper(context);
        helper.setAlarmTime(hour, minute);
        scheduleExactRepeatingDaily(context, hour, minute);
    }
}