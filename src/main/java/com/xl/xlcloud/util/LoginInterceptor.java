package com.xl.xlcloud.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xl.xlcloud.common.FileCodes;
import com.xl.xlcloud.common.UserCodes;
import com.xl.xlcloud.dto.ResultMsgDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginInterceptor implements HandlerInterceptor {
    private StringRedisTemplate stringRedisTemplate;
    private Pattern pattern = Pattern.compile("\\/(file|video)\\/(.*)", Pattern.DOTALL);
    private Pattern dirPattern = Pattern.compile("(.*\\/).*", Pattern.DOTALL);

    private String rootPath;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate, String rootPath) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.rootPath = rootPath;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // CORS 预检、放行 OPTIONS 请求、
        if (request.getMethod().equals(HttpMethod.OPTIONS.toString())) {
            return true;
        }

        // ---------------获取 token、
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token) || token.equals("undefined")){
            // 空 token 也有可能是 img 在访问直链、
            // 检查直链、

/*            // 这里这需要解码一次、
            String rootPath = URLDecoder.decode(
                    request.getRequestURI().replace("+", "%2B"),
                    String.valueOf(StandardCharsets.UTF_8)
            );*/



            String uri = request.getRequestURI();
            Matcher matcher = pattern.matcher(uri);
            if (matcher.find() && matcher.groupCount() >= 2){
                String filePath = new String(Base64.decode(matcher.group(2)), StandardCharsets.UTF_8);
                filePath = rootPath + filePath;
                matcher = dirPattern.matcher(filePath);

                if (!matcher.find()) {
                    return false;
                }

                String key = FileCodes.FILE_DIRECT_LINK_PREFIX + matcher.group(1);
                Object result = stringRedisTemplate.opsForValue()
                        .get(key);

                if (result == null)
                    return false;

                // 存在直链、刷新直链有效期、
                stringRedisTemplate.opsForValue().set(key, "1", FileCodes.FILE_DIRECT_LINK_TTL, TimeUnit.MINUTES);
                return true;
            }
            return false;
        }

        // 基于 token 获取 redis 中的 user、
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(UserCodes.LOGIN_TOKEN_PREFIX + token);
        if (userMap.isEmpty()){
            // 可能 token 过期、通知前端更新、
            returnJson(response, new ResultMsgDTO(false, UserCodes.TOKEN_CHECK_FAIL, "Token expire!", null));
            return false;
        }


        // 刷新有效期并放行、
        stringRedisTemplate.expire(UserCodes.LOGIN_TOKEN_PREFIX + token, UserCodes.LOGIN_TOKEN_TTL, TimeUnit.MINUTES);
        return true;
    }


    private void returnJson(HttpServletResponse response, ResultMsgDTO resultMsgDTO) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonStr = objectMapper.writeValueAsString(resultMsgDTO);
            out = response.getWriter();
            out.append(jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
