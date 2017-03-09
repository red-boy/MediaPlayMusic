package com.example.cxy.mediaplaymusic;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
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
    private AudioManager mAudioManager;//音频
    private int duration;
    private int postion;

    public MusicService() {

    }

    public class Mybind extends Binder {

        //获取歌曲总长度
        public int getMusicDuration() {
            if (mMediaPlayer != null) {
                duration = mMediaPlayer.getDuration();
            } else {
                return 0;
            }
            return duration;
        }

        //跟踪歌曲播放进度
        public int getMusicPostion() {
            if (mMediaPlayer != null) {
                postion = mMediaPlayer.getCurrentPosition();
            } else {
                return 0;
            }
            return postion;
        }

        //手动设置播放进度
        public void seekTo(int position) {
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(position);
            }

        }

        public void pauseMusic() {
            if (mMediaPlayer != null) {
                Log.d("Mybind", "MediaPlayer的pause方法");
                mMediaPlayer.pause();
            }
        }

        public void startMusic() {
            if (mMediaPlayer != null) {
                Log.d("Mybind", "MediaPlayer的start方法");
                mMediaPlayer.start();
            }
        }

        //释放资源
        public void relaseMedia() {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer = null;
                mMediaPlayer.release();
            } else {
                Log.d("Mybind", "mMediaPlayer之前就已为空");
            }
        }

        //循环音乐
        public void loopMusic() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                Log.d("Mybind", "loopMusic");
                mMediaPlayer.setLooping(true);
                mMediaPlayer.start();
            }
        }

        //减小音量
        public void turnDownVolume() {
            if (mAudioManager != null) {
                /**第一个参数：声音类型
                 * 第二个参数：调整音量的方向
                 * 第三个参数：可选的标志位*/
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            }
        }

        //增大音量
        public void turnUpVolume() {
            if (mAudioManager != null) {
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            }
        }

        //静音
        public void muteVolume(boolean isMute) {
            if (mAudioManager != null) {
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, isMute);
            }
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = MediaPlayer.create(this, R.raw.music);//装载资源中的音乐文件
        mMediaPlayer.setLooping(true);//设置循环播放

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

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
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        mMediaPlayer.start();
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
