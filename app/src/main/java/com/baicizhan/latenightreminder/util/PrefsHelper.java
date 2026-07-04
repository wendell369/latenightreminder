package com.baicizhan.latenightreminder.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {

    private static final String PREFS_NAME = "latenight_reminder_prefs";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";

    private static final String KEY_ALARM_HOUR = "alarm_hour";
    private static final String KEY_ALARM_MINUTE = "alarm_minute";
    private static final int DEFAULT_HOUR = 23;
    private static final int DEFAULT_MINUTE = 0;

    public void setAlarmTime(int hour, int minute) {
        prefs.edit().putInt(KEY_ALARM_HOUR, hour).putInt(KEY_ALARM_MINUTE, minute).apply();
    }

    public int getAlarmHour() {
        return prefs.getInt(KEY_ALARM_HOUR, DEFAULT_HOUR);
    }

    public int getAlarmMinute() {
        return prefs.getInt(KEY_ALARM_MINUTE, DEFAULT_MINUTE);
    }

    private final SharedPreferences prefs;

    public PrefsHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setReminderEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply();
    }

    public boolean isReminderEnabled() {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, false);
    }

    public boolean hasAlarmTime() {
        return prefs.contains(KEY_ALARM_HOUR) && prefs.contains(KEY_ALARM_MINUTE);
    }
}