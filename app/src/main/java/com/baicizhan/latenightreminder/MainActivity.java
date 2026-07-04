package com.baicizhan.latenightreminder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.baicizhan.latenightreminder.util.AlarmScheduler;
import com.baicizhan.latenightreminder.util.NotificationHelper;
import com.baicizhan.latenightreminder.util.PrefsHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SwitchCompat switchReminder;
    private TextView tvNextReminder;
    private TextView tvPermissionStatus;

    private AlarmScheduler alarmScheduler;
    private PrefsHelper prefsHelper;

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(MainActivity.this, "通知权限被拒绝，将无法收到提醒", Toast.LENGTH_LONG).show();
                }
                if (switchReminder.isChecked()) {
                    enableReminder();
                }
            });

    private final ActivityResultLauncher<Intent> requestExactAlarmLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmScheduler.canScheduleExactAlarms(this)) {
                        if (switchReminder.isChecked()) {
                            enableReminder();
                        }
                        updatePermissionStatus();
                    } else {
                        Toast.makeText(this, "请授予精确闹钟权限，否则提醒可能不准确", Toast.LENGTH_LONG).show();
                        switchReminder.setChecked(false);
                        prefsHelper.setReminderEnabled(false);
                        updateNextReminderTime();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchReminder = findViewById(R.id.switch_reminder);
        tvNextReminder = findViewById(R.id.tv_next_reminder);
        tvPermissionStatus = findViewById(R.id.tv_permission_status);

        alarmScheduler = new AlarmScheduler();
        prefsHelper = new PrefsHelper(this);

        NotificationHelper.createNotificationChannel(this);

        boolean isEnabled = prefsHelper.isReminderEnabled();
        switchReminder.setChecked(isEnabled);
        updateNextReminderTime();
        updatePermissionStatus();

        if (isEnabled) {
            ensureAlarmScheduled();
        }

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            android.util.Log.d("MainActivity", "Switch toggled: " + isChecked);

            if (isChecked) {
                if (checkAndRequestPermissions()) {
                    enableReminder();
                } else {
                    switchReminder.setChecked(false);
                }
            } else {
                disableReminder();
            }
        });

        if (!prefsHelper.hasAlarmTime()) {  // 新增 hasAlarmTime 方法
            prefsHelper.setAlarmTime(23, 0);
        }

        // 强制测试：直接设置 2 分钟后的闹钟
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 2);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        alarmScheduler.scheduleExactRepeatingDaily(this, hour, minute);
        Toast.makeText(this, "测试闹钟已设置，将在 " + hour + ":" + minute + " 触发", Toast.LENGTH_LONG).show();
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return false;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmScheduler.canScheduleExactAlarms(this)) {
                requestExactAlarmPermission();
                return false;
            }
        }
        return true;
    }

    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            new AlertDialog.Builder(this)
                    .setTitle("需要精确闹钟权限")
                    .setMessage("为了在晚上11点准时提醒您洗澡，需要授予精确闹钟权限。点击确定前往设置开启。")
                    .setPositiveButton("去设置", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        requestExactAlarmLauncher.launch(intent);
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }
    }

    private void enableReminder() {
        android.util.Log.d("MainActivity", "enableReminder() called");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 2);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        alarmScheduler.scheduleDailyReminder(this, hour, minute);  // 自动保存到 Prefs
        prefsHelper.setReminderEnabled(true);
        updateNextReminderTime();
        Toast.makeText(this, "提醒已开启，将在 " + hour + ":" + minute + " 触发", Toast.LENGTH_SHORT).show();
        updatePermissionStatus();
    }

    private void disableReminder() {
        alarmScheduler.cancelAlarm(this);
        prefsHelper.setReminderEnabled(false);
        updateNextReminderTime();
        Toast.makeText(this, "提醒已关闭", Toast.LENGTH_SHORT).show();
    }

    private void ensureAlarmScheduled() {
        if (checkAndRequestPermissions()) {
            alarmScheduler.scheduleDailyReminder(this);  // 从 Prefs 读取
            updateNextReminderTime();
        } else {
            tvNextReminder.setText("提醒未开启，缺少必要权限");
        }
    }

    private void updateNextReminderTime() {
        if (prefsHelper.isReminderEnabled()) {
            int hour = prefsHelper.getAlarmHour();
            int minute = prefsHelper.getAlarmMinute();
            Date nextTime = getNextReminderTime(hour, minute);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            tvNextReminder.setText("下次提醒时间: " + sdf.format(nextTime));
        } else {
            tvNextReminder.setText("提醒未开启");
        }
    }

    private void updatePermissionStatus() {
        StringBuilder status = new StringBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean notificationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
            status.append("通知权限: ").append(notificationGranted ? "已授予" : "未授予").append("\n");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean exactAlarmGranted = alarmScheduler.canScheduleExactAlarms(this);
            status.append("精确闹钟权限: ").append(exactAlarmGranted ? "已授予" : "未授予 (提醒可能不准时)");
        } else {
            status.append("精确闹钟权限: 无需授权");
        }
        tvPermissionStatus.setText(status.toString());
    }

    private Date getNextReminderTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return calendar.getTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefsHelper.isReminderEnabled()) {
            if (checkAndRequestPermissions()) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE, 2);
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                alarmScheduler.scheduleDailyReminder(this, hour, minute);
                switchReminder.setChecked(true);
                updateNextReminderTime();
            } else {
                if (switchReminder.isChecked()) {
                    disableReminder();
                    switchReminder.setChecked(false);
                }
            }
        } else {
            switchReminder.setChecked(false);
        }
        updatePermissionStatus();
    }
}