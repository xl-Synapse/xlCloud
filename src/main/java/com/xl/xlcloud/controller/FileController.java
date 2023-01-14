package com.xl.xlcloud.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xl.xlcloud.common.FileCodes;
import com.xl.xlcloud.dto.FileDTO;
import com.xl.xlcloud.dto.PlayRecordDTO;
import com.xl.xlcloud.dto.ResultMsgDTO;
import com.xl.xlcloud.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;

import cn.hutool.core.codec.Base64;


@Slf4j
@RestController
public class FileController {

    @Autowired
    FileService fileServiceImpl;


    // 适配根目录、
    @GetMapping(value = "/files/")
    public ResultMsgDTO listFilesForRoot() throws UnsupportedEncodingException, JsonProcessingException {
        return fileServiceImpl.listFiles("./");
    }

    @GetMapping(value = "/files/{filePath}")
    public ResultMsgDTO listFiles(@PathVariable String filePath) throws UnsupportedEncodingException, JsonProcessingException {
        filePath = new String(Base64.decode(filePath), StandardCharsets.UTF_8);
        return fileServiceImpl.listFiles(filePath);
    }

    @GetMapping("/file/{filePath}")
    public void downloadFileWithAuth(@PathVariable String filePath, HttpServletResponse response) {
        filePath = new String(Base64.decode(filePath), StandardCharsets.UTF_8);
        log.info("download:" + filePath);
        fileServiceImpl.downloadFile(filePath, response);
    }
}
