package com.example.cxy.mediaplaymusic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button startButton, stopButton;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.start);
        stopButton = (Button) findViewById(R.id.stop);

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mIntent = new Intent(MainActivity.this, MusicService.class);
        switch (v.getId()) {
            case R.id.start:
                Log.d("MainActivity", "开启播放");
                startService(mIntent);
                break;
            case R.id.stop:
                Log.d("MainActivity", "暂停播放");
                stopService(mIntent);
                break;
        }

    }

    /**
     * 如果是 调用者 直接退出(onDestroy生命周期)而没有调用stopService的话，Service(播放歌曲)会一直在后台运行。
     */
    @Override
    protected void onDestroy() {
        Log.d("MainActivity", "onDestroy");
        stopService(mIntent);
        super.onDestroy();
    }
}
