package com.xl.xlcloud.controller;

import com.xl.xlcloud.common.FileCodes;
import com.xl.xlcloud.dto.FileDTO;
import com.xl.xlcloud.dto.PlayRecordDTO;
import com.xl.xlcloud.dto.ResultMsgDTO;
import com.xl.xlcloud.service.FileService;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FileController {

    @Autowired
    FileService fileServiceImpl;

    // 适配根目录、
    @GetMapping(value = "/files/")
    public ResultMsgDTO listFilesForRoot(HttpServletRequest request) {
        return fileServiceImpl.listFiles("");
    }

    @GetMapping(value = "/files/{filePath}")
    public ResultMsgDTO listFiles(@PathVariable String filePath, HttpServletRequest request) {
        filePath = filePath.replace("&", "/");
        return fileServiceImpl.listFiles(filePath);
    }

    @GetMapping("/file/{filePath}")
    public void downloadFileWithAuth(@PathVariable String filePath, HttpServletRequest request, HttpServletResponse response) {
//        String rootPath = getParamFromRequest(request);
        filePath = filePath.replace("&", "/");
        fileServiceImpl.downloadFile(filePath, response);
    }


    private String getParamFromRequest(HttpServletRequest request) {
        String path1 = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        String path2 = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();

        String result = new AntPathMatcher().extractPathWithinPattern(path2, path1);
        try {
            // 自己编码了一次、浏览器编码了一次、第一次解码会把 % 处理掉、第二次解码需要额外处理 +
            // 直接从 request 里拿出来就需要解码两次、
            // 这里只解码一次、模拟 tomcat 自动解码、
/*            return URLDecoder.decode(
                    URLDecoder.decode(
                        result.replace("+", "%2B"),
                        String.valueOf(StandardCharsets.UTF_8)
                    ).replace("+", "%2B"),
                    String.valueOf(StandardCharsets.UTF_8)
            );*/
            return URLDecoder.decode(
                    result.replace("+", "%2B"),
                    String.valueOf(StandardCharsets.UTF_8)
            );

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
