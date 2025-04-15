package com.xupt.xuptfacerecognition.result;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.xupt.xuptfacerecognition.R;
import com.xupt.xuptfacerecognition.base.HeartRateUtils;
import com.xupt.xuptfacerecognition.info.HeartRate;

import java.util.List;

public class BillShowAdapter extends RecyclerView.Adapter<BillShowAdapter.BillShowViewHolder> {

    private List<HeartRate> heartRateInfoList;
    private Context context;

    public BillShowAdapter(List<HeartRate> heartRates, Context context) {
        this.heartRateInfoList = heartRates;
        this.context = context;
    }

    @NonNull
    @Override
    public BillShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_bill, parent, false);
        return new BillShowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillShowViewHolder holder, int position) {
        HeartRate heartRate = heartRateInfoList.get(position);
        if (heartRate.getHeartRate() == 0) {
            holder.detectNumber.setText("检测中...耐心等待");
        } else {
            holder.detectNumber.setText(String.valueOf((int)heartRate.getHeartRate()));
        }

        holder.detectTime.setText(heartRate.getDateTime());

        if (heartRate.getStatus().equals("2")) {
            holder.status.setText("检测成功");
        } else {
            holder.status.setText("检测中...");
        }

        // 添加从右侧滑入的动画
        Animation slideInAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f
        );
        slideInAnimation.setDuration(300); // 动画持续时间，单位毫秒
        slideInAnimation.setFillAfter(true); // 动画结束后保持最后状态
        holder.itemView.startAnimation(slideInAnimation);
        holder.bind(heartRate);
    }

    @Override
    public int getItemCount() {
        return heartRateInfoList.size();
    }

    public static class BillShowViewHolder extends RecyclerView.ViewHolder {
        TextView detectNumber;
        TextView detectTime;
        TextView status;
        ImageView image;

        public BillShowViewHolder(@NonNull View itemView) {
            super(itemView);
            detectNumber = itemView.findViewById(R.id.tv_detect_number);
            detectTime = itemView.findViewById(R.id.tv_detect_time);
            status = itemView.findViewById(R.id.tv_status);
            image = itemView.findViewById(R.id.iv_edit_user_head);
        }

        public void bind(HeartRate heartRate) {
            itemView.setOnClickListener(view -> {

                // 放大动画
                ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f);
                ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f);
                scaleXAnimator.setDuration(100);
                scaleYAnimator.setDuration(100);
                scaleXAnimator.start();
                scaleYAnimator.start();

                // 恢复原状动画
                scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1.1f, 1f);
                scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1.1f, 1f);
                scaleXAnimator.setStartDelay(100);
                scaleYAnimator.setStartDelay(100);
                scaleXAnimator.setDuration(100);
                scaleYAnimator.setDuration(100);
                scaleXAnimator.start();
                scaleYAnimator.start();

            });
        }

    }
}