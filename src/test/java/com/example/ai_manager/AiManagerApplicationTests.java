package com.example.ai_manager;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisListResult;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.task.AsyncTaskListParam;
import com.alibaba.dashscope.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiManagerApplicationTests {

    @Test
    void contextLoads() {
    }

    // Copyright (c) Alibaba, Inc. and its affiliates.


        public static void basicCall() throws ApiException, NoApiKeyException {
            String prompt = "近景镜头，18岁的中国女孩，古代服饰，圆脸，正面看着镜头，民族优雅的服装，商业摄影，室外，电影级光照，半身特写，精致的淡妆，锐利的边缘。";
            ImageSynthesisParam param =
                    ImageSynthesisParam.builder()
                            .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                            .model(ImageSynthesis.Models.WANX_V1)
                            .prompt(prompt)
                            .style("<watercolor>")
                            .n(1)
                            .size("1024*1024")
                            .build();

            ImageSynthesis imageSynthesis = new ImageSynthesis();
            ImageSynthesisResult result = null;
            try {
                System.out.println("---sync call, please wait a moment----");
                result = imageSynthesis.call(param);
            } catch (ApiException | NoApiKeyException e){
                throw new RuntimeException(e.getMessage());
            }
            System.out.println(JsonUtils.toJson(result));
        }

        public static void listTask() throws ApiException, NoApiKeyException {
            ImageSynthesis is = new ImageSynthesis();
            AsyncTaskListParam param = AsyncTaskListParam.builder().build();
            ImageSynthesisListResult result = is.list(param);
            System.out.println(result);
        }

        public void fetchTask() throws ApiException, NoApiKeyException {
            String taskId = "your task id";
            ImageSynthesis is = new ImageSynthesis();
            // If set DASHSCOPE_API_KEY environment variable, apiKey can null.
            ImageSynthesisResult result = is.fetch(taskId, null);
            System.out.println(result.getOutput());
            System.out.println(result.getUsage());
        }

        public static void main(String[] args){
            try{
                basicCall();
                //listTask();
            }catch(ApiException|NoApiKeyException e){
                System.out.println(e.getMessage());
            }
        }

}
