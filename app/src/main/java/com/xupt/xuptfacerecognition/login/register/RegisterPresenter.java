package com.xupt.xuptfacerecognition.login.register;


import android.util.Log;

import com.xupt.xuptfacerecognition.base.VerificationRequestManager;
import com.xupt.xuptfacerecognition.login.LoadTasksCallBack;

public class RegisterPresenter implements RegisterContract.Presenter, LoadTasksCallBack<String> {
    private RegisterContract.View mView;
    private RegisterContract.Model mModel;
    private String Email;

    public RegisterPresenter(RegisterContract.View mView, RegisterContract.Model mModel)
    {
        this.mView = mView;
        this.mModel = mModel;
        mView.setPresenter(this);
    }


    @Override
    public void getRegisterInfo(String email, String password, String phone, String verificationCode) {
        mModel.getRegisterInfo(email, password, phone, this);

    }

    @Override
    public void onstart() {
    }

    @Override
    public void unSubscribe() {
        mModel = null;
        mView = null;
    }

    @Override
    public void onRegisterClick(String email, String password, String phone) {
//        if (!VerificationRequestManager.getInstance().isRequestValid(email)) {
//            onFailed("请先获取验证码");
//            return;
//        }
        Log.d("RegisterPresenter", "onRegisterClick: " + "走到这一步且没问题");
        mModel.getRegisterInfo(email, password, phone, this);

    }

    @Override
    public void getVerificationCode(String email) {
        Email= email;
        mModel.getVerificationCode(email, this);
    }

    @Override
    public void onSuccess(String data) {
        System.out.println(data);
        if (VerificationRequestManager.getInstance().addRequested(Email, data)) {
            Email = null; // 清除临时存储
        }
        if (mView!=null&&mView.isACtive()) {
            mView.showSuccess(data);
        }
    }

    @Override
    public void onFailed(String error) {
        if (mView!=null&&mView.isACtive()) {
            mView.showError(error);
        }
        System.out.println(error);

    }
}