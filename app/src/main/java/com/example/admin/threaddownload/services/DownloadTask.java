package com.example.admin.threaddownload.services;

import android.content.Context;
import android.content.Intent;

import com.example.admin.threaddownload.MainActivity;
import com.example.admin.threaddownload.db.ThreadDB;
import com.example.admin.threaddownload.entities.FileInfo;
import com.example.admin.threaddownload.entities.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * 下载任务类
 *
 * @author Diana
 * @date 2017/6/11
 */

public class DownloadTask {

    private Context mContext;
    private ThreadDB mDb = null;
    private FileInfo mFileInfo = null;
    private int mFinished = 0;
    public boolean isPause = false;

    public DownloadTask(Context context, FileInfo mFileInfo) {
        this.mContext = context;
        this.mFileInfo = mFileInfo;
        this.mDb = new ThreadDB(mContext);
    }

    public void downLoad() {
        //读取数据库的线程信息
        List<ThreadInfo> threads = mDb.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if (threads.size() == 0) {
            //初始化线程信息对象
            threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
        } else {
            threadInfo = threads.get(0);
        }
        // 创建子线程进行下载
        new DownLoadThread(threadInfo).start();
    }

    /**
     * 下载线程
     */
    private class DownLoadThread extends Thread {
        private ThreadInfo mThreadInfo = null;

        public DownLoadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
        }

        @Override
        public void run() {
            //向数据库插入线程信息
            if (!mDb.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
                mDb.insertThread(mThreadInfo);
            }
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            InputStream inputStream = null;

            try {
                URL url = new URL(mThreadInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");

                //设置下载位置
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                connection.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
                //设置文件写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                Intent intent = new Intent();
                intent.setAction(DownloadService.ACTION_UPDATE);
                mFinished += mThreadInfo.getFinished();
                //开始下载
                if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    //读取数据
                    inputStream = connection.getInputStream();
                    byte[] buf = new byte[1024 << 2];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = inputStream.read(buf)) != -1) {
                        //写入文件
                        raf.write(buf, 0, len);
                        //把下载进度发送广播给Activity
                        mFinished += len;
                        if (System.currentTimeMillis() - time > 500) {
                            time = System.currentTimeMillis();
                            intent.putExtra("finished", mFinished * 100 / mThreadInfo.getEnd());
                            mContext.sendBroadcast(intent);
                        }
                        //在下载暂停时，保存 下载进度
                        if (isPause) {
                            mDb.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(),
                                    mFinished);
                            return;
                        }
                    }
                    //下载完成，删除线程信息
                    mDb.deleteThread(mThreadInfo.getUrl(), mThreadInfo.getId());
                    MainActivity.mMainActivity.handler.sendEmptyMessage(0);
                }


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
