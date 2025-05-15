package com.example.ai_manager.service;

import com.example.ai_manager.dao.Images;
import com.example.ai_manager.dao.Voices;

import java.util.List;

public interface createVoiceService {
    // 查询所有记录
    List<Voices> findAll();

    // 根据 phone 查询单条记录
    Voices findByUrl(String url);

    List<Voices> findByPrompt(String prompt);

    // 插入一条记录
    int insert(Voices Voice);
    // 根据 phone 和 status 查询记录


    // 更新一条记录
    int update(Voices Voice);

    // 删除一条记录
    int deleteByPrompt(String phone);

    // 删除一条记录
    int deleteByUrl(String url);
}
