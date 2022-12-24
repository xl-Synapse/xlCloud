package com.xl.xlcloud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileDTO {
    private String path;

    private String fileName;

    private int type; // 0 文件夹 1 文件、

    private String fileMd5;

    public FileDTO () {}

}
