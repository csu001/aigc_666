package com.example.ai_manager.utils;

import org.springframework.stereotype.Component;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

@Component
public class downloadVideoUtils {
    public void downloadVideo(String videoUrl, String outputPath) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            URL url = new URL(videoUrl);
            inputStream = new BufferedInputStream(url.openStream());
            outputStream = new FileOutputStream(outputPath);

            byte[] dataBuffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(dataBuffer, 0, dataBuffer.length)) != -1) {
                outputStream.write(dataBuffer, 0, bytesRead);
            }

            System.out.println("视频下载完成: " + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void downloadAudio(String audioUrl, String outputPath) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            // 1. 创建URL对象并打开连接
            URL url = new URL(audioUrl);
            inputStream = new BufferedInputStream(url.openStream());

            // 2. 创建输出文件流（自动创建文件，但需确保目录存在）
            outputStream = new FileOutputStream(outputPath);

            // 3. 缓冲区配置（1024字节=1KB，可根据需要调整）
            byte[] dataBuffer = new byte[4096]; // 推荐4KB缓冲区平衡性能与内存
            int bytesRead;

            // 4. 数据传输
            while ((bytesRead = inputStream.read(dataBuffer, 0, dataBuffer.length)) != -1) {
                outputStream.write(dataBuffer, 0, bytesRead);
            }

            System.out.println("语音下载完成: " + outputPath);
        } catch (IOException e) {
            System.err.println("下载失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 5. 资源清理（Java 7+ 推荐使用 try-with-resources 自动关闭）
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                System.err.println("资源关闭失败: " + e.getMessage());
            }
        }
    }

    public static String generateRandom3Digits() {
        Random random = new Random();
        int num = random.nextInt(900) + 100; // 100 到 999 之间的随机数
        return String.format("%03d", num);    // 格式化为三位数（如 005）
    }

}
