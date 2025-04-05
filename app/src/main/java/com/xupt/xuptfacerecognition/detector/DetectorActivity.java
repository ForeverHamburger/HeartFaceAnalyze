package com.xupt.xuptfacerecognition.detector;
import android.Manifest;
import android.animation.TimeInterpolator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RotateDrawable;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Preconditions;
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;

import com.google.common.util.concurrent.ListenableFuture;
import com.xupt.xuptfacerecognition.R;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.animation.ValueAnimator;
import android.graphics.ImageFormat;

import java.io.ByteArrayOutputStream;

public class DetectorActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private PreviewView previewView;
    private ProgressBar progressBar;
    private ValueAnimator progressAnimator; // 进度动画
    private int targetProgress = 0; // 目标进度值
    private Button detectButton;
    private TextView resultTextView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private MediaRecorder mediaRecorder;
    private File outputFile;
    private boolean isRecording = false;
    private static final int RECORDING_DURATION = 30000; // 30 seconds in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detector);

        previewView = findViewById(R.id.preview_view);
        detectButton = findViewById(R.id.detect_button);
        resultTextView = findViewById(R.id.result_text_view);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setProgress(0);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }

        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "相机或录音权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void startRecording() {
        isRecording = true;
        detectButton.setText("停止录像");
        progressBar.setProgress(0);
        targetProgress = 100;
        startSmoothProgress(100);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        outputFile = new File(getExternalFilesDir(null), "recording.mp4");
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
        mediaRecorder.setVideoEncodingBitRate(1000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(640, 480);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isRecording) {
                        stopRecording();
                    }
                }
            }, RECORDING_DURATION);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        isRecording = false;
        detectButton.setText("开始录像");
        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
        progressBar.setProgress(100);

        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                Toast.makeText(this, "录像已保存到 " + outputFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    /**
     * 启动平滑进度动画
     * @param target 目标进度（0-100）
     */

    private void startSmoothProgress(int target) {
        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel(); // 取消正在运行的动画
        }

        int currentProgress = progressBar.getProgress();
        progressAnimator = ValueAnimator.ofInt(currentProgress, target);
        progressAnimator.setDuration(RECORDING_DURATION); // 动画时长30秒

        // 设置插值器
        TimeInterpolator interpolator = new AccelerateDecelerateInterpolator();
        progressAnimator.setInterpolator(interpolator);

        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                progressBar.setProgress(value);

                // 可选：进度条颜色随进度渐变（示例：蓝→绿）
                float fraction = animation.getAnimatedFraction();
                int color = evaluate(fraction,
                        Color.parseColor("#007BFF"), // 初始蓝色
                        Color.parseColor("#4CAF50")); // 完成绿色
                updateProgressBarColor(color);
            }
        });
        progressAnimator.start();
    }

    private int evaluate(float fraction, int startValue, int endValue) {
        int startA = (startValue >> 24) & 0xff;
        int startR = (startValue >> 16) & 0xff;
        int startG = (startValue >> 8) & 0xff;
        int startB = startValue & 0xff;

        int endA = (endValue >> 24) & 0xff;
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;

        return ((startA + (int) (fraction * (endA - startA))) << 24) |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                ((startB + (int) (fraction * (endB - startB))));
    }

    /**
     * 更新进度条颜色（需要自定义 ProgressBar 样式）
     */
    private void updateProgressBarColor(int color) {
        RotateDrawable rotateDrawable = (RotateDrawable) progressBar.getProgressDrawable();
        if (rotateDrawable != null) {
            GradientDrawable gradientDrawable = (GradientDrawable) rotateDrawable.getDrawable();
            if (gradientDrawable != null) {
                gradientDrawable.setStroke(4, color); // 设置外边框颜色
                gradientDrawable.setColor(color); // 设置进度颜色
            }
        }
    }
}