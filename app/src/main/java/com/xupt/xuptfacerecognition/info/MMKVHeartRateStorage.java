package com.xupt.xuptfacerecognition.info;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.mmkv.MMKV;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.mmkv.MMKV;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MMKVHeartRateStorage {
    private static final String HEART_RATES_KEY = "heart_rates_list";
    private final MMKV mmkv;
    private static final Logger logger = Logger.getLogger(MMKVHeartRateStorage.class.getName());

    public MMKVHeartRateStorage() {
        mmkv = MMKV.defaultMMKV();
    }

    public void saveHeartRate(HeartRate heartRate) {
        List<HeartRate> heartRates = getAllHeartRates();
        boolean isDuplicate = false;
        String newDateTime = heartRate.getDateTime();

        // 检查是否存在相同时间的 HeartRate 对象
        for (HeartRate hr : heartRates) {
            if (hr.getDateTime().equals(newDateTime)) {
                isDuplicate = true;
                updateHeartRate(heartRate);
                logger.info("saveHeartRate: " + "更新！");
                break;
            }
        }

        // 如果不存在相同时间的对象，则添加新的 HeartRate 对象
        if (!isDuplicate) {
            heartRates.add(heartRate);
            String json = new Gson().toJson(heartRates);
            mmkv.encode(HEART_RATES_KEY, json);
        }
    }

    public void updateHeartRate(HeartRate heartRate) {
        List<HeartRate> heartRates = getAllHeartRates();
        for (int i = 0; i < heartRates.size(); i++) {
            if (heartRates.get(i).getDateTime().equals(heartRate.getDateTime())) {
                heartRates.set(i, heartRate);
                break;
            }
        }
        String json = new Gson().toJson(heartRates);
        mmkv.encode(HEART_RATES_KEY, json);
    }

    public List<HeartRate> getAllHeartRates() {
        String json = mmkv.decodeString(HEART_RATES_KEY, "[]");
        java.lang.reflect.Type type = new TypeToken<List<HeartRate>>() {
        }.getType();
        return new Gson().fromJson(json, type);
    }

    public HeartRate getHeartRateByDate(String dateTime) {
        List<HeartRate> heartRates = getAllHeartRates();
        for (HeartRate heartRate : heartRates) {
            if (heartRate.getDateTime().equals(dateTime)) {
                return heartRate;
            }
        }
        return null;
    }

    public List<HeartRate> getHeartRatesByDateRange(String startDate, String endDate) {
        List<HeartRate> allHeartRates = getAllHeartRates();
        List<HeartRate> result = new ArrayList<>();

        for (HeartRate heartRate : allHeartRates) {
            String heartRateDateTime = heartRate.getDateTime();
            if (heartRateDateTime.compareTo(startDate) >= 0 && heartRateDateTime.compareTo(endDate) <= 0) {
                result.add(heartRate);
            }
        }
        return result;
    }

    public float getAverageHeartRate() {
        List<HeartRate> heartRates = getAllHeartRates();
        if (heartRates.isEmpty()) {
            return 0;
        }
        float total = 0;
        for (HeartRate heartRate : heartRates) {
            total += heartRate.getHeartRate();
        }
        return total / heartRates.size();
    }

    public void deleteHeartRate(String dateTime) {
        List<HeartRate> heartRates = getAllHeartRates();
        for (int i = 0; i < heartRates.size(); i++) {
            if (heartRates.get(i).getDateTime().equals(dateTime)) {
                heartRates.remove(i);
                break;
            }
        }
        String json = new Gson().toJson(heartRates);
        mmkv.encode(HEART_RATES_KEY, json);
    }

    public void clearAllData() {
        mmkv.clear();
    }
}