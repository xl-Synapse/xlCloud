package com.xl.xlcloud.controller;

import com.xl.xlcloud.dto.PlayRecordDTO;
import com.xl.xlcloud.dto.ResultMsgDTO;
import com.xl.xlcloud.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class VideoController {
    @Autowired
    VideoService videoServiceImpl;

    @GetMapping("/video/{filePath}")
    public void playVideoWithAuth(@PathVariable String filePath, HttpServletRequest request, HttpServletResponse response) {
//        String rootPath = getParamFromRequest(request);
        filePath = filePath.replace("&", "/");
        System.out.println("video" + filePath);
        videoServiceImpl.playVideoWithAuth(filePath, request, response);
    }

    @GetMapping("/filerecord/{userId}")
    public ResultMsgDTO getPlayedVideo(@PathVariable int userId, @RequestParam("fileMd5s") List<String> fileMd5s) {
        return videoServiceImpl.getPlayedVideo(userId, fileMd5s);
    }

    @GetMapping("/convertinfo/{filePath}")
    public ResultMsgDTO getConvertInfo(@PathVariable String filePath) {
        filePath = filePath.replace("&", "/");
        return videoServiceImpl.getConvertInfo(filePath);
    }

    @GetMapping("/playrecord/{userId}&&{filePath}")
    public ResultMsgDTO getPlayRecord(@PathVariable int userId, @PathVariable String filePath) {
        filePath = filePath.replace("&", "/");
        return videoServiceImpl.getPlayRecord(userId, filePath);
    }

    @PutMapping("/playrecord")
    public void putPlayRecord(@RequestBody PlayRecordDTO playRecordDTO) {
        videoServiceImpl.updatePlayRecord(playRecordDTO);
    }
}
