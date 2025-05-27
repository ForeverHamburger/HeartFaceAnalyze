package com.xupt.xuptfacerecognition.result;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.xupt.xuptfacerecognition.base.HeartRateUtils;
import com.xupt.xuptfacerecognition.base.TokenManager;
import com.xupt.xuptfacerecognition.databinding.ActivityResultBinding;
import com.xupt.xuptfacerecognition.info.DataParser;
import com.xupt.xuptfacerecognition.info.HeartRate;
import com.xupt.xuptfacerecognition.info.MMKVHeartRateStorage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ResultActivity extends AppCompatActivity implements ResultContract.View {

    private ActivityResultBinding binding;
    private LineChart lineChart;
    private RecyclerView rvBill;
    private long epochMilli;
    private List<DataParser.DataItem> mDataList;
    private ResultContract.ResultPresenter mPresenter;
    private long selectedDateMillis; // 存储选择的日期的毫秒数
    private String token = "";

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        lineChart = binding.lineChart;
        rvBill = binding.rvBill;

        setPresenter(new ResultPresenter(this, new ResultModel()));

        binding.ivRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!token.equals("")){
                    mPresenter.getDetectInfo(token);
                }
            }
        });

        initLineChart();
        initRecyclerView();
        initCalendarListener(); // 初始化日历监听器
    }


    @Override
    public void onStop() {
        super.onStop();
        // 取消注册
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getToken(TokenManager tokenManager) {
        token = tokenManager.getToken();
        Log.d("pic", "getToken: " + token);
        mPresenter.getDetectInfo(token);
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvBill.setLayoutManager(layoutManager);
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
        lineChart.setExtraOffsets(10f, 40f, 30f, 32f);

        // X轴设置
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return sdf.format(Calendar.getInstance().getTime());
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

    // 初始化日历监听器
    private void initCalendarListener() {
        binding.monthCalendar.setOnCalendarChangedListener(new OnCalendarChangedListener() {
            @Override
            public void onCalendarChange(int i, int i1, LocalDate localDate, DateChangeBehavior dateChangeBehavior) {

                LocalDateTime localDateTime = localDate.atStartOfDay();
                ZoneId zoneId = ZoneId.systemDefault();
                ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
                selectedDateMillis = zonedDateTime.toInstant().toEpochMilli();

                if (mDataList != null) {
                    updateData(mDataList);
                }

            }
        });
    }

    // 设置数据到图表和RecyclerView
    private void updateData(List<DataParser.DataItem> dataItemList) {
        List<DataParser.DataItem> dataList = dataItemList; // 假设这个方法获取最近的数据
        List<DataParser.DataItem> filteredDataList = DateUtils.filterDataByDate(dataList, selectedDateMillis);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (filteredDataList.size() == 0) {
                    binding.tvNull.setVisibility(View.VISIBLE);
                    binding.ivHourglass.setVisibility(View.VISIBLE);
                    binding.tvBillLcName.setVisibility(View.GONE);
                    binding.rvBill.setVisibility(View.GONE);
                    binding.tvBillRvName.setVisibility(View.GONE);
                    binding.lineChart.setVisibility(View.GONE);
                } else {
                    binding.tvNull.setVisibility(View.GONE);
                    binding.ivHourglass.setVisibility(View.GONE);
                    binding.tvBillLcName.setVisibility(View.VISIBLE);
                    binding.rvBill.setVisibility(View.VISIBLE);
                    binding.tvBillRvName.setVisibility(View.VISIBLE);
                    binding.lineChart.setVisibility(View.VISIBLE);
                }
            }
        });

        List<HeartRate> heartRates = new ArrayList<>();
        List<Entry> entries = new ArrayList<>();

        // 数据安全检查和处理
        for (DataParser.DataItem dataItem : filteredDataList) {
            if (dataItem != null && dataItem.getData() != null && dataItem.getStatus() != null) {
                float heartRateValue = 0;
                if (!dataItem.getData().equals("")) {
                    heartRateValue = HeartRateUtils.calculateAverage(dataItem.getData());
                }
                HeartRate heartRate = new HeartRate(heartRateValue, dataItem.getCreatedAt(), dataItem.getStatus());
                heartRates.add(heartRate);

                entries.add(new Entry(entries.size(), heartRateValue));
            }
        }

        // 更新图表数据
        LineDataSet dataSet = new LineDataSet(entries, "数据");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // 曲线类型
        dataSet.setColor(Color.parseColor("#F7983C")); // 折线颜色
        dataSet.setCircleColor(Color.parseColor("#32B5E6")); // 顶点颜色
        dataSet.setDrawCircles(false); // 不显示顶点圆点
        dataSet.setDrawFilled(true); // 填充曲线区域
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();

        // 更新RecyclerView数据
        BillShowAdapter billShowAdapter = new BillShowAdapter(heartRates, this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rvBill.setAdapter(billShowAdapter);
            }
        });
    }

    // 假设这个方法获取最近的数据
    private List<DataParser.DataItem> getRecentData() {
        // 这里需要根据你的实际情况实现获取数据的逻辑
        return new ArrayList<>();
    }

    @Override
    public void setPresenter(ResultContract.ResultPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showError(String error) {
        Log.e("ResultActivity", "Error: " + error);
    }

    @Override
    public Boolean isACtive() {
        return null;
    }

    @Override
    public void showSuccess(DataParser.Response data) {
        List<DataParser.DataItem> dataList = data.getDataList();
        mDataList = dataList;
        Log.d("TAG", "showSuccess: " + "获取了" + dataList.size() + "个数据");
        updateData(dataList); // 这里可以根据需要决定是否直接更新数据，或者先存储数据再根据日历选择更新
    }
}