package com.example.ai_manager.controller;

import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.alibaba.dashscope.audio.ttsv2.enrollment.Voice;
import com.alibaba.dashscope.audio.ttsv2.enrollment.VoiceEnrollmentService;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.aliyun.oss.common.utils.HttpHeaders;
import com.example.ai_manager.dao.VoiceDTO;
import com.example.ai_manager.dao.Voices;
import com.example.ai_manager.service.createVoiceService;
import com.example.ai_manager.utils.AliOSSUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencentcloudapi.common.AbstractModel;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.nlp.v20190408.NlpClient;
import com.tencentcloudapi.nlp.v20190408.models.AnalyzeSentimentRequest;
import com.tencentcloudapi.nlp.v20190408.models.AnalyzeSentimentResponse;
import com.tencentcloudapi.tts.v20190823.TtsClient;
import com.tencentcloudapi.tts.v20190823.models.TextToVoiceRequest;
import com.tencentcloudapi.tts.v20190823.models.TextToVoiceResponse;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

//@RestController
//@CrossOrigin(origins = "*") // 允许所有来源的跨域
//public class VoiceController {

//
//    @Autowired
//    private AliOSSUtils aliOSSUtils;
//
//    private static final String prefix = "prefix";
//
//    private static String targetModel = "cosyvoice-v1";
//
//    private String[] voiceIds={};
//
//    private String fileUrl="";
//    @PostMapping("/upload/voice")
//    public String upload(MultipartFile image) throws IOException {
//        //调用阿里云OSS工具类，将上传上来的文件存入阿里云
//        String url = aliOSSUtils.upload(image);
//        //将图片上传完成后的url返回，用于浏览器回显展示
//        fileUrl=url;//公共url
//        return url;
//    }
//
//    //查询所有的音色数据
//    @PostMapping("/listVoice")
//    public VoiceDTO[] listVoice() throws IOException, NoApiKeyException, InputRequiredException {
//        VoiceEnrollmentService service = new VoiceEnrollmentService(apiKey);
//        Voice[] voices = service.listVoice(prefix);
//
//         voiceIds =  Arrays.stream(voices)
//                .map(Voice::getVoiceId)  // 直接提取原始 Voice 对象的 voiceId
//                .toArray(String[]::new);
//
//        return Arrays.stream(voices)
//                .map(VoiceDTO::from)
//                .toArray(VoiceDTO[]::new);
//    }
//
//    //删除音色数据
//    @PostMapping("/deleteVoice")
//    public String deleteVoice() throws IOException, NoApiKeyException, InputRequiredException {
//        VoiceEnrollmentService service = new VoiceEnrollmentService(apiKey);
//
//        for (int i=0;i<voiceIds.length;i++){
//            service.deleteVoice(voiceIds[i]);
//        }
//
//        return "删除成功";
////        return service.deleteVoice();
//    }
//
//    @PostMapping("/creatVoice")
//    public String creatVoice(@RequestParam String prompt) throws NoApiKeyException, InputRequiredException {
//        VoiceEnrollmentService service = new VoiceEnrollmentService(apiKey);
//        Voice myVoice = service.createVoice(targetModel, prefix, fileUrl);
//
//
//        System.out.println("RequestId: " + service.getLastRequestId());
//        System.out.println("your voice id is " + myVoice.getVoiceId());
//        // 使用复刻的声音来合成文本为语音
//        SpeechSynthesisParam param = SpeechSynthesisParam.builder()
//                .apiKey(apiKey)
//                .model(targetModel)
//                .voice(myVoice.getVoiceId())
//                .build();
//        SpeechSynthesizer synthesizer = new SpeechSynthesizer(param, null);
//
//        ByteBuffer audio = synthesizer.call(prompt);//生成语音
//        // 保存合成的语音到文件
//        System.out.println("TTS RequestId: " + synthesizer.getLastRequestId());
//
//        // 定义保存文件的路径
//        File directory = new File("D:\\outputvoice");
//        if (!directory.exists()) {
//            // 如果目录不存在，则创建目录
//            directory.mkdirs();
//        }
//
//        // 生成随机文件名
//        Random random = new Random();
//        int randomNumber = random.nextInt(1000); // 生成0到999之间的随机数
//        String fileName = String.format("output%03d.mp3", randomNumber);// 格式化为三位数，例如output123.mp3
//
//        File file = new File(directory,fileName);
//        try (FileOutputStream fos = new FileOutputStream(file)) {
//            fos.write(audio.array());//将语音输出到文件
//            System.out.println(file);
//
//            String url= aliOSSUtils.uploadFileToOSS(String.valueOf(file));
//            System.out.println(url);
//
//            return url;
////            return file.toString();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
//}

//还剩195秒视频   190次语音



@RestController
@CrossOrigin(origins = "*") // 允许所有来源的跨域
public class VoiceController {
    @Value("${tencent.secret-id}")
    private String tencentSecretId;

    @Value("${tencent.secret-key}")
    private String tencentSecretKey;


    @Value("${dashscope.api-key}")
    private String dashScopeApiKey;
    private static final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private AliOSSUtils aliOSSUtils;

    private static final String prefix = "prefix";

    @Autowired
    private createVoiceService createVoiceService;

    private static String targetModel = "cosyvoice-v1";

    private String[] voiceIds={};

    private String fileUrl="";
    @PostMapping("/upload/voice")
    public String upload(MultipartFile image) throws IOException {
        //调用阿里云OSS工具类，将上传上来的文件存入阿里云
        String url = aliOSSUtils.upload(image);
        //将图片上传完成后的url返回，用于浏览器回显展示
        fileUrl=url;//公共url
        return url;
    }

    //降噪



    //查询所有的音色数据
    @PostMapping("/listVoice")
    public VoiceDTO[] listVoice() throws IOException, NoApiKeyException, InputRequiredException {
        VoiceEnrollmentService service = new VoiceEnrollmentService(dashScopeApiKey);
        Voice[] voices = service.listVoice(prefix);

        voiceIds =  Arrays.stream(voices)
                .map(Voice::getVoiceId)  // 直接提取原始 Voice 对象的 voiceId
                .toArray(String[]::new);

        return Arrays.stream(voices)
                .map(VoiceDTO::from)
                .toArray(VoiceDTO[]::new);
    }

    //删除音色数据
    @PostMapping("/deleteVoice")
    public String deleteVoice() throws IOException, NoApiKeyException, InputRequiredException {
        VoiceEnrollmentService service = new VoiceEnrollmentService(dashScopeApiKey);

        for (int i=0;i<voiceIds.length;i++){
            service.deleteVoice(voiceIds[i]);
        }

        return "删除成功";
//        return service.deleteVoice();
    }

    @PostMapping("/creatVoice")
    public String creatVoice(@RequestParam String prompt) throws NoApiKeyException, InputRequiredException {
        VoiceEnrollmentService service = new VoiceEnrollmentService(dashScopeApiKey);
        Voice myVoice = service.createVoice(targetModel, prefix, fileUrl);


        System.out.println("RequestId: " + service.getLastRequestId());
        System.out.println("your voice id is " + myVoice.getVoiceId());
        // 使用复刻的声音来合成文本为语音
        SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                .apiKey(dashScopeApiKey)
                .model(targetModel)
                .voice(myVoice.getVoiceId())
                .build();
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(param, null);

        ByteBuffer audio = synthesizer.call(prompt);//生成语音
        // 保存合成的语音到文件
        System.out.println("TTS RequestId: " + synthesizer.getLastRequestId());

        // 定义保存文件的路径
        File directory = new File("D:\\outputvoice");
        if (!directory.exists()) {
            // 如果目录不存在，则创建目录
            directory.mkdirs();
        }

        // 生成随机文件名
        Random random = new Random();
        int randomNumber = random.nextInt(1000); // 生成0到999之间的随机数
        String fileName = String.format("output%03d.mp3", randomNumber);// 格式化为三位数，例如output123.mp3

        File file = new File(directory,fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(audio.array());//将语音输出到文件
            System.out.println(file);

            String url= aliOSSUtils.uploadFileToOSS(String.valueOf(file));
            System.out.println(url);


            Voices Voice = new Voices();
            Voice.setPrompt(prompt);
            Voice.setUrl(url);
            Voice.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            createVoiceService.insert(Voice);

            return url;
//            return file.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/creatVoice2")
    public String creatVoice2(@RequestParam String prompt) throws NoApiKeyException, InputRequiredException, TencentCloudSDKException {

            try{

                // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
                Credential cred = new Credential(tencentSecretId, tencentSecretKey);
                // 使用临时密钥示例
                // Credential cred = new Credential("SecretId", "SecretKey", "Token");
                // 实例化一个http选项，可选的，没有特殊需求可以跳过
                HttpProfile httpProfile = new HttpProfile();
                httpProfile.setEndpoint("nlp.tencentcloudapi.com");
                // 实例化一个client选项，可选的，没有特殊需求可以跳过
                ClientProfile clientProfile = new ClientProfile();
                clientProfile.setHttpProfile(httpProfile);
                // 实例化要请求产品的client对象,clientProfile是可选的
                NlpClient client = new NlpClient(cred, "", clientProfile);
//                System.out.println(client);
                // 实例化一个请求对象,每个接口都会对应一个request对象
                AnalyzeSentimentRequest req = new AnalyzeSentimentRequest();
                req.setText(prompt);
                // 返回的resp是一个AnalyzeSentimentResponse的实例，与请求对象对应
                AnalyzeSentimentResponse resp = client.AnalyzeSentiment(req);

                // 1. 解析JSON
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(AbstractModel.toJsonString(resp));
                System.out.println(root);
                // 2. 直接提取路径
                String sentiment = root
                        .get("Sentiment")  // 获取Sentiment字段
                        .asText();         // 转为字符串

                System.out.println("sentiment: " + sentiment);
                if(sentiment.equals("positive")){
                    sentiment="happy";
                }
                if(sentiment.equals("negative")){
                    sentiment="sad";
                }
                if(sentiment.equals("neutral"))
                {
                    sentiment="neutral";
                }
                // 输出json格式的字符串回包
//                System.out.println(AbstractModel.toJsonString(resp));

                System.out.println(sentiment);

                //生成语音
                // 实例化一个http选项，可选的，没有特殊需求可以跳过
                HttpProfile httpProfile1 = new HttpProfile();
                httpProfile1.setEndpoint("tts.tencentcloudapi.com");
                // 实例化一个client选项，可选的，没有特殊需求可以跳过
                ClientProfile clientProfile1 = new ClientProfile();
                clientProfile.setHttpProfile(httpProfile1);
                // 实例化要请求产品的client对象,clientProfile是可选的
                TtsClient client1 = new TtsClient(cred, "", clientProfile1);


                // 实例化一个请求对象,每个接口都会对应一个request对象
                TextToVoiceRequest req1 = new TextToVoiceRequest();

                req1.setVoiceType(301004L);
                req1.setEmotionCategory(sentiment);
                System.out.println(req1.getEmotionCategory());
                req1.setText(prompt);
                String sessionId = UUID.randomUUID().toString(); // 生成唯一ID
                req1.setSessionId(sessionId);


                // 返回的resp是一个TextToVoiceResponse的实例，与请求对象对应
                TextToVoiceResponse resp1 = client1.TextToVoice(req1);

//                System.out.println(AbstractModel.toJsonString(resp1));
                    // 1. 解析JSON
                    ObjectMapper mapper2 = new ObjectMapper();
                    JsonNode rootNode = mapper2.readTree(AbstractModel.toJsonString(resp1));
                    String base64Audio = rootNode.path("Audio").asText();

                    // 2. Base64解码
                    byte[] audioBytes = Base64.getDecoder().decode(base64Audio);

                    // 3. 生成UUID文件名
                    String uuid = UUID.randomUUID().toString().replace("-", ""); // 生成无连字符的UUID
                    String fileName = uuid + ".wav"; // 组合文件名

                    // 4. 定义目标路径（D盘outputvoice目录）
                    Path outputPath = Paths.get("D:\\outputvoice\\" + fileName);

                    // 确保目录存在（自动创建）
                    Files.createDirectories(outputPath.getParent());

                    // 5. 写入文件
                    Files.write(outputPath, audioBytes);

                    System.out.println("音频文件已保存至: " + outputPath);

                // 输出json格式的字符串回包
//                System.out.println(AbstractModel.toJsonString(resp));

                String url=aliOSSUtils.uploadFileToOSS("D:\\outputvoice\\" + fileName);


                Voices Voice = new Voices();
                Voice.setPrompt(prompt);
                Voice.setUrl(url);
                Voice.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                createVoiceService.insert(Voice);


                return url;



            } catch (JsonProcessingException e) {
                System.out.println(e.toString());
            } catch (IOException e) {
                    e.printStackTrace();
                }

        return "";
            }
}