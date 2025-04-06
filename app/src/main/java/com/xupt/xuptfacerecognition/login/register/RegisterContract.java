package com.xupt.xuptfacerecognition.login.register;

import com.xupt.xuptfacerecognition.login.BaseView;
import com.xupt.xuptfacerecognition.login.LoadTasksCallBack;

public interface RegisterContract {
    interface Model {
        void getRegisterInfo(String email, String password,String phone, LoadTasksCallBack callBack);
        void getVerificationCode(String email,LoadTasksCallBack callBack);
    }
    interface Presenter {
        void getRegisterInfo(String email, String password,String phone, String verificationCode);
        void onstart();
        void unSubscribe();

        void onRegisterClick(String email, String password,String phone);

        void getVerificationCode(String string);
    }
    interface View extends BaseView<Presenter> {
        void showError(String error);
        //        void setLoginInData(musicData starData);
        Boolean isACtive();

        void showSuccess(String data);
    }
}
