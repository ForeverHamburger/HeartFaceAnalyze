package com.xupt.xuptfacerecognition.result;

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

public class ResultModel implements ResultContract.ResultModel{
    @Override
    public void getDetectInfo(String token, LoadTasksCallBack callBack) {
        MyDataHandle myDataHandle = new MyDataHandle(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                Log.d("ResultModel", "onResponse: " + result);
                callBack.onSuccess(result);
            }
        });

        RequestParams mToken = new RequestParams();
        mToken.put("Authorization", "Bearer " + token);
        Log.d("TAG", "getDetectInfo: " + token);
        MyOkHttpClient.get(MyRequest.GetRequest(URL.RECOGNITION_QUERY_URL,null,mToken),myDataHandle);
    }

}
