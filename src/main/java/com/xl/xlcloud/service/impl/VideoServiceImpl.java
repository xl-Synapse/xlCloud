package com.xl.xlcloud.service.impl;

import com.xl.xlcloud.common.FileCodes;
import com.xl.xlcloud.common.VideoCodes;
import com.xl.xlcloud.config.NonStaticResourceHttpRequestHandler;
import com.xl.xlcloud.dto.PlayRecordDTO;
import com.xl.xlcloud.dto.ResultMsgDTO;
import com.xl.xlcloud.entity.ConvertInfo;
import com.xl.xlcloud.entity.PlayRecord;
import com.xl.xlcloud.mapper.ConvertInfoMapper;
import com.xl.xlcloud.mapper.PlayRecordMapper;
import com.xl.xlcloud.service.VideoService;
import com.xl.xlcloud.service.impl.async.VideoServiceAsync;
import com.xl.xlcloud.util.FileUtils;
import com.xl.xlcloud.util.VideoUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {
    @Autowired
    PlayRecordMapper playRecordMapper;

    @Autowired
    ConvertInfoMapper convertInfoMapper;

    @Autowired
    VideoServiceAsync videoServiceAsync;

    @Autowired
    NonStaticResourceHttpRequestHandler nonStaticResourceHttpRequestHandler;

    @Value("${root-path}")
    String rootPath;

    // 已经弃用、
    @Override
    public void playVideoWithAuth(String filePath, HttpServletRequest request, HttpServletResponse response) {
        filePath = rootPath + filePath;

        Path path = Paths.get(filePath);
        if (Files.isDirectory(path)){
            return;
        }

        try {
            File file = path.toFile();
            if (file.exists()) {
                request.setAttribute(NonStaticResourceHttpRequestHandler.ATTR_FILE, path.toString());
                nonStaticResourceHttpRequestHandler.handleRequest(request, response);
            }
        } catch (java.nio.file.NoSuchFileException e) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public void playVideo(String filePath, HttpServletRequest request, HttpServletResponse response, HttpHeaders headers) {
        filePath = rootPath + filePath;

        try {
            VideoUtils.download(filePath, request, response, headers);
        } catch (Exception e) {
            log.error("getMedia error, fileName={}", filePath, e);
        }
    }

    @Override
    public ResultMsgDTO getPlayedVideo(int userId, List<String> fileMd5s) {
        List<String> resultFileMd5s =  playRecordMapper.getMd5sIn(userId, fileMd5s);
        return new ResultMsgDTO(true, VideoCodes.PLAYED_SUCCESS, "success", resultFileMd5s);
    }

    @Override
    public ResultMsgDTO getConvertInfo(String filePath) {
        filePath = rootPath + filePath;

        Path path = Paths.get(filePath);
        if (Files.isDirectory(path)){
            return new ResultMsgDTO(false, VideoCodes.GET_CONVERTINFO_NO_SUCH_FILE, "no such file", null);
        }


        File file = path.toFile();
        if (!file.exists()) {
            return new ResultMsgDTO(false, VideoCodes.GET_CONVERTINFO_NO_SUCH_FILE, "no such file", null);
        }

        if (!file.getName().toLowerCase().endsWith(".mkv")) {
            // 非 mkv 不转码、
            return new ResultMsgDTO(false, VideoCodes.GET_CONVERTINFO_SHOULD_NOT_CONVERT_FILE, "no mkv file", null);
        }

        // 查询是否存在转换记录、
        String fileMd5 = FileUtils.getFastMD5(Paths.get(filePath));
        ConvertInfo convertInfo = convertInfoMapper.getConvertByMd5(fileMd5);

        if (convertInfo == null) {
            // 没有转码过、启动转码、
            // 暂时关闭转码功能、等待适配 svp 转码、
//            videoServiceAsync.convert2Mp4(path, fileMd5);
            return new ResultMsgDTO(false, VideoCodes.GET_CONVERTINFO_WAIT_FOR_CONVERT, "wait for convert", null);
        }

        // 存在转码记录、但仍需要判断是否已经转换完毕了、
        if (convertInfo.getTime() == null) {

            return new ResultMsgDTO(false, VideoCodes.GET_CONVERTINFO_WAIT_FOR_CONVERT, "wait for convert", null);
        }


        // 转码记录存在、但仍需要检查目标文件是否存在、
        Path convertedPath = Paths.get(".cache/" + fileMd5 + ".mp4");
        File convertedFile = convertedPath.toFile();
        if (!convertedFile.exists()) {
            // 不存在、转码表出错、删除出错记录、并启动转码、
            convertInfoMapper.deleteConvertInfo(convertInfo);
            videoServiceAsync.convert2Mp4(path, fileMd5);
        }


        // 正常逻辑、返回转码后的文件md5用于前端播放、
        return new ResultMsgDTO(true, VideoCodes.GET_CONVERTINFO_SUCCESS, "success", convertInfo.getFileMd5());

    }


    @Override
    public ResultMsgDTO getPlayRecord(int userId, String filePath) {
        filePath = rootPath + filePath;

        // 计算 快速md5 并到数据库查询播放记录、
        String fileMd5 = FileUtils.getFastMD5(Paths.get(filePath));
        if (fileMd5.equals("")) {
            return new ResultMsgDTO(true, FileCodes.GET_RECORD_SUCCESS, "No record.", new PlayRecordDTO(userId, fileMd5, 0));
        }

        PlayRecord playRecord = playRecordMapper.getPlayRecordByMd5(userId, fileMd5);
        if (playRecord == null) {
            // 没记录、
            return new ResultMsgDTO(true, FileCodes.GET_RECORD_SUCCESS, "No record.", new PlayRecordDTO(userId, fileMd5, 0));
        }

        PlayRecordDTO playRecordDTO = new PlayRecordDTO();
        BeanUtils.copyProperties(playRecord, playRecordDTO);
        return new ResultMsgDTO(true, FileCodes.GET_RECORD_SUCCESS, "Get record.", playRecord);
    }

    @Async
    @Override
    public void updatePlayRecord(PlayRecordDTO playRecordDTO) {

        // 数据检查、
        if (
                playRecordDTO == null ||
                        StringUtils.isBlank(playRecordDTO.getFileMd5()) ||
                        playRecordDTO.getUserId() <= 0 ||
                        playRecordDTO.getPosition() <= 0
        ) {
            return;
        }

        PlayRecord playRecord = new PlayRecord();
        BeanUtils.copyProperties(playRecordDTO, playRecord);
        // 尝试更新、失败则新建、
        int successNum = playRecordMapper.updatePlayRecord(playRecord);
        if (successNum <= 0) {
            playRecordMapper.createPlayRecord(playRecord);
        }
    }

    @Async
    @Override
    public void updatePlayRecordByPath(int userId, String filePath, int position) {
        if (userId <= 0 || StringUtils.isBlank(filePath) || position <= 0) {
            return;
        }

        String fileMd5 = FileUtils.getFastMD5(Paths.get(filePath));
        if (StringUtils.isBlank(fileMd5)) {
            return;
        }

        PlayRecordDTO playRecordDTO = new PlayRecordDTO(userId, fileMd5, position);
        updatePlayRecord(playRecordDTO);
    }
}
