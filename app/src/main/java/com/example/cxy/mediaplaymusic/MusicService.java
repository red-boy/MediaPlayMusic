package com.example.cxy.mediaplaymusic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by HaodaHw on 2017/3/6.
 */

public class MusicService extends Service {
    private MediaPlayer mMediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("MusicService", "MusicService正在运行");
        mMediaPlayer = MediaPlayer.create(this, R.raw.music);
        mMediaPlayer.setLooping(true);
    }

    @Override
    public void onDestroy() {
        Log.d("MusicService", "onDestroy");
        mMediaPlayer.stop();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMediaPlayer.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
