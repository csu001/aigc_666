package com.example.ai_manager.utils;


import com.example.ai_manager.dao.Sign;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import okhttp3.Headers;
import okhttp3.Headers.Builder;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAIv4GenSong {
    @Value("${volcengine.ak}")
    private String volcengineAk;

    @Value("${volcengine.sk}")
    private String volcengineSk;

    public String createSong(String prompt,Integer time) throws IOException{
        String ak =volcengineAk;
        String sk = volcengineSk;
        String Prompt = "写一首关于烟花的歌";
        String Gender = "Male";
        String Genre = "Pop";
        String Mood = "Happy";
        String audioUrl="";
        String action = "GenBGMForTime";
        String version = "2024-08-12";
        String region = "cn-beijing";
        String service = "imagination";

        String host = "open.volcengineapi.com";
        String path = "/";
        String contentType = "application/json";

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(System.out::println);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .build();

        Gson gson = new Gson();
        JsonObject body = new JsonObject();
        body.addProperty("Text", prompt); // 必填参数
        body.add("Genre", gson.toJsonTree(Arrays.asList("pop"))); // 数组格式
        body.add("Mood", gson.toJsonTree(Arrays.asList("happy")));
        body.addProperty("Duration", time); // 限制1-60秒
        body.addProperty("CallbackURL", "https://your-callback.com"); // 可选回调
        String jsonBody = body.toString();

        Map<String, String> query = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", host);
        headers.put("X-Date", Sign.getXDate(new Date()));
        String authorization = null;
        try {
            headers.put("X-Content-Sha256", Sign.hashSHA256(jsonBody.getBytes()));
            headers.put("Content-Type", contentType);
            authorization = Sign.getAuthorization("POST", path, headers, query, action, version, ak, sk, region,
                    service);
            headers.put("Authorization", authorization);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse(contentType),
                jsonBody.getBytes());
        Request request = new Request.Builder()
                .url(getUrl(host, path, query, action, version))
                .headers(asOkhttpHeaders(headers))
                .post(requestBody)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException(response.message());
            }
            assert response.body() != null;
            String responseBody = response.body().string();
            System.out.println("===>responseBody:" + responseBody);
            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            int code = jsonObject.get("Code").getAsInt();
            if (code != 0) {
                throw new RuntimeException(responseBody);
            }
            JsonObject result = jsonObject.getAsJsonObject("Result");

            System.out.println(result);
            // 预计等待歌曲生成时间
//            int predictedWaitTime = result.get("PredictedWaitTime").getAsInt() + 5;
//            System.out.println("预计等待歌曲生成时间：" + predictedWaitTime + "秒");
//            Thread.sleep(predictedWaitTime * 1000L);
            // 查询歌曲生成信息
            String taskId = result.get("TaskID").getAsString();
            body = new JsonObject();
            body.addProperty("TaskID", taskId);
            jsonBody = body.toString();
            action = "QuerySong";
            headers.put("X-Content-Sha256", Sign.hashSHA256(jsonBody.getBytes()));
            authorization = Sign.getAuthorization("POST", path, headers, query, action, version, ak, sk, region,
                    service);
            headers.put("Authorization", authorization);
            JsonElement songDetail;
            do {
                requestBody = RequestBody.create(MediaType.parse(contentType),
                        jsonBody.getBytes());
                request = new Request.Builder()
                        .url(getUrl(host, path, query, action, version))
                        .headers(asOkhttpHeaders(headers))
                        .post(requestBody)
                        .build();
                response = okHttpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new RuntimeException(response.message());
                }
                assert response.body() != null;
                responseBody = response.body().string();
                jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                code = jsonObject.get("Code").getAsInt();
                if (code != 0) {
                    throw new RuntimeException(responseBody);
                }
                result = jsonObject.getAsJsonObject("Result");
                JsonElement failureReason = result.get("FailureReason");
                if (!"null".equals(failureReason.toString())) {
                    throw new RuntimeException(failureReason.toString());
                }
                int progress = result.get("Progress").getAsInt();
                System.out.println("===>Progress：" + progress);
                songDetail = result.get("SongDetail");

                // 安全检查：确保 songDetail 不是 null 且不是 JsonNull
                if (songDetail != null && !songDetail.isJsonNull()) {
                    // 尝试转换为 JsonObject
                    if (songDetail.isJsonObject()) {
                        JsonObject songDetailJson = songDetail.getAsJsonObject();

                        // 提取 AudioUrl（带空值检查）
                        if (songDetailJson.has("AudioUrl") && !songDetailJson.get("AudioUrl").isJsonNull()) {
                            audioUrl = songDetailJson.get("AudioUrl").getAsString();
                            System.out.println("===>提取到AudioUrl: " + audioUrl);
                            break; // 找到后退出循环
                        }
                    }
                }


                if (!"null".equals(songDetail.toString())) {
                    break;
                }
                Thread.sleep(5000L);
            } while (true);
            System.out.println("===>查询歌曲结束：" + audioUrl);
            return audioUrl;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public static String getUrl(String host, String path, Map<String, String> query, String action, String version) {
        return "https://" + host + path + "?" + Sign.getQuery(query, action, version);
    }

    public static Headers asOkhttpHeaders(Map<String, String> headers) {
        Headers.Builder builder = new Builder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }
}