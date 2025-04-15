package com.xupt.xuptfacerecognition.base;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class HeartRateUtils {

    public static float calculateAverage(String data) {
        // 去除字符串两端的方括号
        data = data.replace("[", "").replace("]", "");
        // 按逗号分割字符串
        String[] parts = data.split(",");
        // 解析字符串为数字
        double num1 = Double.parseDouble(parts[0].trim());
        double num2 = Double.parseDouble(parts[1].trim());
        // 计算平均值
        return (float) ((num1 + num2) / 2);
    }

    public static long calculateTime(String data) {
        String dateTimeStr = "2025-04-13T20:47:37.787+08:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        LocalDateTime ldt = LocalDateTime.parse(dateTimeStr, formatter);
        long timestamp = ldt.toInstant(ZoneOffset.of("+08:00")).toEpochMilli();
        return timestamp;
    }


    public static String convertTimestampToFormattedString(long timestamp) {
        LocalDateTime ldt = LocalDateTime.ofEpochSecond(timestamp / 1000, (int) (timestamp % 1000 * 1000000), ZoneOffset.ofTotalSeconds(0));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M月d日HH:mm");
        return ldt.format(formatter);
    }
}
