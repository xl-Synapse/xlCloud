package com.xl.xlcloud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayRecordDTO {
    private int userId;
    private String fileMd5;
    private int position;
}
