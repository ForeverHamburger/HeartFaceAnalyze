package com.xupt.xuptfacerecognition.result;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.necer.enumeration.DateChangeBehavior;
import com.necer.listener.OnCalendarChangedListener;
import com.xupt.xuptfacerecognition.R;
import com.xupt.xuptfacerecognition.databinding.ActivityResultBinding;
import com.xupt.xuptfacerecognition.info.HeartRate;
import com.xupt.xuptfacerecognition.info.MMKVHeartRateStorage;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {

    private ActivityResultBinding binding;
    private LineChart lineChart;
    private PieChart pieChart;
    private boolean pieAccount = true;
    private long epochMilli;
    private MMKVHeartRateStorage mmkvHeartRateStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        lineChart = binding.lineChart;
        pieChart = binding.pieChart;

        mmkvHeartRateStorage = new MMKVHeartRateStorage();
        initLineChart();
        setLineChartData();
        initPieChart();
//        setPieChartData(pieAccount);
        setCalendar();
        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvBill.setLayoutManager(layoutManager);
        List<HeartRate> heartRates = mmkvHeartRateStorage.getAllHeartRates();
        BillShowAdapter billShowAdapter = new BillShowAdapter(heartRates, this);
        binding.rvBill.setAdapter(billShowAdapter);
    }

    private void initRecyclerView(long i) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvBill.setLayoutManager(layoutManager);
        List<HeartRate> heartRates = mmkvHeartRateStorage.getAllHeartRates();
        // 这里假设 BillStatisticsUtils 中相应方法已适配 HeartRate 类
        List<HeartRate> billsByDate = HeartRateStatisticsUtils.getBillsByDate(heartRates, i);
        BillShowAdapter billShowAdapter = new BillShowAdapter(billsByDate, this);
        binding.rvBill.setAdapter(billShowAdapter);
    }

    private void setCalendar() {
        binding.monthCalendar.setOnCalendarChangedListener(new OnCalendarChangedListener() {
            @Override
            public void onCalendarChange(int i, int i1, LocalDate localDate, DateChangeBehavior dateChangeBehavior) {
                binding.tvMonth.setText(i1 + "月");
                binding.tvYear.setText(i + "年");
                LocalDateTime localDateTime = localDate.atStartOfDay();
                ZoneId zoneId = ZoneId.systemDefault();
                ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
                epochMilli = zonedDateTime.toInstant().toEpochMilli();
                initRecyclerView(epochMilli);
            }
        });
    }

    // 初始化图表
    private void initLineChart() {
        lineChart.setBackgroundColor(Color.TRANSPARENT);
        lineChart.setDescription(null); // 隐藏描述
        lineChart.setTouchEnabled(false); // 禁用触摸
        lineChart.setDrawGridBackground(false);
        lineChart.setScaleEnabled(false);
        lineChart.animateY(1000); // Y轴入场动画
        lineChart.getLegend().setEnabled(false);
        lineChart.setExtraOffsets(10f,40f,30f,32f);

        // X轴设置
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                String[] labels = {"MON", "TUE", "WNE", "THR", "FRI", "SAT", "SUN"};
                return labels[(int) value];
            }
        });

        // Y轴设置
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMaximum(200f);
        yAxis.setAxisMinimum(0f);
        yAxis.enableGridDashedLine(6f, 6f, 0f);
        lineChart.getAxisRight().setEnabled(false); // 隐藏右侧Y轴

        // 启用点击事件
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    // 设置数据
    private void setLineChartData() {
        // 获取近七天的心率数据
        List<HeartRate> lastSevenDaysHeartRates = HeartRateStatisticsUtils.getBillsInLastSevenDays(mmkvHeartRateStorage.getAllHeartRates());
        // 这里假设 calculateDailyExpenseAmount 方法已适配 HeartRate 类，用于计算每天的心率相关数据
        Map<Long, Float> dailyProfitLoss = calculateDailyExpenseAmount(lastSevenDaysHeartRates);
        // 将统计结果转换为 Entry 对象列表
        List<Entry> entries = convertToEntries(dailyProfitLoss);

        autoAdjustYAxis(entries);

        LineDataSet dataSet = new LineDataSet(entries, "数据");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // 曲线类型
        dataSet.setColor(Color.parseColor("#F7983C")); // 折线颜色
        dataSet.setCircleColor(Color.parseColor("#32B5E6")); // 顶点颜色
        dataSet.setDrawCircles(false); // 不显示顶点圆点
        dataSet.setDrawFilled(true); // 填充曲线区域
        dataSet.setValueTextSize(10f);

        // 设置填充样式（API 18+）
        if (Build.VERSION.SDK_INT >= 18) {
            dataSet.setFillDrawable(ContextCompat.getDrawable(this, R.drawable.bg_color));
        } else {
            dataSet.setFillColor(Color.LTGRAY);
        }

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }


    // 统计近七天的心率相关数据，这里假设与心率有关的计算逻辑
    private Map<Long, Float> calculateDailyExpenseAmount(List<HeartRate> heartRates) {
        Map<Long, Float> dailyExpenseAmount = new HashMap<>();
        // 初始化近七天的日期，将每天的心率数据初始值设为 0
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        for (int i = 0; i < 7; i++) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long dayStartMillis = calendar.getTimeInMillis();
            dailyExpenseAmount.put(dayStartMillis, 0f);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (HeartRate heartRate : heartRates) {
            long dateMillis = heartRate.getDateMillis();
            Calendar billCalendar = Calendar.getInstance();
            billCalendar.setTimeInMillis(dateMillis);
            billCalendar.set(Calendar.HOUR_OF_DAY, 0);
            billCalendar.set(Calendar.MINUTE, 0);
            billCalendar.set(Calendar.SECOND, 0);
            billCalendar.set(Calendar.MILLISECOND, 0);
            long dayStartMillis = billCalendar.getTimeInMillis();

            float heartRateValue = heartRate.getHeartRate();
            dailyExpenseAmount.put(dayStartMillis, dailyExpenseAmount.get(dayStartMillis) + heartRateValue);
        }
        return dailyExpenseAmount;
    }

    // 将统计结果转换为 Entry 对象列表
    private List<Entry> convertToEntries(Map<Long, Float> dailyExpenseAmount) {
        List<Entry> entries = new ArrayList<>();
        List<Long> sortedDates = new ArrayList<>(dailyExpenseAmount.keySet());
        Collections.sort(sortedDates);
        int index = 0;
        for (Long dateMillis : sortedDates) {
            float expenseAmount = dailyExpenseAmount.get(dateMillis);
            entries.add(new Entry(index++, expenseAmount));
        }
        return entries;
    }
    private void autoAdjustYAxis(List<Entry> entries) {
        if (entries.isEmpty()) {
            return;
        }

        float minValue = Float.MAX_VALUE;
        float maxValue = Float.MIN_VALUE;

        // 找出数据集中的最大值和最小值
        for (Entry entry : entries) {
            float value = entry.getY();
            if (value < minValue) {
                minValue = value;
            }
            if (value > maxValue) {
                maxValue = value;
            }
        }

        // 添加一些额外的空间，避免数据点紧贴坐标轴
        float padding = (maxValue - minValue) * 0.1f;
        minValue = Math.max(0, minValue - padding);
        maxValue = maxValue + padding;

        // 设置 Y 轴的最大值和最小值
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(minValue);
        leftAxis.setAxisMaximum(maxValue);

        // 刷新图表
        lineChart.invalidate();
    }


    // 初始化饼状图
    private void initPieChart() {
        pieChart.setUsePercentValues(true); // 使用百分比显示
        pieChart.getDescription().setEnabled(false); // 隐藏描述
        pieChart.setExtraOffsets(5, 20, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setRotationAngle(0);
        pieChart.setTouchEnabled(true);


        //设置中心部分的字  （一般中间白色圆不隐藏的情况下才设置）
        pieChart.setCenterText("总收入");
        //设置中心字的字体颜色
        pieChart.setCenterTextColor(Color.BLACK);
        //设置中心字的字体大小
        pieChart.setCenterTextSize(12f);
//
//        pieChart.setOnChartGestureListener(new OnChartGestureListener() {
//            @Override
//            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
//            @Override
//            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture){}
//            @Override
//            public void onChartLongPressed(MotionEvent me) {}
//            @Override
//            public void onChartDoubleTapped(MotionEvent me) {}
//            @Override
//            public void onChartSingleTapped(MotionEvent me) {
//                if (!pieAccount) {
//                    pieChart.setCenterText("支出");
//                    setPieChartData(pieAccount);
//                } else {
//                    pieChart.setCenterText("收入");
//                    setPieChartData(pieAccount);
//                }
//                pieAccount = !pieAccount;
//                pieChart.animateY(1400);
//            }
//            @Override
//            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {}
//            @Override
//            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {}
//            @Override
//            public void onChartTranslate(MotionEvent me, float dX, float dY) {}
//        });

        // 设置饼图大小
        pieChart.setExtraOffsets(20f,30f,20f,40f);

        // 启用旋转
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        // 去掉图例
        pieChart.getLegend().setEnabled(false);

        // 设置出场动画
        pieChart.animateY(700);
    }

    // 设置饼状图数据
//    private void setPieChartData(Boolean isIncome) {
//        // 统计分类数据，这里假设统计心率相关分类数据
//        Map<String, Float> categoryTotal =
//
//        List<PieEntry> entries = new ArrayList<>();
//        for (Map.Entry<String, Float> entry : categoryTotal.entrySet()) {
//            String category = entry.getKey();
//            float amount = entry.getValue();
//            entries.add(new PieEntry(amount, category));
//        }
//
//        PieDataSet dataSet = new PieDataSet(entries, "数据");
//
//        // 设置连接线的长度
//        dataSet.setValueLinePart1Length(0.4f);
//        // 设置连接线第二段的长度
//        dataSet.setValueLinePart2Length(0.2f);
//        // 数据连接线距图形片内部边界的距离，为百分数(0~100f)
//        dataSet.setValueLinePart1OffsetPercentage(80f);
//        // 设置X值在圆外显示
//        dataSet.setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
//        // 设置Y值在圆外显示
//        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
//
//        // 设置连接线的颜色
//        dataSet.setValueLineColor(Color.BLACK);
//        // 设置文字的颜色，同时作用于X值和Y值
//        dataSet.setValueTextColor(Color.BLACK);
//
//        // 设置连接线宽度
//        dataSet.setValueLineWidth(1f);
//        dataSet.setSliceSpace(3f);
//        dataSet.setSelectionShift(5f);
//
//        // 设置颜色
//        ArrayList<Integer> colors = new ArrayList<>();
//        int[] ints = generateGradientColors(Color.parseColor("#F7983C"), Color.parseColor("#FFFF00"), entries.size());
//        for (Integer color : ints) {
//            colors.add(color);
//        }
//        dataSet.setColors(colors);
//
//        PieData data = new PieData(dataSet);
//        // 开启在饼状图上显示值
//        data.setValueFormatter(new PercentFormatter(pieChart));
//        data.setValueTextSize(16f);
//        // 再次确认设置文字颜色
//        data.setValueTextColor(Color.BLACK);
//
//        pieChart.setData(data);
//        pieChart.invalidate();
//    }

    public static int[] generateGradientColors(int startColor, int endColor, int steps) {
        int[] colors = new int[steps];
        float[] hslStart = new float[3];
        float[] hslEnd = new float[3];

        android.graphics.Color.RGBToHSV(android.graphics.Color.red(startColor), android.graphics.Color.green(startColor), android.graphics.Color.blue(startColor), hslStart);
        android.graphics.Color.RGBToHSV(android.graphics.Color.red(endColor), android.graphics.Color.green(endColor), android.graphics.Color.blue(endColor), hslEnd);

        for (int i = 0; i < steps; i++) {
            float fraction = (float) i / (steps - 1);
            float[] hsl = {
                    hslStart[0] + fraction * (hslEnd[0] - hslStart[0]),
                    hslStart[1] + fraction * (hslEnd[1] - hslStart[1]),
                    hslStart[2] + fraction * (hslEnd[2] - hslStart[2])
            };
            colors[i] = android.graphics.Color.HSVToColor(hsl);
        }
        return colors;
    }
}