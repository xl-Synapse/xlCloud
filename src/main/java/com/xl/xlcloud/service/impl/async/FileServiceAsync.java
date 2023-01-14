package com.xl.xlcloud.service.impl.async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xl.xlcloud.common.FileCodes;
import com.xl.xlcloud.dto.FileDTO;
import com.xl.xlcloud.dto.ListFilesCacheDTO;
import com.xl.xlcloud.dto.ResultMsgDTO;
import com.xl.xlcloud.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class FileServiceAsync {
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Value("${root-path}")
    String rootPath;

    @Async
    public void writeDirectLink2Redis(String nowPath) {
        if (!nowPath.endsWith("/")) {
            nowPath += "/";
        }

        stringRedisTemplate.opsForValue().set(
                FileCodes.FILE_DIRECT_LINK_PREFIX + nowPath, "1",
                FileCodes.FILE_DIRECT_LINK_TTL, TimeUnit.MINUTES
        );

        // 检查子目录是否存在、
        if (Files.exists(Paths.get(nowPath + "/sub"))) {
            stringRedisTemplate.opsForValue().set(
                    FileCodes.FILE_DIRECT_LINK_PREFIX + nowPath + "/sub/", "1",
                    FileCodes.FILE_DIRECT_LINK_TTL, TimeUnit.MINUTES
            );
        }
    }

    @Async
    public void updateListFilesCache(String cacheKey, Path nowPath) throws JsonProcessingException {
        List<FileDTO> files = synUpdateListFilesCache(cacheKey, nowPath);
        ObjectMapper objectMapper = new ObjectMapper();
        ListFilesCacheDTO cacheDTO = new ListFilesCacheDTO(nowPath.toFile().lastModified(), files);
        String cacheValue = objectMapper.writeValueAsString(cacheDTO);

        // 同类调用异步失效、但是该方法本来就是异步的、
        setToRedis(cacheKey, cacheValue, FileCodes.LIST_FILES_CACHE_TTL, TimeUnit.DAYS);
    }

    @Async
    public void updateExpires(String key, long time, TimeUnit unit) {
        stringRedisTemplate.expire(key, time, unit);
    }

    @Async
    public void setToRedis(String key, String value, long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, time, unit);
    }

    public List<FileDTO> synUpdateListFilesCache(String cacheKey, Path nowPath) throws JsonProcessingException {
        List<FileDTO> files = null;
        try {
            files = Files.list(nowPath)
                    .map(
                            path -> {
                                return new FileDTO(path.toString().substring(rootPath.length()).replace("\\", "/"), path.getFileName().toString(), FileUtils.getFileType(path), FileUtils.getFastMD5(path));
                            }
                    )
                    .collect(Collectors.toList());

        } catch (IOException e) {
            return new ArrayList<>();
        }

//        ObjectMapper objectMapper = new ObjectMapper();
//        ListFilesCacheDTO cacheDTO = new ListFilesCacheDTO(nowPath.toFile().lastModified(), files);
//        String cacheValue = objectMapper.writeValueAsString(cacheDTO);
//        stringRedisTemplate.opsForValue().set(cacheKey, cacheValue, FileCodes.LIST_FILES_CACHE_TTL, TimeUnit.DAYS);
        return files;
    }
}
