package com.xupt.xuptfacerecognition.info;

import java.io.Serializable;

public class HeartRate implements Serializable {

    private float heartRate;
    // 账单日期（毫秒时间戳）
    private long dateMillis;

    public HeartRate(float heartRate, long dateMillis) {
        this.heartRate = heartRate;
        this.dateMillis = dateMillis;
    }

    public float getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(float heartRate) {
        this.heartRate = heartRate;
    }

    public long getDateMillis() {
        return dateMillis;
    }

    public void setDateMillis(long dateMillis) {
        this.dateMillis = dateMillis;
    }
}