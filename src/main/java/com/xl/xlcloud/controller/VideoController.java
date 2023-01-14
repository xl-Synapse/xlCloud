package com.xl.xlcloud.controller;

import com.xl.xlcloud.dto.PlayRecordDTO;
import com.xl.xlcloud.dto.ResultMsgDTO;
import com.xl.xlcloud.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
import cn.hutool.core.codec.Base64;
import java.util.List;

@RestController
public class VideoController {
    @Autowired
    VideoService videoServiceImpl;


    @GetMapping("/video/{filePath}")
    public void playVideo(
            @PathVariable String filePath, HttpServletRequest request, HttpServletResponse response
            , @RequestHeader HttpHeaders headers
    ) {
        filePath = new String(Base64.decode(filePath), StandardCharsets.UTF_8);
//        videoServiceImpl.playVideoWithAuth(filePath, request, response); // 旧播放模式、较为卡顿、
        videoServiceImpl.playVideo(filePath, request, response, headers);
    }

    @GetMapping("/filerecord/{userId}")
    public ResultMsgDTO getPlayedVideo(@PathVariable int userId, @RequestParam("fileMd5s") List<String> fileMd5s) {
        return videoServiceImpl.getPlayedVideo(userId, fileMd5s);
    }

    @GetMapping("/convertinfo/{filePath}")
    public ResultMsgDTO getConvertInfo(@PathVariable String filePath) {
        filePath = new String(Base64.decode(filePath), StandardCharsets.UTF_8);
        return videoServiceImpl.getConvertInfo(filePath);
    }

    @GetMapping("/playrecord/{userId}&&{filePath}") // 后期应该修改为直接使用 md5 查询、
    public ResultMsgDTO getPlayRecord(@PathVariable int userId, @PathVariable String filePath) {
        filePath = new String(Base64.decode(filePath), StandardCharsets.UTF_8);
        return videoServiceImpl.getPlayRecord(userId, filePath);
    }

    @PutMapping("/playrecord")
    public void putPlayRecord(@RequestBody PlayRecordDTO playRecordDTO) {
        videoServiceImpl.updatePlayRecord(playRecordDTO);
    }

    /**
     * 兼容pc端 potplayer + autohotkey 播放记录、
     * */

    @GetMapping("/playrecordpp/{userId}&&{filePath}&&{position}")
    public void putPlayRecordPotPlayer(@PathVariable int userId, @PathVariable String filePath, @PathVariable String position) {
        filePath = new String(Base64.decode(filePath), StandardCharsets.UTF_8);

        // 时间要根据格式做转换、
        String[] positionArray = position.split(":");
        int positionInt = 0;
        for (int i = positionArray.length - 1; i >= 0; i--) {
            positionInt =
                    (int) Math.pow(60, positionArray.length - i - 1) * Integer.parseInt(positionArray[i])
                            + positionInt;
        }
        videoServiceImpl.updatePlayRecordByPath(userId, filePath, positionInt);
    }


}
