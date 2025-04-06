package com.xupt.xuptfacerecognition.login;

public interface LoadTasksCallBack<T> {
    void onSuccess(T data);
    void onFailed(String error);
}
