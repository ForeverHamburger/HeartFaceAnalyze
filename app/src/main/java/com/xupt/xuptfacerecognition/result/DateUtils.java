package com.xupt.xuptfacerecognition.result;


import android.util.Log;

import com.xupt.xuptfacerecognition.info.DataParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// 日期判断工具类
class DateUtils {
    public static List<DataParser.DataItem> filterDataByDate(List<DataParser.DataItem> dataList, long targetDateMillis) {
        List<DataParser.DataItem> filteredList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (DataParser.DataItem dataItem : dataList) {
            if (dataItem != null && dataItem.getCreatedAt() != null) {
                Log.d("TAG", "filterDataByDate: " + "?");
                try {
                    long dataDateMillis = sdf.parse(dataItem.getCreatedAt()).getTime();
                    Log.d("TAG", "filterDataByDate: " + dataDateMillis + "  " + targetDateMillis);

                    if (dataDateMillis == targetDateMillis) {
                        filteredList.add(dataItem);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return filteredList;
    }
}