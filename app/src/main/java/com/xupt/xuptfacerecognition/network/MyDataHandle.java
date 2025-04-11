package com.xupt.xuptfacerecognition.network;

import java.lang.reflect.Type;

import okhttp3.Callback;

//主要用于封装与网络请求数据处理相关的一些关键信息，
//作为一个数据传递的载体，方便在不同的网络请求处理环节中传递必要的数据，
public class MyDataHandle {

    public Callback mListener = null;

    public MyDataHandle(Callback listener) {
        this.mListener = listener;
    }
}