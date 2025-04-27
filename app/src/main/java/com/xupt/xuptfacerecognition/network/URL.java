package com.xupt.xuptfacerecognition.network;

public class URL {
    public static final String BASE_URL = "https://192.168.1.57:8080";
    //注册
    public static final String LOGIN_SIGNUP_URL = BASE_URL +"/api/v1/signup";
    //普通登录
    public static final String LOGIN_LOGIN_URL = BASE_URL +"/api/v1/login";
    public static final String RECOGNITION_QUERY_URL = BASE_URL + "/api/v1/data";
    public static final String SEND_VIDEO_FILE_URL = BASE_URL + "/api/v1/upload";
}
