package com.example.ai_manager.service.impl;

import com.example.ai_manager.dao.Root;
import com.example.ai_manager.dao.User;
import com.example.ai_manager.mapper.LoginMapper;
import com.example.ai_manager.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {
    @Autowired
    private LoginMapper loginMapper;
    @Override
    public String login(String phone, String password) {
        if(loginMapper.selectUser(phone) == null){
            return "";
        }
        else {
            User user=new User();
            user=loginMapper.selectUser(phone);
            if(user.getPassword().equals(password)){
                return "登录成功";
            }
            else {
                return "";
            }
        }

    }

    @Override
    public String register(String phone, String password) {
        if (loginMapper.selectUser(phone) == null){
            loginMapper.insertUser(phone,password);
            return "注册成功";
        }
        return "用户已存在";
    }

    @Override
    public String rootlogin(String phone, String password) {
        if(loginMapper.selectRootUser(phone) == null){
            return "";
        }
        else {
            Root user=new Root();
            user=loginMapper.selectRootUser(phone);
            if(user.getPassword().equals(password)){
                return "登录成功";
            }
            else {
                return "";
            }
        }
    }

    @Override
    public List<User> FindUser() {
        return loginMapper.selectAll();
    }

    @Override
    public Integer deleteUser(String phone) {
        return loginMapper.deleteUser(phone);
    }

    @Override
    public Integer updateUser(String phone, String password) {
        return loginMapper.updateUser(phone,password);
    }

    @Override
    public Integer insertUser(String phone, String password) {
        return loginMapper.insertUser(phone,password);
    }

    @Override
    public User selectUser(String phone) {
        return loginMapper.selectUser(phone);
    }


}
