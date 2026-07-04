package com.baicizhan.latenightreminder.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.baicizhan.latenightreminder.util.AlarmScheduler;
import com.baicizhan.latenightreminder.util.PrefsHelper;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            PrefsHelper prefsHelper = new PrefsHelper(context);
            if (prefsHelper.isReminderEnabled()) {
                AlarmScheduler alarmScheduler = new AlarmScheduler();
                alarmScheduler.scheduleExactRepeatingDaily(context, 23, 0);
            }
        }
    }
}