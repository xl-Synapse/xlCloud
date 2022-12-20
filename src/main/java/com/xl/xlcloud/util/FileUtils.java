package com.xl.xlcloud.util;

import com.xl.xlcloud.common.FileCodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {
    public static int getFileType(Path path) {
        if (Files.isDirectory(path))
            return FileCodes.DIRECTORY_TYPE;

        String fileName = path.getFileName().toString().toLowerCase();
        if (
           fileName.endsWith(".jpg") ||
           fileName.endsWith(".png") ||
           fileName.endsWith(".jpeg") ||
           fileName.endsWith(".gif") ||
           fileName.endsWith(".svg") ||
           fileName.endsWith(".bmp") ||
           fileName.endsWith(".webp") ||
           fileName.endsWith(".xmp")
        )  {
            return FileCodes.IMAGE_TYPE;
        }

        if (
            fileName.endsWith(".mp4") ||
            fileName.endsWith(".3gp") ||
            fileName.endsWith(".mkv") ||
            fileName.endsWith(".mov") ||
            fileName.endsWith(".rmvb") ||
            fileName.endsWith(".avi") ||
            fileName.endsWith(".flv") ||
            fileName.endsWith(".vob") ||
            fileName.endsWith(".rm") ||
            fileName.endsWith(".wmv") ||
            fileName.endsWith(".asf") ||
            fileName.endsWith(".asx") ||
            fileName.endsWith(".mpeg") ||
            fileName.endsWith(".mpe")

        ) {
            return FileCodes.VIDEO_TYPE;
        }

        return FileCodes.FILE_TYPE;
    }

    public static String getFastMD5(String path) {
        BigInteger bi = null;
        try {
            int byteLength = 2048;
            byte[] buffer = new byte[byteLength];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");

            RandomAccessFile file = new RandomAccessFile(path, "r");
            // 头
            len = file.read(buffer);
            md.update(buffer, 0, len);

            // 尾、文件大小足以取出不重叠的头尾时、计算尾的 md5、
            if (file.length() >= 2 * byteLength) {
                file.seek(file.length() - byteLength);
                len = file.read(buffer);
                md.update(buffer, 0, len);
            }

            file.close();
            byte[] b = md.digest();
            bi = new BigInteger(1, b);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return bi == null? "" : bi.toString(16);
    }
}
