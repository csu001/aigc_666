package com.example.ai_manager.dao;

import lombok.Data;

@Data
public class Voices {
    private String prompt;
    private String url;
    private String time;

    public void Voices(String prompt, String url, String time){
        this.prompt = prompt;
        this.url = url;
        this.time = time;
    }
}
