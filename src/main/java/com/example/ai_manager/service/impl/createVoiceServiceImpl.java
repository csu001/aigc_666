package com.example.ai_manager.service.impl;

import com.example.ai_manager.dao.Images;
import com.example.ai_manager.dao.Voices;
import com.example.ai_manager.mapper.VoiceMapper;
import com.example.ai_manager.service.createVoiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class createVoiceServiceImpl implements createVoiceService {

    @Autowired private VoiceMapper  voiceMapper;

    @Override
    public List<Voices> findAll() {
        return voiceMapper.findAll();
    }

    @Override
    public Voices findByUrl(String url) {
       return voiceMapper.findByUrl(url);
    }

    @Override
    public List<Voices> findByPrompt(String prompt) {
        return voiceMapper.findByPrompt(prompt);
    }


    @Override
    public int insert(Voices Voice) {
        return voiceMapper.insert(Voice);
    }

    @Override
    public int update(Voices Voice) {
       return voiceMapper.update(Voice);
    }

    @Override
    public int deleteByPrompt(String prompt) {
        return voiceMapper.deleteByPrompt(prompt);
    }



    @Override
    public int deleteByUrl(String url) {
        return voiceMapper.deleteByUrl(url);
    }
}
