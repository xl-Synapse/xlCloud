package com.xl.xlcloud.dto;

import lombok.Data;

@Data
public class FileDTO {
    private String path;

    private String fileName;

    private int type; // 0 文件夹 1 文件、

    public FileDTO () {}

    public FileDTO(String path, String fileName, int type) {
        this.path = path;
        this.fileName = fileName;
        this.type = type;
    }
}
