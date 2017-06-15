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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.threaddownload.entities.FileInfo;
import com.example.admin.threaddownload.services.DownloadService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    private ListView lvFile;
    private FileListAdapter mAdapter;

    public static MainActivity mMainActivity;
    private List<FileInfo> mFileList = new ArrayList<>();
//
//    public Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            Toast.makeText(mMainActivity, "下载成功", Toast.LENGTH_LONG).show();
//        }
//    };

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                int finised = intent.getIntExtra("finished", 0);
                int id = intent.getIntExtra("id", 0);
                mAdapter.updateProgress(id, finised);
            } else if (DownloadService.ACTION_FINISHED.equals(intent.getAction())) {
                mAdapter.updateProgress(intent.getIntExtra("id", 0), 0);
                Toast.makeText(mMainActivity, "下载成功", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvFile = (ListView) findViewById(R.id.lv_file);
        setAdapter();

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.ACTION_FINISHED);
        registerReceiver(mReceiver, filter);
        mMainActivity = this;
    }

    private void setAdapter() {
        mFileList.add(new FileInfo(0,
                "http://www.imooc.com/mobile/mukewang.apk",
                "mukewang.apk", 0, 0));
        mFileList.add(new FileInfo(1,
                "http://gdown.baidu.com/data/wisegame/d08a178fb05acea2/aiqiyi_80880.apk",
                "aiyiqi.apk", 0, 0));
        mFileList.add(new FileInfo(2,
                "http://sw.bos.baidu.com/sw-search-sp/software/a40ee9c29a4dd/QQ_8.9.3.21149_setup.exe",
                "qq.apk", 0, 0));
        mAdapter = new FileListAdapter(this, mFileList);
        lvFile.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
       /* if (KeyEvent.KEYCODE_BACK == keyCode ) {
            btnStart.performClick();
        }*/
        return super.onKeyUp(keyCode, event);
    }

}
