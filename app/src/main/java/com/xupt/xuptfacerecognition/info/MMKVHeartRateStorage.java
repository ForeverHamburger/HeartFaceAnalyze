package com.xupt.xuptfacerecognition.info;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.mmkv.MMKV;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MMKVHeartRateStorage {
    private static final String HEART_RATES_KEY = "heart_rates_list";
    private final MMKV mmkv;

    public MMKVHeartRateStorage() {
        mmkv = MMKV.defaultMMKV();
    }

    public void saveHeartRate(HeartRate heartRate) {
        List<HeartRate> heartRates = getAllHeartRates();
        heartRates.add(heartRate);
        String json = new Gson().toJson(heartRates);
        mmkv.encode(HEART_RATES_KEY, json);
    }

    public void updateHeartRate(HeartRate heartRate) {
        List<HeartRate> heartRates = getAllHeartRates();
        for (int i = 0; i < heartRates.size(); i++) {
            if (heartRates.get(i).getDateMillis() == heartRate.getDateMillis()) {
                heartRates.set(i, heartRate);
                break;
            }
        }
        String json = new Gson().toJson(heartRates);
        mmkv.encode(HEART_RATES_KEY, json);
    }

    public List<HeartRate> getAllHeartRates() {
        String json = mmkv.decodeString(HEART_RATES_KEY, "[]");
        Type type = new TypeToken<List<HeartRate>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    public HeartRate getHeartRateByDate(long dateMillis) {
        List<HeartRate> heartRates = getAllHeartRates();
        for (HeartRate heartRate : heartRates) {
            if (heartRate.getDateMillis() == dateMillis) {
                return heartRate;
            }
        }
        return null;
    }

    public List<HeartRate> getHeartRatesByDateRange(long startDate, long endDate) {
        List<HeartRate> allHeartRates = getAllHeartRates();
        List<HeartRate> result = new ArrayList<>();

        for (HeartRate heartRate : allHeartRates) {
            if (heartRate.getDateMillis() >= startDate && heartRate.getDateMillis() <= endDate) {
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

    public void deleteHeartRate(long dateMillis) {
        List<HeartRate> heartRates = getAllHeartRates();
        for (int i = 0; i < heartRates.size(); i++) {
            if (heartRates.get(i).getDateMillis() == dateMillis) {
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