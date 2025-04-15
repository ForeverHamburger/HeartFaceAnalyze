package com.xupt.xuptfacerecognition.result;

import com.xupt.xuptfacerecognition.info.DataParser;
import com.xupt.xuptfacerecognition.login.BaseView;
import com.xupt.xuptfacerecognition.login.LoadTasksCallBack;

public interface ResultContract {
    interface ResultModel {
        void getDetectInfo(String token, LoadTasksCallBack callBack);
    }
    interface ResultPresenter {
        void getDetectInfo(String token);
    }
    interface View extends BaseView<ResultPresenter> {
        void showError(String error);
        Boolean isACtive();
        void showSuccess(DataParser.Response data);
    }
}
