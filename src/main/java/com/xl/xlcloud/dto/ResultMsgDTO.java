package com.xl.xlcloud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultMsgDTO {
    private boolean success;
    private int code;
    private String msg;
    private Object data;

    public static ResultMsgDTO ok(int code){
        return new ResultMsgDTO(true, code, null, null);
    }
    public static ResultMsgDTO ok(int code, Object data){
        return new ResultMsgDTO(true, code, null, data);
    }
    public static ResultMsgDTO ok(int code, List<?> data){
        return new ResultMsgDTO(true, code, null, data);
    }
    public static ResultMsgDTO fail(int code, String errorMsg){
        return new ResultMsgDTO(false, code, errorMsg, null);
    }
}
