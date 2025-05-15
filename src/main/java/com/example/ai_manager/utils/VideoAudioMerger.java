package com.example.ai_manager.utils;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class VideoAudioMerger {
    public String mergeAudioToVideo(String videoPath, String audioPath, String outputPath) {
        // FFmpeg 命令：将音频和视频合并，保持视频原时长，音频自动循环或截断
        String ffmpegPath = "D:\\ffmpeg-7.0.2-essentials_build\\bin\\ffmpeg.exe";
        String[] command = {
                ffmpegPath,
                "-i", videoPath,      // 输入视频文件
                "-i", audioPath,      // 输入音频文件
                "-c:v", "copy",       // 复制视频流（不重新编码）
                "-c:a", "aac",        // 编码音频为 AAC 格式
                "-shortest",          // 以最短的流（视频或音频）为输出时长
                "-y",                 // 覆盖输出文件
                outputPath
        };

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            // 读取错误流（FFmpeg 日志输出到 stderr）
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = errorReader.readLine()) != null) {
                System.out.println("[FFmpeg] " + line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("视频与音频合并成功！"+outputPath);
                return outputPath;
            } else {
                System.out.println("合并失败，错误码: " + exitCode);

            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }
}
