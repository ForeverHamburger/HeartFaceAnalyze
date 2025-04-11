package com.xupt.xuptfacerecognition.login.register;


import android.util.Log;

import androidx.annotation.NonNull;

import com.xupt.xuptfacerecognition.login.LoadTasksCallBack;
import com.xupt.xuptfacerecognition.network.MyDataHandle;
import com.xupt.xuptfacerecognition.network.MyOkHttpClient;
import com.xupt.xuptfacerecognition.network.MyRequest;
import com.xupt.xuptfacerecognition.network.RequestParams;
import com.xupt.xuptfacerecognition.network.URL;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RegisterModel implements RegisterContract.Model {

    @Override
    public void getRegisterInfo(String email, String password, String phone, LoadTasksCallBack callBack) {
        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("password", password);
        params.put("phone", phone);
        MyDataHandle myDataHandle = new MyDataHandle(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                Log.d("LoginInModel", "onResponse: " + result);

            }
        });

        MyOkHttpClient.post(MyRequest.PostRequest(URL.LOGIN_SIGNUP_URL,params),myDataHandle);
        callBack.onSuccess("");
    }

    @Override
    public void getVerificationCode(String email, LoadTasksCallBack callBack) {
//        RequestParams params = new RequestParams();
//        params.put("email", email);
//
//        networkHelper.performPostRequest(URL.LOGIN_CODE_URL, params, callBack);
    }
}