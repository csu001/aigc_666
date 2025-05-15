package com.example.ai_manager.controller;

import com.example.ai_manager.dao.Images;
import com.example.ai_manager.dao.Voices;
import com.example.ai_manager.service.LoginService;
import com.example.ai_manager.service.chatToImageService;
import com.example.ai_manager.service.createVoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*") // 允许所有来源的跨域
public class managerController {

    @Autowired private chatToImageService chatToImageService;
    @Autowired private LoginService loginService;
    @Autowired private createVoiceService createVoiceService;

    @PostMapping("/managerLogin")
    public String login(@RequestParam String phone, @RequestParam String password) {
        return loginService.rootlogin(phone, password);
    }

    @GetMapping("/managerImage")
    public List<Images> managerImage(){
        return chatToImageService.findAll();
    }

    @PostMapping("/deleteImage")
    public String deleteImage(@RequestParam String url){
        int result = chatToImageService.deleteByUrl(url);
        if (result > 0) {
            return "删除成功";
        } else {
            return "删除失败";
        }
    }


    @GetMapping("/managerVoice")
    public List<Voices> managerVoice(){
        return createVoiceService.findAll();
    }

    @PostMapping("/deleteToVoice")
    public String deleteVoice(@RequestParam String url){
        int result = createVoiceService.deleteByUrl(url);
        if (result > 0) {
            return "删除成功";
        } else {
            return "删除失败";
        }
    }

    @PostMapping("/selectByphone")
    public  List<Images> selectByphone(@RequestParam String phone){
        List<Images> images = chatToImageService.findByPhone(phone);
        return images;
    }

    @PostMapping("/selectByPromt")
    public  List<Voices>  selectByPromt(@RequestParam String prompt){
        List<Voices> voices = createVoiceService.findByPrompt(prompt);
        return voices;
    }

}
