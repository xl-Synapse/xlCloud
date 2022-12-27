package com.xl.xlcloud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListFilesCacheDTO {
    private long lastModify;
    private List<FileDTO> data;
}
