package com.xl.xlcloud.entity;

import lombok.Data;

@Data
public class PlayRecord {
    private Long id;
    private int userId;
    private String fileMd5;
    private int position;

}
