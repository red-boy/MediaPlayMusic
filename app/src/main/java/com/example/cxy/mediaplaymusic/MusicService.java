package com.example.cxy.mediaplaymusic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by HaodaHw on 2017/3/6.
 */

public class MusicService extends Service {
    private MediaPlayer mMediaPlayer;

    public MusicService() {

    }

    public class Mybind extends Binder {

        //获取歌曲总长度
        public int getMusicDuration() {
            int duration = 0;
            duration = mMediaPlayer.getDuration();
            return duration;
        }

        //跟踪歌曲播放进度
        public int getMusicPostion() {
            int postion = 0;
            postion = mMediaPlayer.getCurrentPosition();
            return postion;
        }

        //手动设置播放进度
        public void seekTo(int position) {
            mMediaPlayer.seekTo(position);

        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MusicService", "MusicService正在运行");
        mMediaPlayer = MediaPlayer.create(this, R.raw.music);
        mMediaPlayer.setLooping(true);//设置循环播放

        /**发送广播*/
        Intent intent = new Intent();
        intent.putExtra("musicPosition", mMediaPlayer.getCurrentPosition());
        intent.putExtra("musicDurtion", mMediaPlayer.getDuration());
        intent.setAction("com.example.cxy.mediaplaymusic");
        sendBroadcast(intent);


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
        return new Mybind();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
