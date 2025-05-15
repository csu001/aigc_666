package com.example.ai_manager.mapper;

import com.example.ai_manager.dao.Images;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ChatToImageMapper {

    // 查询所有记录
    @Select("SELECT * FROM images")
    List<Images> findAll();

    // 根据 phone 查询单条记录
    @Select("SELECT * FROM images WHERE phone = #{phone}")
    List<Images> findByPhone(@Param("phone") String phone);

    // 插入一条记录
    @Insert("INSERT INTO images (phone, url, time,status) VALUES (#{phone}, #{url}, #{time},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "phone") // 设置主键生成策略
    int insert(Images images);

    // 根据 phone 和 status 查询记录
    @Select("SELECT * FROM images WHERE phone = #{phone} AND status = #{status}")
    List<Images> findByPhoneAndStatus(@Param("phone") String phone, @Param("status") int status);

    // 更新一条记录
    @Update("UPDATE images SET url = #{url}, time = #{time} WHERE phone = #{phone}")
    int update(Images images);

    // 删除一条记录
    @Delete("DELETE FROM images WHERE phone = #{phone}")
    int deleteByPhone(@Param("phone") String phone);
    // 删除一条记录
    @Delete("DELETE FROM images WHERE url = #{url}")
    int deleteByUrl(@Param("url") String url);
}


