package com.xupt.xuptfacerecognition.base;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UploadRecordManager {
    private static final String PREF_NAME = "upload_records";
    private final SharedPreferences sp;
    private final String recordKey; // 使用文件MD5作为唯一标识

    public UploadRecordManager(Context context, String fileMD5) {
        this.sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.recordKey = "upload_" + fileMD5;
    }

    // 获取已上传的分块索引集合
    public Set<Integer> getUploadedChunks() {
        return sp.getStringSet(recordKey, new HashSet<>())
                .stream()
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }

    // 标记分块为已上传（线程安全）
    public synchronized void markChunkCompleted(int chunkIndex) {
        Set<String> newSet = new HashSet<>(sp.getStringSet(recordKey, new HashSet<>()));
        newSet.add(String.valueOf(chunkIndex));
        sp.edit().putStringSet(recordKey, newSet).apply();
    }

    // 清除记录（上传完成后调用）
    public void clearRecord() {
        sp.edit().remove(recordKey).apply();
    }
}
