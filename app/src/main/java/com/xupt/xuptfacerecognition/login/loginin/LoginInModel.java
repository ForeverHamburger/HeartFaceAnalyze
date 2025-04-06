package com.xupt.xuptfacerecognition.login.loginin;

import android.util.Log;

import com.xupt.xuptfacerecognition.base.NetworkHelper;
import com.xupt.xuptfacerecognition.login.LoadTasksCallBack;

public class LoginInModel implements LoginInContract.Model {
    private final NetworkHelper networkHelper = new NetworkHelper();
    @Override
    public void getLoginInInfo(String phoneoremail, String password, LoadTasksCallBack callBack) {
        Log.e("TAG", "getLoginInInfo: " );
        // 创建请求参数对象，添加用户名和密码作为参数
//        RequestParams params = new RequestParams();
//        params.put("phoneoremail", phoneoremail);
//        params.put("password", password);
//        networkHelper.performPostRequest(URL.LOGIN_LOGIN_URL, params, callBack);
    }

    public static void getSomeDate() {
        // 这个方法可以用来实现其他数据获取逻辑，比如获取一些额外的信息
    }

}