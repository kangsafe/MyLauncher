package com.zd.note.record;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Admin on 2018/9/7 0007 09:17.
 * Author: kang
 * Email: kangsafe@163.com
 */
public class RecordService extends Service implements AudioRecoderUtils.OnAudioStatusUpdateListener {
    AudioRecoderUtils mAudioRecoderUtils = new AudioRecoderUtils();
    NotificationUtils notificationUtils = new NotificationUtils(this);
    int notifyId = 1;
    Intent lockIntent;
    RecordRecive recordRecive;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        recordRecive = new RecordRecive();
        lockIntent = new Intent(this, LockService.class);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.zd.note.record.start");
        filter.addAction("com.zd.note.record.stop");
        registerReceiver(recordRecive, filter);
        //录音回调
        mAudioRecoderUtils.setOnAudioStatusUpdateListener(this);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(recordRecive);
        super.onDestroy();
        Intent localIntent = new Intent();
        localIntent.setClass(this, RecordService.class); //销毁时重新启动Service
        this.startService(localIntent);
    }

    /**
     * 录音中...
     * 根据分贝值来设置录音时话筒图标的上下波动，下面有讲解
     * mImageView.getDrawable().setLevel((int) (3000 + 6000 * db / 100));
     * mTextView.setText(TimeUtils.long2String(time));
     * 显示通知，id必须不重复，否则新的通知会覆盖旧的通知（利用这一特性，可以对通知进行更新）
     * (int) (3000 + 6000 * db / 100);
     *
     * @param db   当前声音分贝
     * @param time 录音时长
     */
    @Override
    public void onUpdate(double db, long time) {
        notificationUtils.sendNotification(notifyId, "云笔记录音", NotificationUtils.getLookTime(time / 1000), true);
        Message msg = new Message();
        msg.what = 3;
        msg.arg1 = (int) db;
        msg.obj = time;
        EventBus.getDefault().post(msg);
    }

    /**
     * 停止录音
     *
     * @param filePath 保存路径
     */
    @Override
    public void onStop(String filePath) {
        Message msg = new Message();
        msg.what = 2;
        msg.obj = filePath;
        EventBus.getDefault().post(msg);
        notificationUtils.cancleNotification(notifyId);
        stopService(lockIntent);
    }

    public void startRecord() {
        startService(lockIntent);
        try {
            notificationUtils.sendNotification(notifyId, "测试标题", "测试内容", true);
            mAudioRecoderUtils.startRecord();
            Message msg = new Message();
            msg.what = 4;
            EventBus.getDefault().post(msg);
        } catch (Exception e) {
            e.printStackTrace();
            // 启动后删除之前我们定义的通知
            notificationUtils.cancleNotification(notifyId);
        }
    }

    public void stopRecord() {
        try {
            mAudioRecoderUtils.stopRecord();
        } catch (Exception e) {
            stopService(lockIntent);
            Message msg = new Message();
            msg.what = 4;
            EventBus.getDefault().post(msg);
            notificationUtils.cancleNotification(notifyId);
        }
    }

    public class RecordRecive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Action", intent.getAction());
            if (intent.getAction().equals("com.zd.note.record.start")) {
                startRecord();
                Message msg = new Message();
                msg.what = 1;
                EventBus.getDefault().post(msg);
            } else if (intent.getAction().equals("com.zd.note.record.stop")) {
                stopRecord();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        logger.debug("开启锁屏服务");
        return START_REDELIVER_INTENT;
    }
}
