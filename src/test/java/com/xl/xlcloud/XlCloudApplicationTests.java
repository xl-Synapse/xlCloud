package com.xl.xlcloud;

import com.xl.xlcloud.controller.FileController;
import com.xl.xlcloud.entity.User;
import com.xl.xlcloud.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class XlCloudApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testFileController2listFiles(@Autowired UserMapper userMapper) {
        User user = userMapper.getUserByUsername("xl");
        System.out.println(user);
    }

}
