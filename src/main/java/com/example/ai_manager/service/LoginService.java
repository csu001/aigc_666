package com.example.ai_manager.service;


import com.example.ai_manager.dao.User;

import java.util.List;

public interface LoginService {

    public String login(String phone, String password);

    public String register(String phone, String password);

    public String rootlogin(String phone, String password);

    public List<User> FindUser();

    public Integer  deleteUser(String phone);

    public Integer  updateUser(String phone, String password);

    public Integer  insertUser(String phone, String password);

    public User  selectUser(String phone);

}
