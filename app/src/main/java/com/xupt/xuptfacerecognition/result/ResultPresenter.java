package com.xupt.xuptfacerecognition.result;

import com.google.gson.Gson;
import com.xupt.xuptfacerecognition.info.DataParser;
import com.xupt.xuptfacerecognition.login.LoadTasksCallBack;

public class ResultPresenter implements ResultContract.ResultPresenter, LoadTasksCallBack<String> {
    private ResultContract.View view;
    private ResultContract.ResultModel model;

    public ResultPresenter(ResultContract.View view, ResultContract.ResultModel model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void getDetectInfo(String token) {
        model.getDetectInfo(token,this);
    }

    @Override
    public void onSuccess(String data) {
        // 创建 Gson 对象
        Gson gson = new Gson();

        // 解析 JSON 数据到 Response 对象
        DataParser.Response response = gson.fromJson(data, DataParser.Response.class);

        view.showSuccess(response);
    }

    @Override
    public void onFailed(String error) {

    }
}
