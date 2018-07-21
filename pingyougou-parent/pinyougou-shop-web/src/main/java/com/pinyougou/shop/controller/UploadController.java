package com.pinyougou.shop.controller;

import com.pinyougou.util.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    @Value("${IMAGE_SERVER_URL}")
    private String IMAGE_SERVER_URL;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file)  {
        String originalFilename = file.getOriginalFilename();  //获取文件名
        String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);  //得到扩展名
        FastDFSClient dfsClient = null;
        try {
            dfsClient = new FastDFSClient("classpath:config/client.conf");
            String uploadFile = dfsClient.uploadFile(file.getBytes(), extName);
            String url = IMAGE_SERVER_URL + uploadFile;  //图片完整地址
            return new Result(true, url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传失败");
        }



    }
}
