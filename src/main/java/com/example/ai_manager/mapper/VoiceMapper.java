package com.example.ai_manager.mapper;

import com.example.ai_manager.dao.Images;
import com.example.ai_manager.dao.Voices;
import org.apache.ibatis.annotations.*;

import java.util.List;
@Mapper
public interface VoiceMapper {
    // 查询所有记录
    @Select("SELECT * FROM voices")
    List<Voices> findAll();

    // 根据 phone 查询单条记录
    @Select("SELECT * FROM voices WHERE url = #{url}")
    Voices findByUrl(@Param("url") String url);

    @Select("SELECT * FROM voices WHERE prompt = #{prompt}")
    List<Voices> findByPrompt(@Param("prompt") String prompt);

    // 插入一条记录
    @Insert("INSERT INTO voices (prompt, url, time) VALUES (#{prompt}, #{url}, #{time})")
    @Options(useGeneratedKeys = true, keyProperty = "prompt") // 设置主键生成策略
    int insert(Voices voice);

    // 更新一条记录
    @Update("UPDATE voices SET url = #{url}, time = #{time} WHERE phone = #{phone}")
    int update(Voices voice);

    // 删除一条记录
    @Delete("DELETE FROM voices WHERE prompt = #{prompt}")
    int deleteByPrompt(@Param("prompt") String prompt);
    // 删除一条记录
    @Delete("DELETE FROM voices WHERE url = #{url}")
    int deleteByUrl(@Param("url") String url);
}
