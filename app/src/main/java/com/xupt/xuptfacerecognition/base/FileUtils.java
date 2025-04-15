package com.xupt.xuptfacerecognition.base;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class FileUtils {

    /**
     * 将文件保存到本地（支持私有目录或公共媒体目录）
     * @param context       上下文
     * @param outputFile    目标文件对象
     * @param isPublicMedia 是否保存到公共媒体目录（如相册，需WRITE_EXTERNAL_STORAGE权限）
     * @return              保存是否成功
     */
    public static boolean saveFileToLocal(Context context, File outputFile, boolean isPublicMedia) {
        // 1. 确保父目录存在
        File parentDir = outputFile.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            Log.e("FileUtils", "创建父目录失败: " + parentDir.getAbsolutePath());
            return false;
        }

        // 2. 检查文件是否可写（私有目录无需额外权限，公共目录需WRITE_EXTERNAL_STORAGE权限）
        if (isPublicMedia && !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.e("FileUtils", "外部存储不可用");
            return false;
        }

        // 3. 模拟文件写入（如果是MediaRecorder等已自动写入，可跳过此步）
        // 注意：MediaRecorder会自动写入outputFile，此步仅示例普通文件写入
            /*
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                String content = "文件内容";
                fos.write(content.getBytes());
            }
            */

        // 4. 通知媒体库更新（如保存到相册）
        if (isPublicMedia) {
            updateMediaLibrary(context, outputFile);
        }

        return true;
    }

    /**
     * 通知媒体库更新文件（使文件显示在相册中）
     */
    private static void updateMediaLibrary(Context context, File file) {
        if (file == null || !file.exists()) return;

        // 创建媒体扫描Intent
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri fileUri = Uri.fromFile(file);
        mediaScanIntent.setData(fileUri);
        context.sendBroadcast(mediaScanIntent);
    }
}