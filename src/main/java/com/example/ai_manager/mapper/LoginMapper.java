package com.example.ai_manager.mapper;

import com.example.ai_manager.dao.Root;
import com.example.ai_manager.dao.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LoginMapper {

    // 新增: 插入用户信息
    @Insert("INSERT INTO user (phone, password) VALUES (#{phone}, #{password})")
    public int insertUser(@Param("phone") String phone, @Param("password") String password);

    // 新增: 删除用户信息
    @Delete("DELETE FROM user WHERE phone = #{phone}")
    public int deleteUser(@Param("phone") String phone);

    // 新增: 更新用户信息
    @Update("UPDATE user SET password = #{password} WHERE phone = #{phone}")
    public int updateUser(@Param("phone") String phone, @Param("password") String password);

    // 新增: 查询用户信息
    @Select("SELECT * FROM user WHERE phone = #{phone}")
    public User selectUser(@Param("phone") String phone);

    @Select("SELECT * FROM user")
    public List<User> selectAll();

    // 新增: 查询用户信息
    @Select("SELECT * FROM root WHERE phone = #{phone}")
    public Root selectRootUser(@Param("phone") String phone);
}
