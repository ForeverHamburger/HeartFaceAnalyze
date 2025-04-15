package com.xupt.xuptfacerecognition.login.loginin;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.xupt.xuptfacerecognition.base.NetworkHelper;
import com.xupt.xuptfacerecognition.info.ResponseData;
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
                Log.d("TAG", "onFailure: " + "网络请求失败");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                Log.d("LoginInModel", "onResponse: " + result);

                Gson gson = new Gson();
                ResponseData responseData = gson.fromJson(result, ResponseData.class);

                if (responseData.getMsg().equals("success")) {
                    callBack.onSuccess(responseData.getData().getToken());
                } else {
                    callBack.onFailed(responseData.getMsg());
                }

            }
        });
        RequestParams params = new RequestParams();
        params.put("email", phoneoremail);
        params.put("password", password);
        Log.d("TAG", "getLoginInInfo: " + "确实走到LoginModel了");
        MyOkHttpClient.post(MyRequest.PostRequest(URL.LOGIN_LOGIN_URL,params),myDataHandle);
    }

    public static void getSomeDate() {
        // 这个方法可以用来实现其他数据获取逻辑，比如获取一些额外的信息
    }

}