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
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button startButton, stopButton;
    private TextView mTextView;
    private Intent mIntent;
    private IntentFilter mIntentFilter;
    private MusicBroadcastReceiver mMusicBroadcastReceiver;
    private int positon;//音乐播放的节点
    private int durtion;

    private ServiceConnection mServiceConnection;
    private MusicService.Mybind mMybind;

    private static final int MESSAGE_AUTO_SEARCH = 1;
    private static final int MESSAGE_STOP_SEARCH = 2;
    private MusicListenerHandle mMusicListenerHandle;// 创建一个自己的handler
    private HandlerThread mHandlerThread;


    public class MusicBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            durtion = bundle.getInt("musicDurtion");
            Log.d("onReceive", "durtion:" + durtion);

        }
    }

    //子线程更新UI
    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_AUTO_SEARCH:
                    mTextView.setText("durtion是" + durtion + ",position是" + positon);
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
                        Log.d("MusicListenerHandle", "position是" + positon);
                        mHandler.sendEmptyMessage(MESSAGE_AUTO_SEARCH);
                        if (positon <= durtion) {
                            sendEmptyMessage(MESSAGE_AUTO_SEARCH);
                        } else {
                            sendEmptyMessage(MESSAGE_STOP_SEARCH);
                        }
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

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.start:
                Log.d("MainActivity", "开启播放");
                doServiceConnection();
                mMusicListenerHandle.sendEmptyMessage(MESSAGE_AUTO_SEARCH);

                break;
            case R.id.stop:
                Log.d("MainActivity", "暂停播放");
                if (isBind) {
                    unbindService(mServiceConnection);
                    stopService(mIntent);
                    isBind = false;
                }

                mMusicListenerHandle.sendEmptyMessage(MESSAGE_STOP_SEARCH);
                break;
        }

    }

    /**
     * 如果是 调用者 直接退出(onDestroy生命周期)而没有调用stopService/unbindService的话，Service(播放歌曲)会一直在后台运行/程序报错。
     */
    @Override
    protected void onDestroy() {
        Log.d("MainActivity", "onDestroy");
        unbindService(mServiceConnection);
        stopService(mIntent);
        unregisterReceiver(mMusicBroadcastReceiver);
        super.onDestroy();
    }
}
