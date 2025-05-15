package com.example.ai_manager.dao;

import lombok.Data;

@Data
public class Images {
    private String phone;
    private String url;
    private String time;
    private int status;

    public Images(String phone, String imageUrl, String formattedTime,int status) {
        this.phone = phone;
        this.url = imageUrl;
        this.time = formattedTime;
        this.status = status;
    }
}
