package com.example.ai_manager.dao;

import com.alibaba.dashscope.audio.ttsv2.enrollment.Voice;
import com.google.gson.Gson;
import lombok.Data;

import java.util.Map;
@Data
public class VoiceDTO {
    private String voiceId;
    // 使用 Map 或 String 替代 JsonObject
    private Map<String, Object> data;

    // 转换方法：将 Voice 转为 VoiceDTO
    public static VoiceDTO from(Voice voice) {
        VoiceDTO dto = new VoiceDTO();
        dto.setVoiceId(voice.getVoiceId());
        dto.setData(convertGsonToMap(voice.getData()));
        return dto;
    }

    private static Map<String, Object> convertGsonToMap(com.google.gson.JsonObject json) {
        return new Gson().fromJson(json, Map.class);
    }

    // Getter/Setter
}