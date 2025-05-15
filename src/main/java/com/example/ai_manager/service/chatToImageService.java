package com.example.ai_manager.service;

import com.example.ai_manager.dao.Images;
import java.util.List;


public interface chatToImageService {
    // 查询所有记录
    List<Images> findAll();

    // 根据 phone 查询单条记录
    List<Images> findByPhone(String phone);

    // 插入一条记录
    int insert(Images images);
    // 根据 phone 和 status 查询记录

    List<Images> findByPhoneAndStatus(String phone, int status);

    // 更新一条记录
    int update(Images images);

    // 删除一条记录
    int deleteByPhone(String phone);

    // 删除一条记录
    int deleteByUrl(String url);
}