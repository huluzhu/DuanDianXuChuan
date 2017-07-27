package com.bwie.duandianxuchuan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.daimajia.numberprogressbar.NumberProgressBar;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String urlPath = "http://wcy.fgtrj.com/jdsc.apk";
    private NumberProgressBar num;
    private int progress;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            num.setProgress(msg.what);
            if (msg.what == 100) {
                isDown = false;
                size = 0;
                length = 0;

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file),
                        "application/vnd.android.package-archive");
                startActivity(intent);
            }
        }
    };
    private File file;
    private boolean isDown;
    private long length = 0;
    private long size = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        num = (NumberProgressBar) findViewById(R.id.number_progress_bar);
        file = new File(getExternalCacheDir(), "jingdong.apk");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.kaishi:
                startDown();
                break;
            case R.id.zanting:
                pauseDown();
                break;
        }

    }

    private void pauseDown() {
        isDown = false;
    }

    private void startDown() {
        if (isDown)
            return;
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    RandomAccessFile raf = new RandomAccessFile(file, "rw");
                    raf.seek(size);
                    URL url = new URL(urlPath);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    int responCode = 200;
                    if (length != 0) {
                        conn.setRequestProperty("Range", "bytes=" + size + "-" + length);
                        responCode = 206;
                    }
                    if (conn.getResponseCode() == responCode) {
                        if (length == 0)
                            length = conn.getContentLength();
                        InputStream inputStream = conn.getInputStream();
                        byte[] bytes = new byte[1024];
                        int len = -1;
                        while (isDown && (len = inputStream.read(bytes)) != -1) {
                            raf.write(bytes, 0, len);
                            size += len;

                            int pro = (int) (size * 100 / length);
                            if (pro != progress) {
                                progress = pro;
                                sleep(100);
                                handler.sendEmptyMessage(progress);
                            }
                        }
                    }
                    raf.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        isDown = true;
        thread.start();
    }
}
