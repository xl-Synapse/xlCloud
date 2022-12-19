package com.xl.xlcloud.mapper;

import com.xl.xlcloud.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("select * from tb_user where username=#{username}")
    User getUserByUsername(String username);
}
