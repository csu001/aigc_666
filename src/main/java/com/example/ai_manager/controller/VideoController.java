package com.example.ai_manager.controller;

import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesis;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisParam;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.example.ai_manager.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*") // 允许所有来源的跨域
public class VideoController {
    @Autowired
    private VedioToText vedioToText;
    @Autowired
    private OpenAIv4GenSong  openAIv4GenSong;
    @Autowired
    private VideoAudioMerger videoAudioMerger;
    @Autowired
    private Caculate  caculate;
    @Autowired
    private AliOSSUtils aliOSSUtils;
    @Value("${dashscope.api-key}")
    private String dashScopeApiKey;
    @Autowired
    private downloadVideoUtils downloadVideoUtils;
    @PostMapping("/creatVideo")
    public String creatVideo(@RequestParam String prompt) throws NoApiKeyException, InputRequiredException, IOException {
        VideoSynthesis vs = new VideoSynthesis();
        VideoSynthesisParam param =
                VideoSynthesisParam.builder()
                        .model("wanx2.1-t2v-turbo")
                        .prompt(prompt)
                        .size("1280*720")
                        .apiKey("sk-e150afa942024f159288d3fcadf9aa52")
                        .build();
        System.out.println("please wait...");
          //视频生成
        VideoSynthesisResult result = vs.call(param);

        String videoUrl = result.getOutput().getVideoUrl();

        Integer video_duration= (result.getUsage().getVideoDuration());

//        System.out.println(result);
//
//        System.out.println(JsonUtils.toJson(result));

        //下载保存视频
        String outputDir = "D:\\outputvoice";
//
//        // 生成随机文件名（如 video583.mp4）
        String randomFileName = "video" + downloadVideoUtils.generateRandom3Digits() + ".mp4";
        String outputPath = outputDir + File.separator + randomFileName;
        downloadVideoUtils.downloadVideo(videoUrl,outputPath);


        // 3. 生成并合并字幕（新增功能）
        String subtitledVideoPath = processSubtitles(vedioToText.toTxet(null), video_duration,outputPath , outputDir);

//        String url= aliOSSUtils.uploadFileToOSS(outputPath);

//        caculate.calculateVector(url);

      String SongUrl=  openAIv4GenSong.createSong(prompt,video_duration);
//
        String randomFileName1 = "voice" + downloadVideoUtils.generateRandom3Digits() + ".wav";
        String outputPath1 = outputDir + File.separator + randomFileName1;
        downloadVideoUtils.downloadAudio(SongUrl,outputPath1);


        // 生成唯一输出文件名（避免任何冲突）
        String outputFileName = "merged_" + UUID.randomUUID().toString() + ".mp4";
        String outputPath2= outputDir + File.separator + outputFileName;

        String path = videoAudioMerger.mergeAudioToVideo(subtitledVideoPath,outputPath1,outputPath2);

        String url = aliOSSUtils.uploadFileToOSS(path);
//
//        System.out.println(url);
        System.out.println(path);
//        return path;
        return url;
//        return outputPath;
//        return "";
    }


///--------------------- 新增字幕处理核心方法 ---------------------

    /**
     * 字幕处理流程
     */
    private String processSubtitles(String prompt, int duration,
                                    String videoPath, String outputDir) {
        try {
            // 生成字幕文件
            File srtFile = generateSubtitleFile(prompt, duration, outputDir);

            // 合并字幕到视频
            String outputPath = outputDir + File.separator +
                    "subtitled_" + UUID.randomUUID() + ".mp4";
            burnSubtitles(videoPath, outputPath, srtFile);
            System.out.println(outputPath);
            // 清理临时文件
            srtFile.delete();
            new File(videoPath).delete();

            return outputPath;
        } catch (Exception e) {
            throw new RuntimeException("字幕处理失败", e);
        }
    }

    /**
     * 生成SRT字幕文件
     */
    private File generateSubtitleFile(String text, int totalSeconds, String outputDir)
            throws IOException {
        // 智能分句
        List<String> sentences = splitSentences(text);

        // 计算时间分布
        List<SubtitleItem> subtitles = calculateTiming(sentences, totalSeconds);

        // 写入SRT文件
        File srtFile = new File(outputDir, "subs_" + System.currentTimeMillis() + ".srt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(srtFile))) {
            for (int i = 0; i < subtitles.size(); i++) {
                SubtitleItem item = subtitles.get(i);
                writer.write(String.valueOf(i + 1));
                writer.newLine();
                writer.write(formatTime(item.start) + " --> " + formatTime(item.end));
                writer.newLine();
                writer.write(item.text);
                writer.newLine();
                writer.newLine();
            }
        }
        return srtFile;
    }

    /**
     * 智能分句算法（支持中英文标点）
     */
    private List<String> splitSentences(String text) {
        List<String> sentences = new ArrayList<>();
        // 匹配中英文分句符号：。！？.!?
        Pattern pattern = Pattern.compile("[^。！？.!?]+[。！？.!?]?");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String sentence = matcher.group().trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
        }
        return sentences;
    }

    /**
     * 动态时间分配算法
     */
    private List<SubtitleItem> calculateTiming(List<String> sentences, int totalSeconds) {
        List<SubtitleItem> items = new ArrayList<>();
        double totalChars = sentences.stream().mapToInt(String::length).sum();
        double timePerChar = totalSeconds / totalChars;

        final int MIN_DURATION = 2;  // 单句最少显示2秒
        final int MAX_DURATION = 5;  // 单句最多显示5秒

        double current = 0;
        for (String sentence : sentences) {
            int chars = sentence.length();
            double duration = chars * timePerChar;

            // 限制单句时长范围
            duration = Math.max(MIN_DURATION, Math.min(duration, MAX_DURATION));

            // 处理总时长超限
            if (current + duration > totalSeconds) {
                duration = totalSeconds - current;
            }

            items.add(new SubtitleItem(current, current + duration, sentence));
            current += duration;

            if (current >= totalSeconds) break;
        }
        return items;
    }

    /**
     * 时间格式转换（秒转HH:mm:ss,SSS）
     */
    private String formatTime(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);
        int millis = (int) ((seconds - (int)seconds) * 1000);

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, secs, millis);
    }

    /**
     * 使用FFmpeg合并字幕
     */
    private void burnSubtitles(String inputPath, String outputPath, File srtFile) {
        System.out.println(inputPath);
        System.out.println(outputPath);
        System.out.println(srtFile);
        try {
            // ---------------------- 1. 路径规范化处理 ----------------------
            // 确保输出目录存在
            Path outputDir = Paths.get(outputPath).getParent();
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            // ---------------------- 2. 字幕文件验证 ----------------------
            if (!srtFile.exists() || srtFile.length() == 0) {
                throw new RuntimeException("字幕文件不存在或为空: " + srtFile.getAbsolutePath());
            }

            // 路径规范化
            String sanitizedSubPath = srtFile.getAbsolutePath()
                    .replace("\\", "\\\\\\\\")  // 转义为四个反斜杠
                    .replace(":", "\\\\:");     // 转义盘符冒号

            // 2. 构建FFmpeg命令（保持Windows原生路径格式）
            List<String> command = new ArrayList<>();
            command.add("D:\\ffmpeg-7.0.2-essentials_build\\bin\\ffmpeg.exe");
            command.add("-y");
            command.add("-i");
            command.add(inputPath);
            command.add("-vf");
            command.add("subtitles=\"" + sanitizedSubPath + "\":force_style='FontName=Microsoft YaHei,FontSize=24,PrimaryColour=&HFFFFFF,BackColour=&H80000000,Outline=1,Shadow=1,MarginV=30'");
            command.add("-c:a");
            command.add("copy");
            command.add(outputPath);

            // 3. 打印调试命令（显示原生路径）
            System.out.println("[DEBUG] 原生路径命令：\n" + String.join(" ", command));



            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 实时打印日志
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("FFmpeg Log: " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg 退出码: " + exitCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("字幕合并失败: " + e.getMessage(), e);
        }
    }

    private String getFFmpegError(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    //--------------------- 辅助数据结构 ---------------------
    private static class SubtitleItem {
        double start;
        double end;
        String text;

        public SubtitleItem(double start, double end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }
    }
}
