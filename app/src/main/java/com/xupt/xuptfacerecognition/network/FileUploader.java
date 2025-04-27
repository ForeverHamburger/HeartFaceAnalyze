package com.xupt.xuptfacerecognition.network;


import android.util.Log;

import androidx.annotation.NonNull;

import com.xupt.xuptfacerecognition.login.LoadTasksCallBack;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class FileUploader {
    private static final int CHUNK_SIZE = 256 * 1024; // 0.25MB分块大小
    private static final String TAG = "FileUploader";
    private static OkHttpClient client;
    private static final int CONCURRENT_UPLOADS = 3; // 并发上传的分块数量
    private final ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_UPLOADS);

    static {
        try {
            // 自定义证书
            String certificateStr = "-----BEGIN CERTIFICATE-----\n" +
                    "MIIDrzCCApcCFCWKkdltRgvja9jEs2Lh/goV9xm6MA0GCSqGSIb3DQEBCwUAMIGT\n" +
                    "MQswCQYDVQQGEwJDTjEQMA4GA1UECAwHU2hhYW54aTERMA8GA1UEBwwIWGlhbllh\n" +
                    "bmcxEzARBgNVBAoMCk15IENvbXBhbnkxFDASBgNVBAsMC0RldmVsb3BtZW50MRIw\n" +
                    "EAYDVQQDDAlsb2NhbGhvc3QxIDAeBgkqhkiG9w0BCQEWETIzMTIwNTUwMDZAcXEu\n" +
                    "Y29tMB4XDTI1MDEyMjA4MDIxMFoXDTI2MDEyMjA4MDIxMFowgZMxCzAJBgNVBAYT\n" +
                    "AkNOMRAwDgYDVQQIDAdTaGFhbnhpMREwDwYDVQQHDAhYaWFuWWFuZzETMBEGA1UE\n" +
                    "CgwKTXkgQ29tcGFueTEUMBIGA1UECwwLRGV2ZWxvcG1lbnQxEjAQBgNVBAMMCWxv\n" +
                    "Y2FsaG9zdDEgMB4GCSqGSIb3DQEJARYRMjMxMjA1NTAwNkBxcS5jb20wggEiMA0G\n" +
                    "CSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCkUMalu7n48GFrX218CuBbMPotGVok\n" +
                    "wQry1iYBN25+3fSDJrGy5DVYLO4cHFXWv0iEv6LlwybgeKhVjuMF3XBuhf6LBsv0\n" +
                    "rOO5WWJ+N9A9Mmby4XjpyEavYUO3WpPbW6DpwLghE2o4HrCC3dCybiZUpN5jBHKn\n" +
                    "ghJd/hsBR4D8tTckr2yLZ5f3LK0iomhiIQBYe1wDF+qswGGDf3UkxzhPA8ct0nvQ\n" +
                    "aZDXelRwXL/81X1DmP2VYOQSlgAtI1/ELviTizevmKpuzIZt+pnAcWEerpsF0a+U\n" +
                    "NcZxEQQBrJCQozKbbIJsz16yqmMKyjtfCxCyFuZl/FfjTP/xhwOo6DYxAgMBAAEw\n" +
                    "DQYJKoZIhvcNAQELBQADggEBAHAEtkQ7veZrZEeJ+ABYLbL9YNycMvDEFYlmN/fU\n" +
                    "8FxvY1T+Jq2xOApPOFYphRo7kqlNORIXvnS3c2olBWPgSEyIph8ltOn+2j53PZGF\n" +
                    "HBusYTmQxc/SvIE+XncB2l9hBM1BKffoTzm2G1zURICoEc+C+7HAfvW5Lxf2AdDX\n" +
                    "qiGDP3Pu33zFAIuaMQpR3LVOKYpa2LUBMQy38jqSByIbDZ/Di0ywSET7U6x40uaK\n" +
                    "kpb/Gu//7ffPjKzcVPDk4IsGierndSEon/JhtK5t5de/Qudq7VJNhh1T/zmmO31u\n" +
                    "2oEODfz8+nkyPWexdwrH1hSZp4IxvZB0fnl2MonoZ6kyjR4=\n" +
                    "-----END CERTIFICATE-----";
            ByteArrayInputStream certificateStream = new ByteArrayInputStream(certificateStr.getBytes());

            // 创建混合 TrustManager
            MixedTrustManager mixedTrustManager = new MixedTrustManager(new InputStream[]{certificateStream});

            // 创建 SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{mixedTrustManager}, null);

            // 配置 OkHttpClient
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .followRedirects(true)
                    .sslSocketFactory(sslContext.getSocketFactory(), mixedTrustManager)
                    .hostnameVerifier((hostname, session) -> true);

            client = builder.build();
        } catch (Exception e) {
            throw new RuntimeException("初始化 OkHttpClient 时发生错误", e);
        }
    }

    public FileUploader() {

    }

    public void sendDetectVideo(File file, String token,String fileMD5String, LoadTasksCallBack callBack) {
        long fileSize = file.length();
        int totalChunks = (int) Math.ceil(fileSize * 1.0 / CHUNK_SIZE);
        CountDownLatch latch = new CountDownLatch(totalChunks);
        Map<Integer, Future<Boolean>> futures = new HashMap<>();

        for (int i = 0; i < totalChunks; i++) {
            final int chunkIndex = i;
            long start = i * CHUNK_SIZE;
            long end = Math.min((i + 1) * CHUNK_SIZE, fileSize);
            Callable<Boolean> task = () -> {
                try {
                    Log.d("TAG", "sendDetectVideo: " + token);
                    return uploadChunk(file, token, start, end, chunkIndex, totalChunks, fileMD5String, callBack);
                } finally {
                    latch.countDown();
                }
            };
            Future<Boolean> future = executorService.submit(task);
            futures.put(chunkIndex, future);
        }

        try {
            latch.await();
            boolean allSuccess = true;
            for (Future<Boolean> future : futures.values()) {
                if (!future.get()) {
                    allSuccess = false;
                    break;
                }
            }
            if (allSuccess) {
                callBack.onSuccess("所有分块上传成功");
            } else {
                callBack.onFailed("部分分块上传失败");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            callBack.onFailed("上传过程中出现异常: " + e.getMessage());
        } finally {
            executorService.shutdown();
        }
    }

    private boolean uploadChunk(File file, String token, long start, long end, int chunkIndex,int totalChunks, String MD5, LoadTasksCallBack callBack) {
        RequestParams mToken = new RequestParams();
        mToken.put("Authorization", "Bearer " + token);

        MultipartBody.Builder requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        Log.d(TAG, "uploadChunk: " + chunkIndex + " " + totalChunks + " " + MD5);
        MultipartBody multipartBody = requestBody.addFormDataPart("md5", MD5)
                .addFormDataPart("chunkIndex", String.valueOf(chunkIndex))
                .addFormDataPart("totalChunks", String.valueOf(totalChunks))
                .addFormDataPart("file", "file"
                        , createChunkRequestBody(file, chunkIndex, start, end))
                .build();

        Request request = createRequest(URL.SEND_VIDEO_FILE_URL, multipartBody, mToken, start);
        return executeRequest(request, callBack);
    }

    private Request createRequest(String url, MultipartBody multipartBody, RequestParams mToken, long start) {
        Headers.Builder mHeadersBuilder = new Headers.Builder();
        for (Map.Entry<String, String> entry : mToken.urlParams.entrySet()) {
            mHeadersBuilder.add(entry.getKey(), entry.getValue());
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .headers(mHeadersBuilder.build())
                .post(multipartBody);

        return requestBuilder.build();
    }

    private boolean executeRequest(Request request, LoadTasksCallBack callBack) {
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String string = response.body().string();
                System.out.println("FileonResponse: " + string);
                callBack.onSuccess(string);
                return true;
            } else if (response.code() == 416) {
                // 416表示请求的Range无效，可能需要重新上传该分块
                System.out.println("onResponse: 分块上传失败，重新上传该分块");
                callBack.onFailed("分块上传失败，重新上传该分块");
                return false;
            } else {
                System.out.println("onResponse: 上传失败，状态码: " + response.code());
                callBack.onFailed("上传失败，状态码: " + response.code());
                return false;
            }
        } catch (IOException e) {
            System.out.println("onFailure: " + "上传失败");
            e.printStackTrace();
            callBack.onFailed("上传失败: " + e.getMessage());
            return false;
        }
    }


    private RequestBody createChunkRequestBody(File videoFile,int chunkIndex, long start, long end) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("video/mp4");
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                try (RandomAccessFile file = new RandomAccessFile(videoFile, "r");
                    FileChannel channel = file.getChannel()) {
                    ByteBuffer buffer = ByteBuffer.allocate(8192);
                    long position = start;
                    long remaining = end - start;

                    while (remaining > 0) {
                        int readSize = (int) Math.min(buffer.capacity(), remaining);
                        buffer.limit(readSize);
                        int bytesRead = channel.read(buffer, position);
                        if (bytesRead == -1) break;

                        sink.write(buffer.array(), 0, bytesRead);
                        position += bytesRead;
                        remaining -= bytesRead;
                        buffer.clear();
                    }
                }
            }
        };
    }
}
