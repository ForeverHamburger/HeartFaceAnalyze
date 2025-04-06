package com.xupt.xuptfacerecognition.login.register;


import com.xupt.xuptfacerecognition.login.LoadTasksCallBack;

public class RegisterModel implements RegisterContract.Model {

    @Override
    public void getRegisterInfo(String email, String password, String phone, LoadTasksCallBack callBack) {
//        RequestParams params = new RequestParams();
//        params.put("email", email);
//        params.put("password", password);
//        params.put("phone", phone);
//        params.put("verificationcode", verificationCode);
//
//        networkHelper.performPostRequest(URL.LOGIN_SIGNUP_URL, params, callBack);
    }

    @Override
    public void getVerificationCode(String email, LoadTasksCallBack callBack) {
//        RequestParams params = new RequestParams();
//        params.put("email", email);
//
//        networkHelper.performPostRequest(URL.LOGIN_CODE_URL, params, callBack);
    }
}