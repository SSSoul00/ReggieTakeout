package com.itheima.controller;

import com.alibaba.fastjson.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@RestController
@RequestMapping("common")
@Slf4j
public class CommonController {
    //从yaml文件读取存储文件目录
    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     *
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        //截取原始文件后缀
        String originalFilename = file.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));

        //判断文件夹是否存在，若不存在则创建
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        //定义文件名，利用UUID保证不重复
        String fileName = UUID.randomUUID().toString() + substring;

        //将文件转存到指定位置
        file.transferTo(new File(basePath + fileName));
        return R.success(fileName);
    }

    /**
     * 下载文件
     * @param name
     * @param response
     * @throws IOException
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {
        response.setContentType("image/jpeg");
        FileInputStream is = new FileInputStream(basePath + name);
        ServletOutputStream os = response.getOutputStream();

        byte[] bytes = new byte[1024];
        int len = 0;
        while ((len = is.read(bytes)) != -1){
            os.write(bytes,0,len);
            os.flush();
        }

        is.close();
        os.close();
    }
}
