package com.xupt.xuptfacerecognition.detector;

import com.xupt.xuptfacerecognition.info.DataParser;
import com.xupt.xuptfacerecognition.login.BaseView;
import com.xupt.xuptfacerecognition.login.LoadTasksCallBack;

import java.io.File;

public interface DetectorContract {
    interface DetectorModel {
        void sendDetectVideo(File file, String token, LoadTasksCallBack callBack);
        void sendVideo(File file, String token, LoadTasksCallBack callBack);
    }
    interface DetectorPresenter {
        void sendVideoInfo(File file,String token);
    }
    interface DetectorView extends BaseView<DetectorPresenter> {
        void showError(String error);
        Boolean isACtive();
        void showSuccess(String data);
    }
}
