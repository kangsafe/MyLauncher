package com.zd.note.record;

import android.app.Application;
import android.content.Intent;

import java.util.HashSet;

/**
 * Created by Clearlee on 2017/12/26 0026.
 */

public class App extends Application {

    private static App app;
    public static boolean scanMusicFinish;
    public MainActivity activity;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        scanMusic();
        startService(new Intent(this, RecordService.class));
    }

    public static App getApp() {
        return app;
    }

    private void scanMusic() {
//        ThreadManager.getExecutorService().execute(new Runnable() {
//            @Override
//            public void run() {
//                HashSet<LocalMusicInfo> data = MusicUtil.getInstance().getLocalMusicData();
//                if (data != null && data.size() > 0) {
//                    MusicUtil.getInstance().getPlayMusicList().addAll(data);
//                }
//                scanMusicFinish = true;
//            }
//        });

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        stopService(new Intent(this, RecordService.class));
    }
}