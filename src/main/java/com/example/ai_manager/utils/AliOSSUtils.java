package com.example.ai_manager.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
public class AliOSSUtils {
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    /**
     * 实现上传文件到OSS
     */
    public String upload(MultipartFile multipartFile) throws IOException {
        // 获取上传的文件的输入流
        InputStream inputStream = multipartFile.getInputStream();
        System.out.println(endpoint);
        // 避免文件覆盖
        String originalFilename = multipartFile.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));

        // 上传文件到 OSS
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, fileName, inputStream);

        // 构造文件访问路径（更安全的方式）
        String url;
        if (endpoint != null && (endpoint.startsWith("http://") || endpoint.startsWith("https://"))){
            String[] parts = endpoint.split("//");
            if (parts.length > 1) {
                url = endpoint.split("//")[0] + "//" + bucketName + "." + parts[1] + "/" + fileName;
            } else {
                // 如果 split 结果不符合预期，使用默认方式构造（这里仅作示例，实际应根据业务需求处理）
                url = "https://" + bucketName + "." + endpoint.replace("http://", "").replace("https://", "") + "/" + fileName;
            }
        } else {
            throw new IllegalArgumentException("无效的 endpoint 地址");
        }


        ossClient.shutdown();
        return url; // 把上传到 oss 的路径返回
    }


    public String uploadFileToOSS(String localFilePath) throws IOException {
        File file = new File(localFilePath);
        if (!file.exists()) {
            throw new FileNotFoundException("本地文件不存在: " + localFilePath);
        }

        String fileName = file.getName();
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + fileExtension;

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ossClient.putObject(bucketName, newFileName, file);

            // 构造URL
            String url;
            if (endpoint.startsWith("http://")) {
                url = "http://" + bucketName + "." + endpoint.substring(7) + "/" + newFileName;
            } else if (endpoint.startsWith("https://")) {
                url = "https://" + bucketName + "." + endpoint.substring(8) + "/" + newFileName;
            } else {
                url = "https://" + bucketName + "." + endpoint + "/" + newFileName;
            }

            return url;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
            // 可选：删除本地临时文件
            // file.delete();
        }
    }

}
