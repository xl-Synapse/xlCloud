package com.xl.xlcloud.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private int type;
    private String token;
    private int userId;
}
