package com.xupt.xuptfacerecognition.detector;

import android.util.Log;

import androidx.annotation.NonNull;

import com.xupt.xuptfacerecognition.base.FileToMD5;
import com.xupt.xuptfacerecognition.login.LoadTasksCallBack;
import com.xupt.xuptfacerecognition.network.FileUploader;
import com.xupt.xuptfacerecognition.network.MyDataHandle;
import com.xupt.xuptfacerecognition.network.MyOkHttpClient;
import com.xupt.xuptfacerecognition.network.MyRequest;
import com.xupt.xuptfacerecognition.network.RequestParams;
import com.xupt.xuptfacerecognition.network.URL;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DetectorModel implements DetectorContract.DetectorModel {

    @Override
    public void sendDetectVideo(File file, String token, LoadTasksCallBack callBack) {
        Log.d("TAG", "sendDetectVideo: " + token);
        String fileMD5String = FileToMD5.getFileMD5String(file);
        FileUploader uploader = new FileUploader();
        uploader.sendDetectVideo(file, token, fileMD5String, callBack);
    }

    @Override
    public void sendVideo(File file, String token, LoadTasksCallBack callBack) {
        Log.d("TAG", "sendDetectVideo: " + token);

        String fileMD5String = FileToMD5.getFileMD5String(file);
        MyDataHandle myDataHandle = new MyDataHandle(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callBack.onFailed(e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("TAG", "onResponse: " +response.body().string());
            }
        });

        RequestParams mToken = new RequestParams();
        mToken.put("Authorization", "Bearer " + token);

        RequestParams requestParams = new RequestParams();
        String num = "1";

        try {
            requestParams.put("file", file);
            requestParams.put("chunkIndex", num);
            requestParams.put("totalChunks", num);
            requestParams.put("md5", fileMD5String);
        } catch (FileNotFoundException e) {
            return;
        }

        MyOkHttpClient.get(MyRequest.TestMultiPostRequest(URL.SEND_VIDEO_FILE_URL, requestParams, mToken), myDataHandle);
    }
}
