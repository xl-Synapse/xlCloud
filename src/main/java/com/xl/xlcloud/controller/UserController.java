package com.xl.xlcloud.controller;

import com.xl.xlcloud.dto.ResultMsgDTO;
import com.xl.xlcloud.entity.User;
import com.xl.xlcloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    UserService userServiceImpl;

    @PostMapping("/user-sign")
    public ResultMsgDTO signIn(@RequestBody User user) {
        // 传上来就应该是 md5 密码、
        return userServiceImpl.signIn(user.getUsername(), user.getPassword());
    }
}
