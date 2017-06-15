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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private int mThreadCount = 1;
    private List<DownLoadThread> mDownLoadThreadList = null;
    public static ExecutorService mExecutorService = Executors.newCachedThreadPool();//线程池

    public DownloadTask(Context context, FileInfo mFileInfo, int threadCount) {
        this.mContext = context;
        this.mFileInfo = mFileInfo;
        this.mThreadCount = threadCount;
        this.mDb = ThreadDB.getInstance(context);
    }

    public void downLoad() {
        //读取数据库的线程信息
        List<ThreadInfo> threads = mDb.getThreads(mFileInfo.getUrl());

        //分段
        if (threads.size() == 0) {
            int len = mFileInfo.getLength() / mThreadCount;
            for (int i = 0; i < mThreadCount; i++) {
                ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(),
                        i * len, (i + 1) * len - 1, 0);
                if (i == mThreadCount - 1) {
                    threadInfo.setEnd(mFileInfo.getLength());
                }
                threads.add(threadInfo);
            }
        }
        //多线程下载
        mDownLoadThreadList = new ArrayList<>();
        for (ThreadInfo threadinfo : threads) {
            DownLoadThread thread = new DownLoadThread(threadinfo);
            mExecutorService.execute(thread);
            mDownLoadThreadList.add(thread);
        }
    }

    /**
     * 下载线程
     */
    private class DownLoadThread extends Thread {
        private ThreadInfo mThreadInfo = null;
        public boolean isFinished = false;

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
                //整个文件的进度
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
                        mFinished += len;
                        mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
                        if (System.currentTimeMillis() - time > 1000) {
                            time = System.currentTimeMillis();
                            intent.putExtra("finished", mFinished * 100 / mThreadInfo.getEnd());
                            intent.putExtra("id", mFileInfo.getId());
                            mContext.sendBroadcast(intent);
                        }
                        //在下载暂停时，保存 下载进度
                        if (isPause) {
                            mDb.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(),
                                    mThreadInfo.getFinished());
                            return;
                        }
                    }
                    isFinished = true;
                    checkAllThreadFinished();
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

    private synchronized void checkAllThreadFinished() {
        boolean allFinished = true;
        for (DownLoadThread thread : mDownLoadThreadList) {
            if (!thread.isFinished) {
                allFinished = false;
                break;
            }
        }
        if (allFinished) {
            mDb.deleteThread(mFileInfo.getUrl());
            //下载结束，发送广播
            Intent intent = new Intent(DownloadService.ACTION_FINISHED);
            intent.putExtra("fileInfo", mFileInfo);
            intent.putExtra("id", mFileInfo.getId());
            mContext.sendBroadcast(intent);
        }
    }
}
