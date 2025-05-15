//package com.example.ai_manager.utils;
//
//
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//public class SubtitleSplitter {
//    // 支持中英文的智能分割
//    private static final Pattern SPLIT_PATTERN = Pattern.compile(
//            "([。！？；]|(?<!\\d)\\.(?!\\d))|" + // 中文标点
//                    "(?<=[,.;!?])\\s+"                // 英文标点
//    );
//
//    public static List<SubtitleSegment> splitSubtitles(
//            String text,
//            int totalDurationSec,
//            SubtitleConfig config
//    ) {
//        // 标点分割
//        String[] sentences = SPLIT_PATTERN.split(text);
//
//        // 过滤空内容
//        List<String> validSentences = Arrays.stream(sentences)
//                .filter(s -> !s.trim().isEmpty())
//                .collect(Collectors.toList());
//
//        // 计算分段时长
//        return calculateSegments(validSentences, totalDurationSec, config);
//    }
//
//    private static List<SubtitleSegment> calculateSegments(
//            List<String> sentences,
//            int totalDuration,
//            SubtitleConfig config
//    ) {
//        List<SubtitleSegment> result = new ArrayList<>();
//        double timePerChar = (totalDuration * 1000.0) / countAllChars(sentences);
//
//        double currentStart = 0;
//        for (String sentence : sentences) {
//            int charCount = sentence.length();
//            double duration = Math.max(
//                    charCount * timePerChar,
//                    config.getMinSegmentDuration() * 1000
//            );
//
//            // 控制单行长度
//            if (charCount > config.getMaxCharsPerLine()) {
//                List<String> lines = splitLongLine(sentence, config.getMaxCharsPerLine());
//                double lineDuration = duration / lines.size();
//                for (String line : lines) {
//                    result.add(new SubtitleSegment(
//                            currentStart,
//                            currentStart + lineDuration,
//                            line
//                    ));
//                    currentStart += lineDuration;
//                }
//            } else {
//                result.add(new SubtitleSegment(
//                        currentStart,
//                        currentStart + duration,
//                        sentence
//                ));
//                currentStart += duration;
//            }
//        }
//        return result;
//    }
//
//    // 辅助方法
//    private static int countAllChars(List<String> list) {
//        return list.stream().mapToInt(String::length).sum();
//    }
//
//    public static List<String> splitLongLine(String text, int maxLength) {
//        // 参数校验
//        if (maxLength <= 0) {
//            throw new IllegalArgumentException("maxLength 必须为正整数");
//        }
//        if (text == null) {
//            return Collections.emptyList(); // 返回空列表而非null
//        }
//
//        List<String> parts = new ArrayList<>();
//        int length = text.length();
//
//        // 传统循环替代流，提升大文本处理性能
//        for (int i = 0; i < length; i += maxLength) {
//            int end = Math.min(i + maxLength, length);
//            parts.add(text.substring(i, end));
//        }
//
//        return parts;
//    }
//}
