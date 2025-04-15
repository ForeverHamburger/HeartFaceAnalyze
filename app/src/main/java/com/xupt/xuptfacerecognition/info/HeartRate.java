package com.xupt.xuptfacerecognition.info;

import java.io.Serializable;
import java.io.Serializable;

public class HeartRate implements Serializable {
    private float heartRate;
    private String dateTime;
    private String status;

    public HeartRate(float heartRate, String dateTime, String status) {
        this.heartRate = heartRate;
        this.dateTime = dateTime;
        this.status = status;
    }

    public HeartRate(float heartRate, String dateTime) {
        this.heartRate = heartRate;
        this.dateTime = dateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(float heartRate) {
        this.heartRate = heartRate;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "HeartRate{" +
                "heartRate=" + heartRate +
                ", dateTime=" + dateTime +
                ", status='" + status + '\'' +
                '}';
    }
}