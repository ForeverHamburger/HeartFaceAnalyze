package com.xupt.xuptfacerecognition.detector;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.xupt.xuptfacerecognition.databinding.ActivityDetectorBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetectorActivity extends AppCompatActivity {
    private ActivityDetectorBinding viewBinding;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private ExecutorService cameraExecutor;
    private static final String TAG = "DetectorActivity";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS;

    static {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        } else {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityDetectorBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

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

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    // Implements VideoCapture use case, including start and stop capturing.
    private void captureVideo() {
        if (videoCapture == null) return;

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
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video");
        }

        MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions
                .Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build();

        File outputFile = new File(getExternalFilesDir(null), "test.mp4");
        FileOutputOptions fileOutputOptions = new FileOutputOptions.Builder(outputFile)
                .build();

        recording = videoCapture.getOutput()
                .prepareRecording(this, fileOutputOptions)
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
                                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT)
                                        .show();
                                Log.d(TAG, msg);
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

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // Used to bind the lifecycle of cameras to the lifecycle owner
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    // Preview
                    Preview preview = new Preview.Builder()
                            .build();
                    preview.setSurfaceProvider(viewBinding.viewFinder.getSurfaceProvider());

                    Recorder recorder = new Recorder.Builder()
                            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                            .build();
                    videoCapture = VideoCapture.withOutput(recorder);

                    // Select back camera as a default
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

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
}