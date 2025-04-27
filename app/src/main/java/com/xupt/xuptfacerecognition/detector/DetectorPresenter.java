package com.xupt.xuptfacerecognition.detector;

import android.util.Log;

import com.xupt.xuptfacerecognition.login.LoadTasksCallBack;

import java.io.File;

public class DetectorPresenter implements DetectorContract.DetectorPresenter, LoadTasksCallBack<String> {
    private DetectorContract.DetectorView view;
    private DetectorContract.DetectorModel model;

    public DetectorPresenter(DetectorContract.DetectorView view, DetectorContract.DetectorModel model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void sendVideoInfo(File file, String token) {
        model.sendDetectVideo(file, token, this);
//        model.sendVideo(file, token, this);
    }

    @Override
    public void onSuccess(String data) {
        view.showSuccess("成功！");
    }

    @Override
    public void onFailed(String error) {
        Log.d("TAG", "onFailed: " + error);
    }
}
