package com.xupt.xuptfacerecognition.detector;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;

import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;

import com.google.common.util.concurrent.ListenableFuture;
import com.xupt.xuptfacerecognition.R;
import com.xupt.xuptfacerecognition.base.TokenManager;
import com.xupt.xuptfacerecognition.databinding.ActivityDetectorBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetectorActivity extends AppCompatActivity implements DetectorContract.DetectorView{
    private ActivityDetectorBinding viewBinding;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private ExecutorService cameraExecutor;
    private String token = null;
    private CameraSelector currentCamera = CameraSelector.DEFAULT_BACK_CAMERA; // 初始化为后置
    private static final String TAG = "TAG";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS;
    private DetectorContract.DetectorPresenter mPresenter;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private boolean isRunning = false;
    private Preview preview;


    // 创建计时更新任务
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            long elapsedMillis = currentTime - startTime;

            // 格式化时间为 HH:mm:ss.SSS
            int hours = (int) (elapsedMillis / 3600000);
            int minutes = (int) (elapsedMillis % 3600000) / 60000;
            int seconds = (int) (elapsedMillis % 60000) / 1000;
            int milliseconds = (int) (elapsedMillis % 1000);

            String time = String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
            viewBinding.tvSeconds.setText(time);

            // 每1毫秒更新一次（实际精度取决于系统）
            timerHandler.postDelayed(this, 1);
        }
    };

    static {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        } else {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        }
    }

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
        viewBinding = ActivityDetectorBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        setPresenter(new DetectorPresenter(this,new DetectorModel()));
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        viewBinding.videoCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureVideo();
            }
        });

        viewBinding.ivSwith.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera(); // 调用切换摄像头方法
            }
        });

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void switchCamera() {
        // 切换摄像头类型：后置 ↔ 前置
        currentCamera = (currentCamera == CameraSelector.DEFAULT_BACK_CAMERA)
                ? CameraSelector.DEFAULT_FRONT_CAMERA
                : CameraSelector.DEFAULT_BACK_CAMERA;
        preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(viewBinding.viewFinder.getSurfaceProvider());
        // 重新绑定相机（需先解绑当前相机）
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(DetectorActivity.this).get();
                cameraProvider.unbindAll(); // 解绑所有用例
                cameraProvider.bindToLifecycle(DetectorActivity.this, currentCamera, preview, videoCapture); // 重新绑定新摄像头
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
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
    }
    // Implements VideoCapture use case, including start and stop capturing.
    private void captureVideo() {
        if (videoCapture == null) return;

        if (!isRunning) {
            // 开始计时
            startTime = System.currentTimeMillis();
            isRunning = true;
            timerHandler.post(timerRunnable);
        } else {
            // 停止计时
            timerHandler.removeCallbacks(timerRunnable);
            isRunning = false;
        }

        viewBinding.videoCaptureButton.setEnabled(false);

        if (recording != null) {
            // Stop the current recording session.
            recording.stop();
            recording = null;
            return;
        }

        // create and start a new recording session
        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "file");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video");
        }

        MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions
                .Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build();



        recording = videoCapture.getOutput()
                .prepareRecording(this, mediaStoreOutputOptions)
                .start(ContextCompat.getMainExecutor(this), new Consumer<VideoRecordEvent>() {
                    @Override
                    public void accept(VideoRecordEvent recordEvent) {
                        if (recordEvent instanceof VideoRecordEvent.Start) {
                            viewBinding.videoCaptureButton.setText(getString(R.string.stop_capture));
                            viewBinding.videoCaptureButton.setEnabled(true);
                        } else if (recordEvent instanceof VideoRecordEvent.Finalize) {
                            if (!((VideoRecordEvent.Finalize) recordEvent).hasError()) {
                                String msg = "Video capture succeeded: " +
                                        ((VideoRecordEvent.Finalize) recordEvent).getOutputResults().getOutputUri();
                                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, msg);
//
//                                Uri outputUri = ((VideoRecordEvent.Finalize) recordEvent).getOutputResults().getOutputUri();
//                                File videoFileFromUriQAndAbove = getVideoFileFromUriQAndAbove(getBaseContext(), outputUri);

                                // 获取视频文件的Uri
                                Uri outputUri = ((VideoRecordEvent.Finalize) recordEvent).getOutputResults().getOutputUri();
                                // 将Uri转换为File对象
                                File videoFile = getVideoFileFromUri(outputUri);

                                // 检查视频时长
                                long duration = getVideoDuration(videoFile);
                                Toast.makeText(DetectorActivity.this, "视频时长" + duration, Toast.LENGTH_SHORT).show();
                                if (duration >= 0) { // 25 秒 = 25000 毫秒
                                    // 打印文件大小
                                    float fileSizeInMb = videoFile.length() / (1024f * 1024f);
                                    Log.d(TAG, "Video file size: " + String.format("%.2f", fileSizeInMb) + " MB");
                                    // 调用 Presenter 层方法
                                    if (mPresenter != null && token != null) {
                                        Log.d(TAG, "accept: " + "开始发送");
                                        mPresenter.sendVideoInfo(getBaseContext(), videoFile, token);
                                    }
                                }
                            } else {
                                if (recording != null) {
                                    recording.close();
                                    recording = null;
                                }
                                Log.e(TAG, "Video capture ends with error: " +
                                        ((VideoRecordEvent.Finalize) recordEvent).getError());
                            }
                            viewBinding.videoCaptureButton.setText(getString(R.string.start_capture));
                            viewBinding.videoCaptureButton.setEnabled(true);
                        }
                    }
                });
    }


    private File getVideoFileFromUri(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return new File(filePath);
        }
        return null;
    }

    private static File getVideoFileFromUriQAndAbove(Context context, Uri outputUri) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            // 获取文件名
            String[] projection = {MediaStore.Video.Media.DISPLAY_NAME};
            Cursor cursor = contentResolver.query(outputUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                String displayName = cursor.getString(columnIndex);
                cursor.close();

                // 获取内部存储的Movies/CameraX-Video目录
                File internalMoviesDir = new File(context.getFilesDir(), "Movies/CameraX-Video");
                if (!internalMoviesDir.exists()) {
                    if (!internalMoviesDir.mkdirs()) {
                        // 目录创建失败，处理错误
                        return null;
                    }
                }

                // 构建完整的文件路径
                File videoFile = new File(internalMoviesDir, displayName);
                return videoFile;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private long getVideoDuration(File videoFile) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoFile.getAbsolutePath());
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return Long.parseLong(durationStr);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // Used to bind the lifecycle of cameras to the lifecycle owner
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    // Preview
                    preview = new Preview.Builder()
                            .build();
                    preview.setSurfaceProvider(viewBinding.viewFinder.getSurfaceProvider());

                    Recorder recorder = new Recorder.Builder()
                            .setQualitySelector(QualitySelector.from(Quality.LOWEST))
                            .build();
                    videoCapture = VideoCapture.withOutput(recorder);

                    // Select back camera as a default
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                    try {
                        // Unbind use cases before rebinding
                        cameraProvider.unbindAll();

                        // Bind use cases to camera
                        cameraProvider
                                .bindToLifecycle(DetectorActivity.this, cameraSelector, preview, videoCapture);
                    } catch (Exception exc) {
                        Log.e(TAG, "Use case binding failed", exc);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    getBaseContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(
                        this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT
                ).show();
                finish();
            }
        }
    }

    @Override
    public void showError(String error) {

    }

    @Override
    public Boolean isACtive() {
        return null;
    }

    @Override
    public void showSuccess(String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DetectorActivity.this, "数据上传成功！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void setPresenter(DetectorContract.DetectorPresenter presenter) {
        mPresenter = presenter;
    }
}