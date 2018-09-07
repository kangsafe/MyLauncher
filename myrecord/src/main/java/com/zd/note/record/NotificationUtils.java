package com.zd.note.record;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class NotificationUtils extends ContextWrapper {

    private NotificationManager manager;
    public static final String id = "channel_1";
    public static final String name = "channel_name_1";

    public NotificationUtils(Context context) {
        super(context);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void createNotificationChannel() {
//        NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT);
        //关闭震动
        channel.enableVibration(false);
        //关闭声音
        channel.setSound(null, null);
        getManager().createNotificationChannel(channel);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void createNotificationChannel(boolean isVibrate,
                                          boolean hasSound,
                                          String channelId,
                                          String channelName,
                                          int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        //设置震动
        channel.enableVibration(isVibrate);
        //设置闪光
        channel.enableLights(true);
        //设置提示音
        if (!hasSound) {
            channel.setSound(null, null);
        }
        getManager().createNotificationChannel(channel);
    }

    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getChannelNotification(String title, String content, boolean aways) {
        return new Notification.Builder(getApplicationContext(), id)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(!aways)//android 8.0消息提醒声音屏蔽
                .setOnlyAlertOnce(true)
                .setOngoing(aways);
    }

    public NotificationCompat.Builder getNotification_25(String title, String content, boolean aways) {
        return new NotificationCompat.Builder(getApplicationContext())
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(!aways)
                .setOnlyAlertOnce(true)//android 8.0消息提醒声音屏蔽
                .setOngoing(aways);
    }

    public void sendNotification(String title, String content, boolean aways) {
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel();
            Notification notification = getChannelNotification
                    (title, content, aways).build();
            getManager().notify(1, notification);
        } else {
            Notification notification = getNotification_25(title, content, aways).build();
            getManager().notify(1, notification);
        }
    }

    public void sendNotification(int notifyid, String title, String content, boolean aways) {
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel();
            Notification notification = getChannelNotification
                    (title, content, aways).build();
            getManager().notify(notifyid, notification);
        } else {
            Notification notification = getNotification_25(title, content, aways).build();
            getManager().notify(notifyid, notification);
        }
    }

    public void cancleNotification(int notifyid) {
        getManager().cancel(notifyid);
    }

    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

    @SuppressLint("NewApi")
    public static boolean isNotificationEnabled(Context context) {

        AppOpsManager mAppOps =
                (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        Class appOpsClass = null;

        /* Context.APP_OPS_MANAGER */
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());

            Method checkOpNoThrowMethod =
                    appOpsClass.getMethod(CHECK_OP_NO_THROW,
                            Integer.TYPE, Integer.TYPE, String.class);

            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (Integer) opPostNotificationValue.get(Integer.class);

            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) ==
                    AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getLookTime(long time) {  // time为秒
        // 小时
        long hour = time / (60 * 60);
        // 分钟
        long min = (time / 60) - (hour * 60);
        // 秒
        long sec = time - (hour * 60 * 60) - (min * 60);
        String result = "";
        if (hour > 0) {
            result = hour + "时" + min + "分" + sec + "秒";
        } else {
            if (min > 0) {
                result = min + "分" + sec + "秒";
            } else {
                result = sec + "秒";
            }
        }

        return result;
    }
}