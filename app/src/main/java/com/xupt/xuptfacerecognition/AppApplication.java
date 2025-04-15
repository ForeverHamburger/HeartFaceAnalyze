package com.xupt.xuptfacerecognition;
import android.app.Application;
import com.tencent.mmkv.MMKV;

public class AppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 MMKV（必须在所有 MMKV 使用前调用）
        MMKV.initialize(this);
    }
}