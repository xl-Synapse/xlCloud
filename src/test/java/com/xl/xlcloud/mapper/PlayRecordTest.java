package com.xl.xlcloud.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class PlayRecordTest {
    @Test
    void testGetMd5sIn(@Autowired PlayRecordMapper playRecordMapper) {
        List<String> fileMd5s = new ArrayList<>();
        fileMd5s.add("34635645675474");
        fileMd5s.add("72de90e59df2e8a114fb3d8f71b683ab");
        fileMd5s.add("8d8167777163936f448f6c4c034ec089");
        fileMd5s.add("c29654fdaa4202c28a7544c3b8579ae8");
        List<String> resultFileMd5s = playRecordMapper.getMd5sIn(1, fileMd5s);
        System.out.println(resultFileMd5s);
    }
}
