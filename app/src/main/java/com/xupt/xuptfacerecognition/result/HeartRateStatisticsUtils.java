package com.xupt.xuptfacerecognition.result;


import com.xupt.xuptfacerecognition.info.HeartRate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HeartRateStatisticsUtils {
//
//    /**
//     * 获取某一天的所有心率数据
//     * @param heartRateList 心率数据列表
//     * @param targetDateMillis 目标日期的毫秒时间戳
//     * @return 该天的心率数据列表
//     */
//    public static List<HeartRate> getBillsByDate(List<HeartRate> heartRateList, long targetDateMillis) {
//        List<HeartRate> result = new ArrayList<>();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        String targetDateStr = sdf.format(new Date(targetDateMillis));
//        for (HeartRate heartRate : heartRateList) {
//            String billDateStr = sdf.format(new Date(heartRate.getDateMillis()));
//            if (billDateStr.equals(targetDateStr)) {
//                result.add(heartRate);
//            }
//        }
//        return result;
//    }
//
//    /**
//     * 获取近七天的所有心率数据
//     * @param heartRateList 心率数据列表
//     * @return 近七天的心率数据列表
//     */
//    public static List<HeartRate> getBillsInLastSevenDays(List<HeartRate> heartRateList) {
//        List<HeartRate> result = new ArrayList<>();
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DAY_OF_YEAR, -7);
//        long sevenDaysAgoMillis = calendar.getTimeInMillis();
//        long currentTimeMillis = System.currentTimeMillis();
//        for (HeartRate heartRate : heartRateList) {
//            long billDateMillis = heartRate.getDateMillis();
//            if (billDateMillis >= sevenDaysAgoMillis && billDateMillis <= currentTimeMillis) {
//                result.add(heartRate);
//            }
//        }
//        return result;
//    }

    /**
     * 统计心率数据的某种综合指标（这里假设类似盈亏的概念，根据具体需求定义）
     * @param heartRateList 心率数据列表
     * @return 综合指标数值
     */
    public static float calculateTotalProfitLoss(List<HeartRate> heartRateList) {
        float totalValue = 0;
        for (HeartRate heartRate : heartRateList) {
            float heartRateValue = heartRate.getHeartRate();
            // 这里假设根据心率值的某种计算逻辑来得到综合指标，具体计算方式可根据实际需求调整
            totalValue += heartRateValue;
        }
        return totalValue;
    }

//    /**
//     * 统计累计记录心率数据的天数
//     * @param heartRateList 心率数据列表
//     * @return 累计记录天数
//     */
//    public static int calculateRecordedDays(List<HeartRate> heartRateList) {
//        Set<String> recordedDates = new HashSet<>();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        for (HeartRate heartRate : heartRateList) {
//            String dateStr = sdf.format(new Date(heartRate.getDateMillis()));
//            recordedDates.add(dateStr);
//        }
//        return recordedDates.size();
//    }
}