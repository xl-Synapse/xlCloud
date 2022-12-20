package com.xl.xlcloud;

import com.xl.xlcloud.controller.FileController;
import com.xl.xlcloud.entity.PlayRecord;
import com.xl.xlcloud.entity.User;
import com.xl.xlcloud.mapper.PlayRecordMapper;
import com.xl.xlcloud.mapper.UserMapper;
import com.xl.xlcloud.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;

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

    @Test
    void testAsync() throws InterruptedException {
        System.out.println(Thread.currentThread().getName());
        Thread.sleep(2000);
        System.out.println(Thread.currentThread().getName());
    }

    @Test
    void testMD5() {
        String md5 = FileUtils.getFastMD5("file/[KTXP+Team.Kaguya][Kaguya_Love_Is_War_2][OVA][GB_CN][HEVC_opus][720p][DVDRIP].mkv");
        System.out.println(md5);
    }

    @Test
    void testPlayRecordSelect(@Autowired PlayRecordMapper playRecordMapper){
        PlayRecord playRecord = playRecordMapper.getPlayRecordByMd5(2, "3453453gsedrgherhedh");
        System.out.println(playRecord);
    }

    @Test
    void testPlayRecordUpdate(@Autowired PlayRecordMapper playRecordMapper){
        PlayRecord playRecord = new PlayRecord();
        playRecord.setUserId(1);
        playRecord.setFileMd5("hrhgesgsegsegesgtht6456");
        playRecord.setPosition(23);

        playRecordMapper.updatePlayRecord(playRecord);
        System.out.println(playRecord);
    }

    @Test
    void testPlayRecordCreate(@Autowired PlayRecordMapper playRecordMapper){
        PlayRecord playRecord = new PlayRecord();
        playRecord.setUserId(1);
        playRecord.setFileMd5("sgesgsegseg3434534");
        playRecord.setPosition(256563);

        playRecordMapper.createPlayRecord(playRecord);
        System.out.println(playRecord);
    }

    @Test
    void testPlayRecordDelete(@Autowired PlayRecordMapper playRecordMapper){
        PlayRecord playRecord = new PlayRecord();
        playRecord.setUserId(1);
        playRecord.setFileMd5("sgesgsegseg3434534");

        playRecordMapper.deletePlayRecord(playRecord);

        System.out.println(playRecord);
    }

}
