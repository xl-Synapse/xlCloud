package com.xl.xlcloud.service.impl;

import com.xl.xlcloud.common.FileCodes;
import com.xl.xlcloud.dto.FileDTO;
import com.xl.xlcloud.dto.ResultMsgDTO;
import com.xl.xlcloud.service.FileService;
import com.xl.xlcloud.util.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @Override
    public ResultMsgDTO listFiles(String rootPath) throws IOException{
        // 生成当前目录直链、这个应该交给线程池去做、
        stringRedisTemplate.opsForValue().set(
                FileCodes.FILE_DIRECT_LINK_PREFIX + "/" + (rootPath.equals("") ? "" : rootPath + "/"), "1",
                FileCodes.FILE_DIRECT_LINK_TTL, TimeUnit.MINUTES
        );

        List<FileDTO> files = Files.list(Paths.get(rootPath))
                .map(
                        path -> new FileDTO(path.toString().replace("\\", "/"), path.getFileName().toString(), FileUtils.getFileType(path))
                )
                .collect(Collectors.toList());
        return new ResultMsgDTO(true, FileCodes.LIST_FILES_SUCCESS, "List files success.", files);
    }

    @Override
    public void downloadFileWithoutAuth(String rootPath, HttpServletRequest request, HttpServletResponse response) {
        Path path = Paths.get(rootPath);
        if (Files.isDirectory(path)){
            return;
        }

        File file = path.toFile();
        byte[] buffer = new byte[1024];
        BufferedInputStream bis = null;
        OutputStream os = null;
        try {
            //文件是否存在
            if (file.exists()) {
                //设置响应
                response.setContentType("application/octet-stream;charset=UTF-8");
                response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
                response.setHeader("Content-Disposition","attachment;filename=" + path.getFileName());
                response.setCharacterEncoding("UTF-8");
                os = response.getOutputStream();
                bis = new BufferedInputStream(new FileInputStream(file));
                while(bis.read(buffer) != -1){
                    os.write(buffer);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(bis != null) {
                    bis.close();
                }
                if(os != null) {
                    os.flush();
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
    public void playVideoWithAuth(String filePath, HttpServletResponse response) {
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
    }

}
