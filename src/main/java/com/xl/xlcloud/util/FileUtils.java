package com.xl.xlcloud.util;

import com.xl.xlcloud.common.FileCodes;

import java.nio.file.Files;
import java.nio.file.Path;

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
}
