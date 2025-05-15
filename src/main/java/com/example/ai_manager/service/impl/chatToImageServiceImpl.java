package com.example.ai_manager.service.impl;

import com.example.ai_manager.dao.Images;
import com.example.ai_manager.mapper.ChatToImageMapper;
import com.example.ai_manager.service.chatToImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class chatToImageServiceImpl implements chatToImageService {

    @Autowired
    private ChatToImageMapper chatToImageMapper;

    @Override
    public List<Images> findAll() {
        return chatToImageMapper.findAll();
    }

    @Override
    public List<Images> findByPhone(String phone) {
        return chatToImageMapper.findByPhone(phone);
    }

    @Override
    public int insert(Images images) {
        return chatToImageMapper.insert(images);
    }

    @Override
    public List<Images> findByPhoneAndStatus(String phone, int status) {
        return chatToImageMapper.findByPhoneAndStatus(phone,status);
    }

    @Override
    public int update(Images images) {
        return chatToImageMapper.update(images);
    }

    @Override
    public int deleteByPhone(String phone) {
        return chatToImageMapper.deleteByPhone(phone);
    }

    @Override
    public int deleteByUrl(String url) {
        return chatToImageMapper.deleteByUrl(url);
    }
}