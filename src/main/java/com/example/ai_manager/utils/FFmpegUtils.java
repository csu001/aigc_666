//package com.example.ai_manager.utils;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//
//public class FFmpegUtils {
//    public static void burnSubtitles(
//            String inputVideo,
//            String srtPath,
//            String outputPath,
//            SubtitleStyle style
//    ) throws IOException, InterruptedException {
//
//        String styleParams = String.format(
//                "force_style='FontName=%s,FontSize=%d,PrimaryColour=&H%s'",
//                style.getFont(),
//                style.getFontSize(),
//                style.getTextColor()
//        );
//
//        ProcessBuilder pb = new ProcessBuilder(
//                "ffmpeg",
//                "-y",
//                "-i", inputVideo,
//                "-vf", "subtitles=" + srtPath + ":" + styleParams,
//                "-c:a", "copy",
//                "-c:v", "libx264",
//                "-crf", "23",
//                outputPath
//        );
//
//        Process process = pb.start();
//        // 错误流处理（必须消费）
//        consumeErrorStream(process);
//
//        int exitCode = process.waitFor();
//        if (exitCode != 0) {
//            throw new RuntimeException("FFmpeg处理失败，退出码：" + exitCode);
//        }
//    }
//
//    private static void consumeErrorStream(Process process) {
//        new Thread(() -> {
//            try (BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(process.getErrorStream()))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    System.err.println("[FFmpeg] " + line);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }
//}
