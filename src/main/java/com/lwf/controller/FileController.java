package com.lwf.controller;

import com.lwf.utils.AliOSSUtils;
import com.lwf.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class FileController {

    @Autowired
    private AliOSSUtils aliOSSUtils;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws IOException {


        String upload = aliOSSUtils.upload(file);


        return Result.success(upload);
    }
}
