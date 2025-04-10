package com.sbmatch.deviceopt.utils;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper {
    private final NotificationManager notificationManager;

    private NotificationHelper(Context context){
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    private NotificationHelper() {
        this.notificationManager = (NotificationManager) ActivityThread.currentApplication().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static NotificationHelper getInstance() {
        return new NotificationHelper();
    }
    public static NotificationHelper getInstance(Context context) {
        return new NotificationHelper(context);
    }

    // 创建通知渠道（Android 8.0 以上需要）
    public NotificationChannel createNotificationChannel(String channelId, String channelName, int importance, Uri soundUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setSound(soundUri, null); // 设置通知声音
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
            return channel;
        }
        return null;
    }

    public static class NotificationConfig {
        public String channelId;
        public String title;
        public String content;
        public int notificationId;
        public boolean autoCancel;
        public Intent intent;
        public Uri soundUri;
        public String category;
    }

    // 推送通知
    public void showNotification(String channelId, String title, String content, int notificationId, boolean autoCancel, Intent intent, Uri soundUri, String category) {


        Context context = ActivityThread.currentApplication();

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // 设置通知图标
                .setContentTitle(title) // 设置通知标题
                .setStyle(new NotificationCompat.BigTextStyle())
                .setContentText(content)// 设置通知内容
                .setAutoCancel(autoCancel) // 点击后是否自动取消
                .setContentIntent(pendingIntent) // 设置点击动作
                .setSound(soundUri) // 设置通知声音
                .setCategory(category); // 设置通知分类

        // 如果设置了自动取消，则在悬浮通知消失时自动关闭通知
        if (autoCancel) {
            builder.setTimeoutAfter(3000); // 设置悬浮通知显示时间，单位为毫秒
        }

        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
    }

    // 取消通知
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }

    // 取消所有通知
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    // 检查是否拥有指定权限

    public boolean hasPostPermission() {
        return ContextCompat.checkSelfPermission(ActivityThread.currentApplication(), "android.permission.POST_NOTIFICATIONS") == PackageManager.PERMISSION_GRANTED;
    }

    // 请求权限
    public void requestPermission(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }
}