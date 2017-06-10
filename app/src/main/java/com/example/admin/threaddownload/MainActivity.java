package com.example.admin.threaddownload;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

    private TextView tvFileName = null;
    private ProgressBar pbProgress = null;
    private Button btnStart = null;
    private Button btnPause = null;

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

    }

    private void pauseDownload() {

    }
}
