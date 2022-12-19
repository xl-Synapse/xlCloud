package com.xl.xlcloud.service;

import com.xl.xlcloud.dto.ResultMsgDTO;

public interface UserService {

    ResultMsgDTO signIn(String username, String password);
}
