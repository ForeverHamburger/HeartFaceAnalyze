package com.xupt.xuptfacerecognition.login.loginin;


import com.xupt.xuptfacerecognition.login.BaseView;
import com.xupt.xuptfacerecognition.login.LoadTasksCallBack;

public interface LoginInContract {
    interface Model {
        void getLoginInInfo(String username, String password, LoadTasksCallBack callBack);
    }
    interface Presenter {
        void getLoginInInfo(String username, String password);
        void onstart();
        void unSubscribe();

        void onLoginClick(String username, String password);

    }
    interface View extends BaseView<Presenter> {
        void showError(String error);
//        void setLoginInData(musicData starData);
        Boolean isACtive();
        void loginSuccess();
    }
}