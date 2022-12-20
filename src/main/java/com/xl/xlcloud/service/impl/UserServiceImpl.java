package com.xl.xlcloud.service.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import com.xl.xlcloud.common.UserCodes;
import com.xl.xlcloud.dto.ResultMsgDTO;
import cn.hutool.core.bean.BeanUtil;

import com.xl.xlcloud.dto.UserDTO;
import com.xl.xlcloud.entity.User;
import com.xl.xlcloud.mapper.UserMapper;
import com.xl.xlcloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public ResultMsgDTO signIn(String username, String password) {
        User user = userMapper.getUserByUsername(username);

        if (user == null) {
            return ResultMsgDTO.fail(UserCodes.LOGIN_FAIL, "Null user.");
        }

        if (!user.getPassword().equals(password)){
            return ResultMsgDTO.fail(UserCodes.LOGIN_FAIL, "Wrong password.");
        }

        String token = UUID.randomUUID().toString();
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        userDTO.setUserId(user.getId());
        userDTO.setToken(token);

        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO,
                new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((key, value) -> value.toString()));

        String tokenKey = UserCodes.LOGIN_TOKEN_PREFIX + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);

        // 注意这里的过期时间是写死的、当用户有操作时、即登录拦截器满足时、需要更新 token 的有效期、
        stringRedisTemplate.expire(tokenKey, UserCodes.LOGIN_TOKEN_TTL, TimeUnit.MINUTES);

        return ResultMsgDTO.ok(UserCodes.LOGIN_SUCCESS, userDTO);
    }
}
