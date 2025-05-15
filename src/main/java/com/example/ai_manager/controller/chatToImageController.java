package com.example.ai_manager.controller;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.example.ai_manager.dao.Images;
import com.example.ai_manager.service.chatToImageService;
import com.example.ai_manager.utils.Caculate;
import com.example.ai_manager.utils.OpenAIv4GenSong;
import com.example.ai_manager.utils.VedioToText;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@CrossOrigin(origins = "*") // 允许所有来源的跨域
public class  chatToImageController {
    @Autowired
    private VedioToText vedioToText;
    @Autowired
    private OpenAIv4GenSong  openAIv4GenSong;
    @Autowired
    private Caculate caculate;
    @Autowired
    private chatToImageService chatToImageService;
    @Value("${dashscope.api-key}")
    private String dashScopeApiKey;
    private final ImageSynthesis imageSynthesis = new ImageSynthesis();
    @PostMapping("/chatToImage")//文生图
    public String chatToImage(@RequestParam String phone,@RequestParam String prompt) {

        try {
            // 构建参数对象
            ImageSynthesisParam param = ImageSynthesisParam.builder()
                    .apiKey(dashScopeApiKey)
                    .model(ImageSynthesis.Models.WANX_V1)
                    .prompt(prompt)
                    .style("<watercolor>")
                    .n(1)
                    .size("1024*1024")
                    .build();

            // 调用图像合成服务
            ImageSynthesisResult result3 = imageSynthesis.call(param);
            ImageSynthesisResult result4 = imageSynthesis.call(param);
            ImageSynthesisResult result5 = imageSynthesis.call(param);

            // 将结果转换为 JSON 字符串并返回
            ObjectMapper objectMapper = new ObjectMapper(); // 使用 Jackson 将对象转换为 JSON

            JsonNode rootNode = objectMapper.readTree(objectMapper.writeValueAsString(result3)); // 将 result 转为 JSON 字符串再解析
            JsonNode rootNode1 = objectMapper.readTree(objectMapper.writeValueAsString(result4)); // 将 result 转为 JSON 字符串再解析
            JsonNode rootNode2 = objectMapper.readTree(objectMapper.writeValueAsString(result5)); // 将 result 转为 JSON 字符串再解析

            String best="";
            String best1="";
            // 提取 URL
            String imageUrl = rootNode.path("output").path("results").get(0).path("url").asText();
            String imageUrl1 = rootNode1.path("output").path("results").get(0).path("url").asText();
            String imageUrl2 = rootNode2.path("output").path("results").get(0).path("url").asText();
            System.out.println(imageUrl);
            System.out.println(imageUrl1);
            System.out.println(imageUrl2);

            MultiModalConversation conv = new MultiModalConversation();
            MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("image", imageUrl),
                            Collections.singletonMap("image", imageUrl1),
                            Collections.singletonMap("image", imageUrl2),
                            Collections.singletonMap("text", "请返回质量最高的图片序号（例如:1）,不需要其他描述"))).build();
            MultiModalConversationParam param1 = MultiModalConversationParam.builder()
                    // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                    .apiKey("sk-e150afa942024f159288d3fcadf9aa52")
                    // 此处以qwen-vl-plus为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
                    .model("qwen-vl-plus")
                    .message(userMessage)
                    .build();
            MultiModalConversationResult result1 = conv.call(param1);
            System.out.println(JsonUtils.toJson(result1));


            String jsonResponse = JsonUtils.toJson(result1);
            ObjectMapper objectMapper1 = new ObjectMapper();
            JsonNode rootNode0 = objectMapper.readTree(jsonResponse);

// 提取content数组中的文本
            String context = rootNode0.path("output")
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .get(0)
                    .path("text")
                    .asText();

            if(context.equals("1")){
                best=imageUrl;
            }
            else if(context.equals("2")){
                best=imageUrl1;
            }
            else if(context.equals("3")){
                best=imageUrl2;
            }

            double vector1 =  caculate.calculateTextToImagVector(imageUrl,prompt);
            double vector2=   caculate.calculateTextToImagVector(imageUrl1,prompt);
            double vector3 =caculate.calculateTextToImagVector(imageUrl2,prompt);

            if(vector1<vector2&&vector1<vector3){
                best1=imageUrl;
            }
            if (vector2<vector1&&vector2<vector3){
                best1=imageUrl1;
            }
            if (vector3<vector1&&vector3<vector2)
            {
                best1=imageUrl2;
            }

            if(best1!=best){
                best=best1;
            }

            //获取当前时间
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 如果需要格式化时间，可以使用 DateTimeFormatter
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = now.format(formatter);
            System.out.println(phone);
            System.out.println(best);
            chatToImageService.insert( new Images(phone, best,formattedTime,0));

            return objectMapper.writeValueAsString(result3);//返回整个json响应

        } catch (ApiException | NoApiKeyException e) {
            // 捕获异常并返回错误信息
            return "{\"error\": \"" + e.getMessage() + "\"}";
        } catch (Exception e) {
            // 捕获其他异常
            return "{\"error\": \"Unexpected error occurred: " + e.getMessage() + "\"}";
        }
    }

    @GetMapping("/listImage1")
    public Images[] listImage(@RequestParam String phone) throws IOException {
        System.out.println(chatToImageService.findByPhoneAndStatus(phone,0));
//        caculate.calculateVector("0");
//        caculate.calculateTextToImagVector("0","0");

//        openAIv4GenSong.createSong("一只猫",4);

//        vedioToText.toTxet("0");
//        System.out.println(phone);
        return chatToImageService.findByPhoneAndStatus(phone,0).toArray(new Images[0]);
    }
}




//@RestController
//@CrossOrigin(origins = "*") // 允许所有来源的跨域
//public class  chatToImageController {
//
//    @Autowired
//    private chatToImageService chatToImageService;
//
//    private final ImageSynthesis imageSynthesis = new ImageSynthesis();
//    @PostMapping("/chatToImage")//文生图
//    public String chatToImage(@RequestParam String phone,@RequestParam String prompt) {
//
//        try {
//            // 构建参数对象
//            ImageSynthesisParam param = ImageSynthesisParam.builder()
//                    .apiKey("sk-e150afa942024f159288d3fcadf9aa52")
//                    .model(ImageSynthesis.Models.WANX_V1)
//                    .prompt(prompt)
//                    .style("<watercolor>")
//                    .n(1)
//                    .size("1024*1024")
//                    .build();
//
//            // 调用图像合成服务
//            ImageSynthesisResult result3 = imageSynthesis.call(param);
//            ImageSynthesisResult result4 = imageSynthesis.call(param);
//            ImageSynthesisResult result5 = imageSynthesis.call(param);
//
//            // 将结果转换为 JSON 字符串并返回
//            ObjectMapper objectMapper = new ObjectMapper(); // 使用 Jackson 将对象转换为 JSON
//
//            JsonNode rootNode = objectMapper.readTree(objectMapper.writeValueAsString(result3)); // 将 result 转为 JSON 字符串再解析
//            JsonNode rootNode1 = objectMapper.readTree(objectMapper.writeValueAsString(result4)); // 将 result 转为 JSON 字符串再解析
//            JsonNode rootNode2 = objectMapper.readTree(objectMapper.writeValueAsString(result5)); // 将 result 转为 JSON 字符串再解析
//
//            // 提取 URL
//            String imageUrl = rootNode.path("output").path("results").get(0).path("url").asText();
//            String imageUrl1 = rootNode1.path("output").path("results").get(0).path("url").asText();
//            String imageUrl2 = rootNode2.path("output").path("results").get(0).path("url").asText();
//            System.out.println(imageUrl);
//            System.out.println(imageUrl1);
//            System.out.println(imageUrl2);
//
//            MultiModalConversation conv = new MultiModalConversation();
//            MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
//                    .content(Arrays.asList(
//                            Collections.singletonMap("image", imageUrl),
//                            Collections.singletonMap("image", imageUrl1),
//                            Collections.singletonMap("image", imageUrl2),
//                            Collections.singletonMap("text", "请返回质量最高的图片路径"))).build();
//            MultiModalConversationParam param1 = MultiModalConversationParam.builder()
//                    // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
//                    .apiKey("sk-e150afa942024f159288d3fcadf9aa52")
//                    // 此处以qwen-vl-plus为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
//                    .model("qwen-vl-plus")
//                    .message(userMessage)
//                    .build();
//            MultiModalConversationResult result1 = conv.call(param1);
//            System.out.println(JsonUtils.toJson(result1));
//
//            //获取当前时间
//            // 获取当前时间
//            LocalDateTime now = LocalDateTime.now();
//            // 如果需要格式化时间，可以使用 DateTimeFormatter
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//            String formattedTime = now.format(formatter);
//            System.out.println(phone);
//            System.out.println(imageUrl);
//            chatToImageService.insert( new Images(phone, imageUrl,formattedTime,0));
//
//            return objectMapper.writeValueAsString(result3);//返回整个json响应
//
//        } catch (ApiException | NoApiKeyException e) {
//            // 捕获异常并返回错误信息
//            return "{\"error\": \"" + e.getMessage() + "\"}";
//        } catch (Exception e) {
//            // 捕获其他异常
//            return "{\"error\": \"Unexpected error occurred: " + e.getMessage() + "\"}";
//        }
//    }
//
//    @GetMapping("/listImage1")
//    public Images[] listImage(@RequestParam String phone) {
//        System.out.println(chatToImageService.findByPhoneAndStatus(phone,0).toArray(new Images[0]));
//        return chatToImageService.findByPhoneAndStatus(phone,0).toArray(new Images[0]);
//    }
//}
//







//package com.example.ai_manager.controller;
//
//import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
//import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
//import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
//import com.alibaba.dashscope.exception.ApiException;
//import com.alibaba.dashscope.exception.NoApiKeyException;
//import com.example.ai_manager.dao.Images;
//import com.example.ai_manager.service.chatToImageService;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.common.collect.ImmutableMap;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.*;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//@RestController
//@CrossOrigin(origins = "*") // 允许所有来源的跨域
//public class  chatToImageController {
//
//    @Autowired
//    private chatToImageService chatToImageService;
//
//    private final ImageSynthesis imageSynthesis = new ImageSynthesis();
//    @PostMapping("/chatToImage")//文生图
//    public String chatToImage(@RequestParam String phone,@RequestParam String prompt) {
//
//        try {
//            // 构建参数对象
//            ImageSynthesisParam param = ImageSynthesisParam.builder()
//                    .apiKey("sk-e150afa942024f159288d3fcadf9aa52")
//                    .model(ImageSynthesis.Models.WANX_V1)
//                    .prompt(prompt)
//                    .style("<watercolor>")
//                    .n(1)
//                    .size("1024*1024")
//                    .build();
//
//            // 调用图像合成服务
//            ImageSynthesisResult result = imageSynthesis.call(param);
//
//            // 将结果转换为 JSON 字符串并返回
//            ObjectMapper objectMapper = new ObjectMapper(); // 使用 Jackson 将对象转换为 JSON
//
//            JsonNode rootNode = objectMapper.readTree(objectMapper.writeValueAsString(result)); // 将 result 转为 JSON 字符串再解析
//
//            // 提取 URL
//            String imageUrl = rootNode.path("output").path("results").get(0).path("url").asText();
//            System.out.println(imageUrl);
//
//            //获取当前时间
//            // 获取当前时间
//            LocalDateTime now = LocalDateTime.now();
//            // 如果需要格式化时间，可以使用 DateTimeFormatter
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//            String formattedTime = now.format(formatter);
//            System.out.println(phone);
//            System.out.println(imageUrl);
//            chatToImageService.insert( new Images(phone, imageUrl,formattedTime,0));
//
//            return objectMapper.writeValueAsString(result);//返回整个json响应
//
//        } catch (ApiException | NoApiKeyException e) {
//            // 捕获异常并返回错误信息
//            return "{\"error\": \"" + e.getMessage() + "\"}";
//        } catch (Exception e) {
//            // 捕获其他异常
//            return "{\"error\": \"Unexpected error occurred: " + e.getMessage() + "\"}";
//        }
//    }
//
//    @GetMapping("/listImage1")
//    public Images[] listImage(@RequestParam String phone) {
//        System.out.println(chatToImageService.findByPhoneAndStatus(phone,0).toArray(new Images[0]));
//        return chatToImageService.findByPhoneAndStatus(phone,0).toArray(new Images[0]);
//    }
//}
//