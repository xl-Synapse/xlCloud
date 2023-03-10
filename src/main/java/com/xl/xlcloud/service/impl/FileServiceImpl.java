package com.xl.xlcloud.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xl.xlcloud.common.FileCodes;
import com.xl.xlcloud.config.NonStaticResourceHttpRequestHandler;
import com.xl.xlcloud.dto.FileDTO;
import com.xl.xlcloud.dto.ListFilesCacheDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
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

    @Value("${root-path}")
    String rootPath;

    @Override
    public ResultMsgDTO listFiles(String filePath) throws UnsupportedEncodingException, JsonProcessingException {
        filePath = rootPath + filePath;

        System.out.println("list: " + filePath);

        // ??????????????????????????????
        Path rootP = Paths.get(filePath);
        if (!Files.exists(rootP)) {
            return new ResultMsgDTO(false, FileCodes.LIST_FILES_FAIL, "No such file", null);
        }

        //------------------???????????????????????????????????????
        fileServiceAsync.writeDirectLink2Redis(filePath);


        //------------------???????????????????????????????????????md5?????????????????????
        ObjectMapper objectMapper = new ObjectMapper();

        // redis ???????????????
        String cacheKey = FileCodes.LIST_FILES_CACHE_PREFIX + URLEncoder.encode(filePath, "UTF-8");
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);

        long lastModified = rootP.toFile().lastModified();

        // ???????????????????????????
        if (!StringUtils.isBlank(cacheValue)) {
            ListFilesCacheDTO cacheDTO = objectMapper.readValue(cacheValue, ListFilesCacheDTO.class);
            if (cacheDTO.getLastModify() == lastModified) {
/*
                // ??????????????????????????????
                fileServiceAsync.updateListFilesCache(cacheKey, rootP);
                // ?????????????????????
                return new ResultMsgDTO(true, FileCodes.LIST_FILES_SUCCESS, "List files success.", cacheDTO.getData());
*/
                // ??????????????????????????????????????????
                fileServiceAsync.updateExpires(cacheKey, FileCodes.LIST_FILES_CACHE_TTL, TimeUnit.DAYS);
                return new ResultMsgDTO(true, FileCodes.LIST_FILES_SUCCESS, "List files success.", cacheDTO.getData());

            }
            // ???????????????????????????????????????????????????????????????
        }

        // ?????????????????????????????????????????????

        List<FileDTO> files = fileServiceAsync.synUpdateListFilesCache(cacheKey, rootP);
        ListFilesCacheDTO cacheDTO = new ListFilesCacheDTO(lastModified, files);
        cacheValue = objectMapper.writeValueAsString(cacheDTO);

        // ?????????????????????
        fileServiceAsync.setToRedis(cacheKey, cacheValue, FileCodes.LIST_FILES_CACHE_TTL, TimeUnit.DAYS);

        return new ResultMsgDTO(true, FileCodes.LIST_FILES_SUCCESS, "List files success.", files);
    }
    

    @Override
    public void downloadFile(String filePath, HttpServletResponse response) {
        filePath = rootPath + filePath;

        Path path = Paths.get(filePath);
        if (Files.isDirectory(path)){
            return;
        }

        File file = path.toFile();
        InputStream iStream = null;
        OutputStream oStream = null;
        try {
            //??????????????????
            if (file.exists()) {
                //????????????
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

}
