package com.example.ai_manager.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class Caculate {
    @Value("${dashscope.api-key}")
    private String dashScopeApiKey;



    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/embeddings/multimodal-embedding/multimodal-embedding";
    private static final ObjectMapper mapper = new ObjectMapper();


    List<Double> embeddingList = new ArrayList<>();
    List<Double> embeddingList1 = new ArrayList<>();
    public String calculateVector(String imageUrl) {
        try {
//            System.out.println(callMultimodalAPI(imageUrl));
            return callMultimodalAPI(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public double calculateTextToImagVector(String imageUrl,String prompt) {
        double sum=0;
        try {
//            System.out.println(callMultimodalAPI(imageUrl));
            callMultimodalAPI(imageUrl);
            callMultimodalTextAPI(prompt);
            for (int i = 0; i < embeddingList.size(); i++){
                sum=sum+embeddingList.get(i)-embeddingList1.get(i);
            }
            System.out.println(sum);
            return sum;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String callMultimodalAPI(String imageUrl) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 1. 构建请求体
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("model", "multimodal-embedding-v1");

            // 构建 contents 数组
            ArrayNode contentsArray = mapper.createArrayNode();

            // 添加文本内容（可选）
//            ObjectNode textNode = mapper.createObjectNode();
//            textNode.put("text", "通用多模态表征模型");
//            contentsArray.add(textNode);

            // 添加图片内容
            ObjectNode imageNode = mapper.createObjectNode();
            imageNode.put("image", imageUrl);
            contentsArray.add(imageNode);

            // 添加视频内容（可选）
//            ObjectNode videoNode = mapper.createObjectNode();
//            videoNode.put("video", "https://aigc-sw.oss-cn-beijing.aliyuncs.com/9db83b45-0c1f-408e-8b3a-09ab33cd7b56.mp4");
//            contentsArray.add(videoNode);

            // 设置 input 和 parameters
            ObjectNode inputNode = mapper.createObjectNode();
            inputNode.set("contents", contentsArray);
            requestBody.set("input", inputNode);
            requestBody.set("parameters", mapper.createObjectNode()); // 空参数

            // 2. 配置HTTP请求
            HttpPost httpPost = new HttpPost(API_URL);
            httpPost.addHeader("Authorization", "Bearer " + dashScopeApiKey);
            httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");

            StringEntity entity = new StringEntity(
                    requestBody.toString(),
                    StandardCharsets.UTF_8
            );
            httpPost.setEntity(entity);
//            System.out.println("Request URL: " + API_URL);
//            System.out.println("Request Headers: " + httpPost.getAllHeaders());
//            System.out.println("Request Body: " + requestBody.toPrettyString());

            // 3. 处理响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                HttpEntity responseEntity = response.getEntity();
                String responseBody = EntityUtils.toString(responseEntity);
//                System.out.println(responseBody);
                if (statusCode == 200) {

                    // 将JSON字符串解析为JsonNode对象
                    JsonNode rootNode = mapper.readTree(responseBody);

                    // 定位到embedding数组
                    JsonNode embeddingsNode = rootNode
                            .path("output")          // 进入output对象
                            .path("embeddings")      // 进入embeddings数组
                            .get(0)                 // 取第一个元素（根据实际索引调整）
                            .path("embedding");     // 获取embedding数组

                    // 将JsonNode转换为List<Double>

                    if (embeddingsNode.isArray()) {
                        for (JsonNode valueNode : embeddingsNode) {
                            embeddingList.add(valueNode.asDouble());
                        }
                    }
//                    System.out.println(embeddingList);

                    return responseBody;
                } else {
                    throw new RuntimeException(
                            "API请求失败，状态码：" + statusCode + "\n响应内容：" + responseBody
                    );
                }
            }
        }
    }

    private String callMultimodalTextAPI(String prompt) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 1. 构建请求体
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("model", "multimodal-embedding-v1");

            // 构建 contents 数组
            ArrayNode contentsArray = mapper.createArrayNode();

            // 添加文本内容（可选）
            ObjectNode textNode = mapper.createObjectNode();
            textNode.put("text", prompt);
            contentsArray.add(textNode);

            // 添加图片内容
//            ObjectNode imageNode = mapper.createObjectNode();
//            imageNode.put("image", "https://aigc-sw.oss-cn-beijing.aliyuncs.com/1e7611a7-cf68-4d22-967d-25b5e91f20ec.jpg");
//            contentsArray.add(imageNode);

            // 添加视频内容（可选）
//            ObjectNode videoNode = mapper.createObjectNode();
//            videoNode.put("video", "https://aigc-sw.oss-cn-beijing.aliyuncs.com/9db83b45-0c1f-408e-8b3a-09ab33cd7b56.mp4");
//            contentsArray.add(videoNode);

            // 设置 input 和 parameters
            ObjectNode inputNode = mapper.createObjectNode();
            inputNode.set("contents", contentsArray);
            requestBody.set("input", inputNode);
            requestBody.set("parameters", mapper.createObjectNode()); // 空参数

            // 2. 配置HTTP请求
            HttpPost httpPost = new HttpPost(API_URL);
            httpPost.addHeader("Authorization", "Bearer " + dashScopeApiKey);
            httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");

            StringEntity entity = new StringEntity(
                    requestBody.toString(),
                    StandardCharsets.UTF_8
            );
            httpPost.setEntity(entity);
//            System.out.println("Request URL: " + API_URL);
//            System.out.println("Request Headers: " + httpPost.getAllHeaders());
//            System.out.println("Request Body: " + requestBody.toPrettyString());

            // 3. 处理响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                HttpEntity responseEntity = response.getEntity();
                String responseBody = EntityUtils.toString(responseEntity);
                System.out.println(responseBody);
                if (statusCode == 200) {

                    // 将JSON字符串解析为JsonNode对象
                    JsonNode rootNode = mapper.readTree(responseBody);

                    // 定位到embedding数组
                    JsonNode embeddingsNode = rootNode
                            .path("output")          // 进入output对象
                            .path("embeddings")      // 进入embeddings数组
                            .get(0)                 // 取第一个元素（根据实际索引调整）
                            .path("embedding");     // 获取embedding数组

                    // 将JsonNode转换为List<Double>

                    if (embeddingsNode.isArray()) {
                        for (JsonNode valueNode : embeddingsNode) {
                            embeddingList1.add(valueNode.asDouble());
                        }
                    }
//                    System.out.println(embeddingList1);
                    return responseBody;
                } else {
                    throw new RuntimeException(
                            "API请求失败，状态码：" + statusCode + "\n响应内容：" + responseBody
                    );
                }
            }
        }
    }
}
