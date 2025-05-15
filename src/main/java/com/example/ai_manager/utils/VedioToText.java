package com.example.ai_manager.utils;

import com.alibaba.dashscope.audio.asr.transcription.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class VedioToText {

    @Value("${dashscope.api-key}")
    private String dashScopeApiKey;

    private static final Pattern TAG_PATTERN = Pattern.compile("<\\|.*?\\|>");
    private static final Pattern CHINESE_PATTERN =
            Pattern.compile("[^\u4E00-\u9FA5，。、；：！？《》「」『』【】（）—]");
    public String toTxet(String url) throws ProtocolException {
        TranscriptionParam param =
                TranscriptionParam.builder()
                        // 若未配置环境变量，取消下一行的注释并将your-api-key替换成您自己的API Key
                        .apiKey(dashScopeApiKey)
                        .model("sensevoice-v1")
                        .fileUrls(Collections.singletonList("https://aigc-sw.oss-cn-beijing.aliyuncs.com/%E9%BE%99%E5%A9%890.mp3"))
                        .parameter("language_hints", new String[]{"zh-CN"})
                        .build();
        try {

            Transcription transcription = new Transcription();
            // 提交任务
            TranscriptionResult result = transcription.asyncCall(param);
            // 等待任务完成
            result =
                    transcription.wait(
                            TranscriptionQueryParam.FromTranscriptionParam(param, result.getTaskId()));
            // 获取语音识别结果
            List<TranscriptionTaskResult> taskResultList = result.getResults();
            if (taskResultList != null && taskResultList.size() > 0) {
                TranscriptionTaskResult taskResult = taskResultList.get(0);
                // 获取识别结果的url
                String transcriptionUrl = taskResult.getTranscriptionUrl();
//                System.out.println("transcriptionUrl: " + transcriptionUrl);
                // 获取url内对应的结果
                HttpURLConnection connection =
                        (HttpURLConnection) new URL(transcriptionUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

                // 关键步骤：将流内容完整读取到字符串
                StringBuilder jsonContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                // 解析JSON（只解析一次）
                JsonObject jsonObject = gson.fromJson(jsonContent.toString(), JsonObject.class);


                // 安全检查层级结构
                JsonArray transcripts = jsonObject.getAsJsonArray("transcripts");
                if (transcripts == null || transcripts.isEmpty()) {
                    throw new IllegalStateException("transcripts数组不存在或为空");
                }

                JsonObject firstTranscript = transcripts.get(0).getAsJsonObject();
                if (!firstTranscript.has("text")) {
                    throw new IllegalStateException("第一个transcript缺少text字段");
                }

                // 提取并处理文本
                String rawText = firstTranscript.get("text").getAsString();
                String chineseText = processChineseText(rawText);

                System.out.println("最终结果: " + chineseText);
                return chineseText;


                // 格式化输出json结果
//                    Gson gson = new GsonBuilder().setPrettyPrinting().create();

//                    System.out.println(gson.toJson(gson.fromJson(reader, JsonObject.class)));


//                    JsonObject jsonObject = new Gson().fromJson(
//                            new JsonReader(new StringReader(gson.toJson(gson.fromJson(reader, JsonObject.class)))),
//                            JsonObject.class
//                    );

//            System.exit(0);

            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    private static String processChineseText(String rawText) {
        // 分步处理更清晰
        String step1 = TAG_PATTERN.matcher(rawText).replaceAll("");
        String step2 = CHINESE_PATTERN.matcher(step1).replaceAll("");
        return step2.trim();
    }

}
