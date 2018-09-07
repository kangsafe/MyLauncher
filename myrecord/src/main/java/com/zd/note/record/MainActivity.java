package com.zd.note.record;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends Activity implements View.OnClickListener {
    Button vstart;
    Button vstop;
    private String TAG = getClass().getSimpleName();
    private int PERMISSIONS_REQUEST_FOR_AUDIO = 0x10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vstart = findViewById(R.id.vstart);
        vstop = findViewById(R.id.vstop);
        vstart.setOnClickListener(this);
        vstop.setOnClickListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.vstart:
                //安卓6.0以上录音相应权限处理
                if (Build.VERSION.SDK_INT > 22) {
                    permissionForM();
                } else {
                    startRecord();
                }
                break;
            case R.id.vstop:
                stopRecord();
                break;
        }
    }

    /*******6.0以上版本手机权限处理***************************/
    /**
     * @description 兼容手机6.0权限管理
     * @author ldm
     * @time 2016/5/24 14:59
     */
    private void permissionForM() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_FOR_AUDIO);
        } else {
            startRecord();
        }

    }

    private void startRecord() {
        sendBroadcast(new Intent("com.zd.note.record.start"));
    }

    private void stopRecord() {
        sendBroadcast(new Intent("com.zd.note.record.stop"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST_FOR_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecord();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showProgress(Message message) {
        switch (message.what) {
            case 1://开始
                if (!NotificationUtils.isNotificationEnabled(this)) {
                    final AlertDialog dialog = new AlertDialog.Builder(this).create();
                    dialog.show();

                    View view = View.inflate(this, R.layout.dialog, null);
                    dialog.setContentView(view);

                    TextView context = (TextView) view.findViewById(R.id.tv_dialog_context);
                    context.setText("检测到您没有打开通知权限，是否去打开");

                    TextView confirm = (TextView) view.findViewById(R.id.btn_confirm);
                    confirm.setText("确定");
                    confirm.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog.cancel();
                            Intent localIntent = new Intent();
                            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (Build.VERSION.SDK_INT >= 9) {
                                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                localIntent.setData(Uri.fromParts("package", MainActivity.this.getPackageName(), null));
                            } else if (Build.VERSION.SDK_INT <= 8) {
                                localIntent.setAction(Intent.ACTION_VIEW);

                                localIntent.setClassName("com.android.settings",
                                        "com.android.settings.InstalledAppDetails");

                                localIntent.putExtra("com.android.settings.ApplicationPkgName",
                                        MainActivity.this.getPackageName());
                            }
                            startActivity(localIntent);
                        }
                    });

                    TextView cancel = (TextView) view.findViewById(R.id.btn_off);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });
                }
                Log.i(TAG, "开始：" + System.currentTimeMillis());
                break;
            case 2://结束
                Log.i(TAG, "结束：" + message.obj.toString());
                break;
            case 3://更新
//                Log.i(TAG, "更新：" + message.obj.toString() + ":" + message.arg1);
                break;
            case 4://错误
                break;
        }
    }
}
