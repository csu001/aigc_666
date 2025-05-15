package com.example.ai_manager.controller;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.example.ai_manager.dao.Images;
import com.example.ai_manager.service.chatToImageService;
import com.example.ai_manager.utils.AliOSSUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@RestController
@CrossOrigin(origins = "*") // 允许所有来源的跨域
public class ImageToImageController {
    @Autowired
    private chatToImageService chatToImageService;

    @Autowired
    private AliOSSUtils aliOSSUtils;

    private String imageUrls="";
    @Value("${dashscope.api-key}")
    private String dashScopeApiKey;

    @PostMapping("/upload/image")
    public String upload(MultipartFile image) throws IOException {
        //调用阿里云OSS工具类，将上传上来的文件存入阿里云
        String url = aliOSSUtils.upload(image);
        //将图片上传完成后的url返回，用于浏览器回显展示
        imageUrls=url;//公共url
        return url;
    }

    @PostMapping("/imageToImage")//图生图
    public String imageToImage(@RequestParam String phone, @RequestParam String prompt) {

        //使用公网url链接
        String refImage = imageUrls;
        HashMap<String,Object> parameters = new HashMap<>();
        parameters.put("ref_strength", 0.5);
        parameters.put("ref_mode", "repaint");
        try {
            // 构建参数对象
            ImageSynthesisParam param =
                    ImageSynthesisParam.builder()
                            .apiKey(dashScopeApiKey)
                            .model(ImageSynthesis.Models.WANX_V1)
                            .prompt(prompt)
                            .style("<auto>")
                            .n(1)
                            .size("1024*1024")
                            .refImage(refImage)
                            .parameters(parameters)
                            .build();

            ImageSynthesis imageSynthesis = new ImageSynthesis();


            // 调用图像合成服务
            ImageSynthesisResult result = imageSynthesis.call(param);

            // 将结果转换为 JSON 字符串并返回
            ObjectMapper objectMapper = new ObjectMapper(); // 使用 Jackson 将对象转换为 JSON

            JsonNode rootNode = objectMapper.readTree(objectMapper.writeValueAsString(result)); // 将 result 转为 JSON 字符串再解析

            // 提取 URL
            String imageUrl = rootNode.path("output").path("results").get(0).path("url").asText();
            System.out.println(imageUrl);

            //获取当前时间
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 如果需要格式化时间，可以使用 DateTimeFormatter
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = now.format(formatter);

            chatToImageService.insert( new Images(phone, imageUrl,formattedTime,1));

            return objectMapper.writeValueAsString(result);//返回整个json响应

        } catch (ApiException | NoApiKeyException e) {
            // 捕获异常并返回错误信息
            return "{\"error\": \"" + e.getMessage() + "\"}";
        } catch (Exception e) {
            // 捕获其他异常
            return "{\"error\": \"Unexpected error occurred: " + e.getMessage() + "\"}";
        }
    }
    @GetMapping("/listImage2")
    public Images[] listImage(@RequestParam String phone)
    {
        return chatToImageService.findByPhoneAndStatus(phone,1).toArray(new Images[0]);
    }
}
