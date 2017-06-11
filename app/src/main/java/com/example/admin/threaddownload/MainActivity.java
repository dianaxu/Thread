package com.example.admin.threaddownload;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.threaddownload.entities.FileInfo;
import com.example.admin.threaddownload.services.DownloadService;

public class MainActivity extends Activity implements View.OnClickListener {

    private TextView tvFileName = null;
    private ProgressBar pbProgress = null;
    private Button btnStart = null;
    private Button btnPause = null;
    public static MainActivity mMainActivity;
    private FileInfo mFileInfo;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(mMainActivity, "下载成功", Toast.LENGTH_LONG).show();
        }
    };

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                int finised = intent.getIntExtra("finished", 0);
                pbProgress.setProgress(finised);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvFileName = (TextView) findViewById(R.id.tv_fileName);
        pbProgress = (ProgressBar) findViewById(R.id.pb_progress);
        btnStart = (Button) findViewById(R.id.btn_start);
        btnPause = (Button) findViewById(R.id.btn_pause);

        btnStart.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        mFileInfo = new FileInfo(0,
                "http://www.imooc.com/mobile/mukewang.apk",
                "mukewang.apk", 0, 0);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver, filter);
        mMainActivity = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode && btnStart != null) {
            btnStart.performClick();
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                startDownload();
                break;
            case R.id.btn_pause:
                pauseDownload();
                break;
            default:
                break;
        }
    }

    private void startDownload() {
        Intent intent = new Intent(MainActivity.this, DownloadService.class);
        intent.setAction(DownloadService.ACTION_START);
        intent.putExtra("fileInfo", mFileInfo);
        startService(intent);
    }

    private void pauseDownload() {
        Intent intent = new Intent(MainActivity.this, DownloadService.class);
        intent.setAction(DownloadService.ACTION_PAUSE);
        intent.putExtra("fileInfo", mFileInfo);
        startService(intent);
    }


}
