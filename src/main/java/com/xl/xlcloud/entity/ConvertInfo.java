package com.xl.xlcloud.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ConvertInfo {
    private Long id;
    private String fileMd5;
    private String convertedPath;
    private LocalDateTime time;
}
