package com.xl.xlcloud.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import ws.schild.jave.process.ProcessWrapper;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;


public class VideoUtils {

    public static boolean convert2Mp4(Path path, Path targetPath) {
        ProcessWrapper ffmpeg = new DefaultFFMPEGLocator().createExecutor();
//        ffmpeg.addArgument("-hwaccel");
//        ffmpeg.addArgument("cuda");

        ffmpeg.addArgument("-i");
        ffmpeg.addArgument(path.toString());
        ffmpeg.addArgument("-c:v");
        ffmpeg.addArgument("h264_nvenc");
        ffmpeg.addArgument("-b:v");
        ffmpeg.addArgument("4M");
//        ffmpeg.addArgument("libx264");


//        ffmpeg.addArgument("-crf");
//        ffmpeg.addArgument("18");

//        ffmpeg.addArgument("-preset");
//        ffmpeg.addArgument("superfast");

        ffmpeg.addArgument("-c:a");
        ffmpeg.addArgument("copy");

        ffmpeg.addArgument("-strict");
        ffmpeg.addArgument("experimental");
        ffmpeg.addArgument(targetPath.toString());

        try {
            ffmpeg.execute();
        } catch (IOException e) {
            System.out.println("can not convert");
            return false;
        }

        BufferedReader br = null;
        try {
//            br = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));
            br = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));
            blockFfmpeg(br);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // 转换成功、
        return true;
    }



    private static void blockFfmpeg(BufferedReader br) throws IOException {
        String line;
        // 该方法阻塞线程，直至合成成功
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }

}
