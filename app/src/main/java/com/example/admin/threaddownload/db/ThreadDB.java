package com.example.admin.threaddownload.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.admin.threaddownload.entities.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Diana
 * @date 2017/6/11
 */

public class ThreadDB implements IThreadDB {
    private DBHelper mHelper = null;
    private static ThreadDB mThreadDB = null;

    public static synchronized ThreadDB getInstance(Context context) {
        if (mThreadDB == null) {
            mThreadDB = new ThreadDB(context);
        }
        return mThreadDB;
    }

    private ThreadDB(Context context) {
        mHelper = new DBHelper(context);
    }

    @Override
    public synchronized void insertThread(ThreadInfo threadInfo) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("thread_id", threadInfo.getId());
        values.put("start", threadInfo.getStart());
        values.put("end", threadInfo.getEnd());
        values.put("url", threadInfo.getUrl());
        values.put("finished", threadInfo.getFinished());
        db.insert("thread_info", null, values);
        db.close();
    }

    @Override
    public synchronized void deleteThread(String url) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete("thread_info", "url = ? and thread_id = ?",
                new String[]{url});
        db.close();
    }

    @Override
    public synchronized void updateThread(String url, int thread_id, int finished) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("finished", finished);
        db.update("thread_info", values, "url = ? and thread_id = ?",
                new String[]{url, String.valueOf(thread_id)});
        db.close();
    }

    @Override
    public synchronized List<ThreadInfo> getThreads(String url) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ?", new String[]{url});

        List<ThreadInfo> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            ThreadInfo info = new ThreadInfo(
                    cursor.getInt(cursor.getColumnIndex("thread_id")),
                    cursor.getString(cursor.getColumnIndex("url")),
                    cursor.getInt(cursor.getColumnIndex("start")),
                    cursor.getInt(cursor.getColumnIndex("end")),
                    cursor.getInt(cursor.getColumnIndex("finished"))
            );
            list.add(info);
        }
        cursor.close();
        db.close();
        return list;
    }

    @Override
    public synchronized boolean isExists(String url, int thread_id) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ? and thread_id = ?",
                new String[]{url, String.valueOf(thread_id)});
        boolean exists = cursor.moveToNext();
        cursor.close();
        db.close();
        return exists;
    }
}
