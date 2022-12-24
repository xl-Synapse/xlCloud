package com.xl.xlcloud.service;

import com.xl.xlcloud.dto.PlayRecordDTO;
import com.xl.xlcloud.dto.ResultMsgDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface VideoService {
    void playVideoWithAuth(String filePath, HttpServletRequest request, HttpServletResponse response);

    ResultMsgDTO getPlayedVideo(int userId, List<String> fileMd5s);

    ResultMsgDTO getConvertInfo(String filePath);

    ResultMsgDTO getPlayRecord(int userId, String filePath);

    void updatePlayRecord(PlayRecordDTO playRecordDTO);
}
