package com.xl.xlcloud.service.impl.async;

import com.xl.xlcloud.entity.ConvertInfo;
import com.xl.xlcloud.mapper.ConvertInfoMapper;
import com.xl.xlcloud.util.VideoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Component
public class VideoServiceAsync {
    @Autowired
    ConvertInfoMapper convertInfoMapper;

    @Value("${root-path}")
    String rootPath;

    @Async
    public void convert2Mp4(Path path, String fileMd5) {
        ConvertInfo convertInfo = null;
        synchronized (VideoServiceAsync.class) {
            // 防止并发下、多用户触发转码、再次查询、
            convertInfo = convertInfoMapper.getConvertByMd5(fileMd5);
            if (convertInfo != null) {
                // 其他用户已经启动转码了、
                return;
            }

            // 需要保证 查询与写入 操作是原子的、因此需要加锁、后期修改为 事务、
            // 写入转换表、
            convertInfo = new ConvertInfo(null, fileMd5, rootPath +".cache/" + fileMd5 + ".mp4", null);
            convertInfoMapper.createConvertInfoWithoutTime(convertInfo);
        }

        // 唯一触发转码、但转码之前需要删除缓存、以腾出空间、


        boolean result = VideoUtils.convert2Mp4(path, Paths.get(convertInfo.getConvertedPath()));
        if (!result) {
            // 删除转换信息、
            convertInfoMapper.deleteConvertInfo(convertInfo);
            return;
        }

        // 修改转换信息、标识转换完毕、
        convertInfo.setTime(LocalDateTime.now());
        System.out.println(convertInfo);
        convertInfoMapper.updateConvertInfo(convertInfo);

    }
}
