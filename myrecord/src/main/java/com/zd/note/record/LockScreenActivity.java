package com.zd.note.record;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.zd.note.record.NotificationUtils.getLookTime;

/**
 * Created by Admin on 2018/9/7 0007 11:04.
 * Author: kang
 * Email: kangsafe@163.com
 */
public class LockScreenActivity extends Activity {
    SildingFinishLayout slf;
    TextView vimg;
    TextView vtime;
    ImageView mImageView;

    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(R.layout.activity_lockscreen);
        initView();
        initListener();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 左右滑动的监听
     */
    private final class OnSildingFinishListenerImplementation implements
            SildingFinishLayout.OnSildingFinishListener {
        @Override
        public void onSildingForward() {
            LockScreenActivity.this.finish();
        }

        @Override
        public void onSildingBack() {
            LockScreenActivity.this.finish();
        }
    }

    public void initView() {
        slf = findViewById(R.id.sfl);
        vimg = findViewById(R.id.vimg);
        vtime = findViewById(R.id.tv_recording_time);
        mImageView = findViewById(R.id.iv_recording_icon); //录音话筒图标
        slf.setEnableLeftSildeEvent(true);
        slf.setEnableRightSildeEvent(false);
    }


    public void initListener() {
        slf.setOnSildingFinishListener(new OnSildingFinishListenerImplementation());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showProgress(Message message) {
        switch (message.what) {
            case 1://开始
                Log.i(TAG, "开始：" + System.currentTimeMillis());
                break;
            case 2://结束
                finish();
                Log.i(TAG, "结束：" + message.obj.toString());
                break;
            case 3://更新
                mImageView.getDrawable().setLevel((int) (3000 + 6000 * message.arg1 / 100));
                long time = (long) message.obj;
                vtime.setText(getLookTime(time / 1000));
//                Log.i(TAG, "更新：" + message.obj.toString() + ":" + message.arg1);
                break;
            case 4://错误
                finish();
                break;
        }
    }
}
