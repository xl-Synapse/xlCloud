package com.xl.xlcloud.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xl.xlcloud.dto.FileDTO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;

@SpringBootTest
public class FileServiceTest {

    @Resource
    StringRedisTemplate stringRedisTemplate;




    @Test
    public void testRedisAndMd5() throws JsonProcessingException {
        FileDTO fileDTO = new FileDTO("segg", "gvesgsg", 23, "456546547645747geg");

//        stringRedisTemplate.opsForValue().get("xl");

    }

    @Test
    public void testLastModify() {
        File file = new File("file");
        System.out.println("最后修改时间：" + new Date(file.lastModified()));
    }
}
