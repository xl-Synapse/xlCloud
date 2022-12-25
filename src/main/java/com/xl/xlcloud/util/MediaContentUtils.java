package com.xl.xlcloud.util;

import java.nio.charset.StandardCharsets;

import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.net.URLEncoder;

/**
 * 文件内容辅助方法集
 */
public final class MediaContentUtils {

    public static String filePath() {
        String osName = System.getProperty("os.name");
        String filePath = "/data/files/";
        if (osName.startsWith("Windows")) {
            filePath = "D:\\" + filePath;
        }
//        else if (osName.startsWith("Linux")) {
//            filePath = MediaContentConstant.FILE_PATH;
//        }
        else if (osName.startsWith("Mac") || osName.startsWith("Linux")) {
            filePath = "/home/admin" + filePath;
        }
        return filePath;
    }

    public static String encode(String fileName) {
        return URLEncoder.DEFAULT.encode(fileName, StandardCharsets.UTF_8);
    }

    public static String decode(String fileName) {
        return URLDecoder.decode(fileName, StandardCharsets.UTF_8);
    }
}