package com.example.ai_manager.controller;

import com.example.ai_manager.dao.User;
import com.example.ai_manager.service.LoginService;
import com.example.ai_manager.service.impl.LoginServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*") // 允许所有来源的跨域
//@RequestMapping("/user")
public class LoginController {


    @Autowired
    private LoginService loginService;


    @GetMapping("/register")
    public String register(@RequestParam String phone, @RequestParam String password) {
        if (loginService.selectUser(phone)!= null){
            return null;
        }
       return loginService.register(phone, password);
    }

    @PostMapping("/login")
    public String login(@RequestParam String phone, @RequestParam String password) {
        return loginService.login(phone, password);
    }
    @PostMapping("ListUser")
    public List<User> ListUser() {
        return loginService.FindUser();
    }
    @PostMapping("deleteUser")
    public Integer deleteUser(@RequestParam String phone) {
        return loginService.deleteUser(phone);
    }
    @PostMapping("updateUser")
    public Integer updateUser(@RequestParam String phone, @RequestParam String password) {
        return loginService.updateUser(phone, password);
    }
    @PostMapping("insertUser")
    public Integer insertUser(@RequestParam String phone, @RequestParam String password) {
        if (loginService.selectUser(phone)!= null){
            return 0;
        }
        return loginService.insertUser(phone, password);
    }
    @PostMapping("selectUser")
    public User selectUser(@RequestParam String phone) {
        return loginService.selectUser(phone);
    }
}
