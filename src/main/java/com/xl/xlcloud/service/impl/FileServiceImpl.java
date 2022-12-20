package com.xl.xlcloud.service.impl;

import com.xl.xlcloud.common.FileCodes;
import com.xl.xlcloud.config.NonStaticResourceHttpRequestHandler;
import com.xl.xlcloud.dto.FileDTO;
import com.xl.xlcloud.dto.PlayRecordDTO;
import com.xl.xlcloud.dto.ResultMsgDTO;
import com.xl.xlcloud.entity.PlayRecord;
import com.xl.xlcloud.mapper.PlayRecordMapper;
import com.xl.xlcloud.service.FileService;
import com.xl.xlcloud.service.impl.async.FileServiceAsync;
import com.xl.xlcloud.util.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    NonStaticResourceHttpRequestHandler nonStaticResourceHttpRequestHandler;

    @Autowired
    FileServiceAsync fileServiceAsync;
    
    @Autowired
    PlayRecordMapper playRecordMapper;

    @Override
    public ResultMsgDTO listFiles(String rootPath) throws IOException{
        // 先检查有没有该目录、
        Path rootP = Paths.get(rootPath);
        if (!Files.exists(rootP)) {
            return new ResultMsgDTO(false, FileCodes.LIST_FILES_FAIL, "No such file", null);
        }
        // 生成当前目录直链、这个应该交给线程池去做、
        // 为了兼容字幕播放、也需要生成当前目录 sub 子目录的直链、
        // 异步执行、
        fileServiceAsync.writeDirectLink2Redis(rootPath);

        List<FileDTO> files = Files.list(rootP)
                .map(
                        path -> new FileDTO(path.toString().replace("\\", "/"), path.getFileName().toString(), FileUtils.getFileType(path))
                )
                .collect(Collectors.toList());
        return new ResultMsgDTO(true, FileCodes.LIST_FILES_SUCCESS, "List files success.", files);
    }
    

    @Override
    public void downloadFileWithAuth(String filePath, HttpServletResponse response) {
        Path path = Paths.get(filePath);
        if (Files.isDirectory(path)){
            return;
        }

        File file = path.toFile();
        InputStream iStream = null;
        OutputStream oStream = null;
        try {
            //文件是否存在
            if (file.exists()) {
                //设置响应
                response.setContentType("application/octet-stream;charset=UTF-8");
                response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
                response.setHeader("Content-Disposition", "attachment;filename=" + path.getFileName());
                response.setCharacterEncoding("UTF-8");
                iStream = new FileInputStream(file);
                oStream = response.getOutputStream();
                IOUtils.copy(iStream, oStream);
                response.flushBuffer();
            }
        } catch (java.nio.file.NoSuchFileException e) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } finally {
            try {
                if (oStream != null) {
                    oStream.close();
                }

                if (iStream != null) {
                    iStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void playVideoWithAuth(String filePath, HttpServletRequest request, HttpServletResponse response) {
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
    public ResultMsgDTO getPlayRecord(int userId, String filePath) {
        // 计算 快速md5 并到数据库查询播放记录、
        String fileMd5 = FileUtils.getFastMD5(filePath);
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

/*    @Override
    public void playVideoWithAuth(String filePath, HttpServletRequest request, HttpServletResponse response) {
        Path path = Paths.get(filePath);
        if (Files.isDirectory(path)){
            return;
        }

        try {
            File file = path.toFile();
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename="+file.getName().replace(" ", "_"));
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Length", "" + file.length());
            InputStream iStream = new FileInputStream(file);
            IOUtils.copy(iStream, response.getOutputStream());
            response.flushBuffer();
        } catch (java.nio.file.NoSuchFileException e) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }*/

}
