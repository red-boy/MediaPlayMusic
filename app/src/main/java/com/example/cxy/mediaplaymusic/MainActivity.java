package com.example.cxy.mediaplaymusic;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cxy.mediaplaymusic.utils.SecToTime;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private Button startButton, stopButton, btnTurnup, btnTurndown, btnMute;
    private TextView mTextView;
    private SeekBar seekbar;
    private Intent mIntent;
    private IntentFilter mIntentFilter;
    private MusicBroadcastReceiver mMusicBroadcastReceiver;
    private float positon;//音乐播放的节点
    private float durtion;

    private ServiceConnection mServiceConnection;
    private MusicService.Mybind mMybind;

    private static final int MESSAGE_AUTO_SEARCH = 1;
    private static final int MESSAGE_STOP_SEARCH = 2;
    private MusicListenerHandle mMusicListenerHandle;// 创建一个自己的handler
    private HandlerThread mHandlerThread;

    /**
     * Seekbar监听:
     * onProgressChanged方法的参数fromUser，可用于判断进度改变操作是否来自用户
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d("MainActivity", "progress:" + progress + "拖动进度条操作是否来自用户:" + fromUser);
        if (fromUser) {
            seekbar.setProgress(progress);
            mMybind.seekTo((int) (progress * durtion / 100));
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    public class MusicBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            durtion = bundle.getInt("musicDurtion");

        }
    }

    private float mFloat;//当前时间占总时间的百分比
    //子线程更新UI
    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_AUTO_SEARCH:
                    mFloat = (positon * 100 / durtion);
                    int progress = (int) mFloat;
                    seekbar.setMax(100);
                    seekbar.setProgress(progress);
//                    mTextView.setText("durtion是" + durtion + ",position是" + positon + ",进度是" + progress + "%");
                    mTextView.setText("当前时间：" + SecToTime.secToTime((int) (positon / 1000)) + ",当前播放:" + positon + "毫秒");
                    break;
                case MESSAGE_STOP_SEARCH:
                    Toast.makeText(MainActivity.this, "音乐播放服务结束", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private class MusicListenerHandle extends Handler {
        public MusicListenerHandle(Looper looper) {
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_AUTO_SEARCH:
                    if (mMybind != null) {
                        positon = mMybind.getMusicPostion();

                        mHandler.sendEmptyMessage(MESSAGE_AUTO_SEARCH);

                        sendEmptyMessage(MESSAGE_AUTO_SEARCH);

                    } else {
                        doServiceConnection();
                    }

                    break;
                case MESSAGE_STOP_SEARCH:
                    Log.d("MusicListenerHandle", "音乐播放暂停");

                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.start);
        stopButton = (Button) findViewById(R.id.stop);
        mTextView = (TextView) findViewById(R.id.musicPostion);
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        btnTurnup = (Button) findViewById(R.id.btnTurnup);
        btnTurndown = (Button) findViewById(R.id.btnTurndown);
        btnMute = (Button) findViewById(R.id.btnMute);

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        btnTurnup.setOnClickListener(this);
        btnTurndown.setOnClickListener(this);
        btnMute.setOnClickListener(this);

        //注册广播
        mMusicBroadcastReceiver = new MusicBroadcastReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.example.cxy.mediaplaymusic");
        registerReceiver(mMusicBroadcastReceiver, mIntentFilter);

        //连接服务
        doServiceConnection();

        if (mMusicListenerHandle == null) {
            mHandlerThread = new HandlerThread("handler-thread");
            mHandlerThread.start();
            mMusicListenerHandle = new MusicListenerHandle(mHandlerThread.getLooper());
        }

        //seekbar监听
        seekbar.setOnSeekBarChangeListener(this);


    }

    private void doServiceConnection() {
        if (mServiceConnection == null) {
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    //通过绑定服务获取的Binder对象，传递数据
                    mMybind = (MusicService.Mybind) service;
                    durtion = mMybind.getMusicDuration();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
            mIntent = new Intent(MainActivity.this, MusicService.class);
            startService(mIntent);
            //绑定服务
            isBind = bindService(mIntent, mServiceConnection, Service.BIND_AUTO_CREATE);
        } else {
            Log.d("MainActivity", "ServiceConnection已创建");
        }
    }


    /**
     * 用于判断是否绑定了服务，避免后面程序报错“Service not registered:”
     */
    private boolean isBind = false;
    private boolean isMute = false;//用于静音按钮

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.start:
                Log.d("MainActivity", "开启播放");
                mMybind.startMusic();
                mMusicListenerHandle.sendEmptyMessage(MESSAGE_AUTO_SEARCH);

                break;
            case R.id.stop:
                Log.d("MainActivity", "暂停播放");
                mMybind.pauseMusic();
                mMusicListenerHandle.sendEmptyMessage(MESSAGE_STOP_SEARCH);
                break;

            case R.id.btnTurnup:
                Log.d("MainActivity", "btnTurnup按下");
                mMybind.turnUpVolume();
                break;

            case R.id.btnTurndown:
                Log.d("MainActivity", "btnTurndown按下");
                mMybind.turnDownVolume();
                break;

            case R.id.btnMute:
                Log.d("MainActivity", "btnMute");
                if (isMute == false) {
                    isMute = true;
//                    btnMute.setBackground(getResources().getDrawable(R.drawable.control_btn_wifi1));该方法需要API16版本以上
                    btnMute.setBackgroundResource(R.drawable.control_btn_wifi1);
                    mMybind.muteVolume(isMute);
                } else {
                    isMute = false;
                    btnMute.setBackgroundResource(R.drawable.control_btn_wifi);
                    mMybind.muteVolume(isMute);
                }
                Log.d("MainActivity", "btnMute按下");

                break;


        }

    }

    /**
     * 如果是 调用者 直接退出(onDestroy生命周期)而没有调用stopService/unbindService的话，Service(播放歌曲)会一直在后台运行/程序报错。
     */
    @Override
    protected void onDestroy() {
        if (isBind) {
            unbindService(mServiceConnection);
            stopService(mIntent);
            isBind = false;
        }
        unregisterReceiver(mMusicBroadcastReceiver);

        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity", "onStop");
    }
}
