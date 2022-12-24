package com.xl.xlcloud.mapper;

import com.xl.xlcloud.entity.PlayRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PlayRecordMapper {

    @Select("select * from tb_play_record where user_id=#{userId} and file_md5=#{fileMd5}")
    PlayRecord getPlayRecordByMd5(int userId, String fileMd5);

    @Insert("insert into tb_play_record (user_id, file_md5, position) values (#{userId}, #{fileMd5}, #{position})")
    int createPlayRecord(PlayRecord playRecord);

    @Update("update tb_play_record set position=#{position} where user_id=#{userId} and file_md5=#{fileMd5}")
    int updatePlayRecord(PlayRecord playRecord);

    @Delete("delete from tb_play_record where file_md5=#{fileMd5}")
    int deletePlayRecord(PlayRecord playRecord);

    @Select("<script>"
            + "SELECT file_md5 FROM tb_play_record WHERE user_id=#{userId} and file_md5 IN "
            + "<foreach item='item' index='index' collection='fileMd5s' open='(' separator=',' close=')'>"
            + "#{item}"
            + "</foreach>"
            + "</script>")
    List<String> getMd5sIn(int userId, List<String> fileMd5s);
}
