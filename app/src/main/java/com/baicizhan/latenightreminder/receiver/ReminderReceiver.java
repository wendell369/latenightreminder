package com.baicizhan.latenightreminder.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.baicizhan.latenightreminder.util.AlarmScheduler;
import com.baicizhan.latenightreminder.util.NotificationHelper;
import com.baicizhan.latenightreminder.util.PrefsHelper;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.sendReminderNotification(context);
        PrefsHelper helper = new PrefsHelper(context);
        if (helper.isReminderEnabled()) {
            AlarmScheduler scheduler = new AlarmScheduler();
            scheduler.scheduleDailyReminder(context);  // 从 Prefs 读取保存的时间
        }
    }
}