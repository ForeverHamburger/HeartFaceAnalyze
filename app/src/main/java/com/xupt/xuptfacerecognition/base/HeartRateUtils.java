package com.xupt.xuptfacerecognition.base;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class HeartRateUtils {

    public static float calculateAverage(String data) {
        // 去除所有方括号和空格
        data = data.replaceAll("[\\[\\] ]", "");
        // 按逗号分割字符串
        String[] parts = data.split(",");

        double sum = 0;
        int count = 0;

        for (String part : parts) {
            // 跳过空字符串（例如输入为"1,,2"时）
            if (!part.trim().isEmpty()) {
                sum += Double.parseDouble(part.trim());
                count++;
            }
        }

        // 处理无有效数字的情况（避免除以零）
        if (count == 0) {
            return 0f;
        }

        return (float) (sum / count);
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
