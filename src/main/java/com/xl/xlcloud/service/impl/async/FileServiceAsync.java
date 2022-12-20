package com.xl.xlcloud.service.impl.async;

import com.xl.xlcloud.common.FileCodes;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Component
public class FileServiceAsync {
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Async
    public void writeDirectLink2Redis(String rootPath) {
        stringRedisTemplate.opsForValue().set(
                FileCodes.FILE_DIRECT_LINK_PREFIX + "/" + (rootPath.equals("") ? "" : rootPath + "/"), "1",
                FileCodes.FILE_DIRECT_LINK_TTL, TimeUnit.MINUTES
        );

        // 检查子目录是否存在、
        if (Files.exists(Paths.get(rootPath + "/sub"))) {
            stringRedisTemplate.opsForValue().set(
                    FileCodes.FILE_DIRECT_LINK_PREFIX + "/" + (rootPath.equals("") ? "sub/" : rootPath + "/sub/"), "1",
                    FileCodes.FILE_DIRECT_LINK_TTL, TimeUnit.MINUTES
            );
        }
    }
}
