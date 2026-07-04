package com.baicizhan.latenightreminder.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.baicizhan.latenightreminder.util.AlarmScheduler;
import com.baicizhan.latenightreminder.util.PrefsHelper;

public class TimeChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_TIME_CHANGED.equals(action) || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            PrefsHelper prefsHelper = new PrefsHelper(context);
            if (prefsHelper.isReminderEnabled()) {
                AlarmScheduler alarmScheduler = new AlarmScheduler();
                alarmScheduler.scheduleExactRepeatingDaily(context, 23, 0);
            }
        }
    }
}