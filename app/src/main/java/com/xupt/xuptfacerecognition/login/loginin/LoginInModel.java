package com.xupt.xuptfacerecognition.login.loginin;


import android.util.Log;

import androidx.annotation.NonNull;

import com.xupt.xuptfacerecognition.base.NetworkHelper;
import com.xupt.xuptfacerecognition.login.LoadTasksCallBack;
import com.xupt.xuptfacerecognition.network.MyDataHandle;
import com.xupt.xuptfacerecognition.network.MyOkHttpClient;
import com.xupt.xuptfacerecognition.network.MyRequest;
import com.xupt.xuptfacerecognition.network.RequestParams;
import com.xupt.xuptfacerecognition.network.URL;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class LoginInModel implements LoginInContract.Model {
    @Override
    public void getLoginInInfo(String phoneoremail, String password, LoadTasksCallBack callBack) {
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
        RequestParams params = new RequestParams();
        params.put("phoneoremail", phoneoremail);
        params.put("password", password);

        MyOkHttpClient.post(MyRequest.PostRequest(URL.LOGIN_SIGNUP_URL,params),myDataHandle);
        callBack.onSuccess("");
    }

    public static void getSomeDate() {
        // 这个方法可以用来实现其他数据获取逻辑，比如获取一些额外的信息
    }

}