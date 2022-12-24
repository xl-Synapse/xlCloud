package com.xl.xlcloud.mapper;

import com.xl.xlcloud.entity.ConvertInfo;
import com.xl.xlcloud.entity.PlayRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConvertInfoMapper {
    @Select("select * from tb_convert where file_md5=#{fileMd5}")
    ConvertInfo getConvertByMd5(String fileMd5);

    @Insert("insert into tb_convert (file_md5, converted_path, time) values (#{fileMd5}, #{convertedPath}, #{time})")
    int createConvertInfo(ConvertInfo convertInfo);

    @Insert("insert into tb_convert (file_md5, converted_path) values (#{fileMd5}, #{convertedPath})")
    int createConvertInfoWithoutTime(ConvertInfo convertInfo);

    @Update("update tb_convert set time=#{time} where file_md5=#{fileMd5}")
    int updateConvertInfo(ConvertInfo convertInfo);

    @Delete("delete from tb_convert where file_md5=#{fileMd5}")
    int deleteConvertInfo(ConvertInfo convertInfo);

}
