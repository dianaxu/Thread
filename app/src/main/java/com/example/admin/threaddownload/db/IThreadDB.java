package com.example.admin.threaddownload.db;

import com.example.admin.threaddownload.entities.ThreadInfo;

import java.util.List;

/**
 * 线程数据访问接口
 *
 * @author Diana
 * @date 2017/6/11
 */

public interface IThreadDB {
    //插入线程信息
    void insertThread(ThreadInfo threadInfo);

    //删除线程信息
    void deleteThread(String url, int thread_id);

    //更新线程下载进度
    void updateThread(String url, int thread_id, int finished);

    //查询线程信息
    List<ThreadInfo> getThreads(String url);

    //线程信息是否存在
    boolean isExists(String url, int thread_id);
}
