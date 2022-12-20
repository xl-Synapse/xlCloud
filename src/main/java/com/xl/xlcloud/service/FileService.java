package com.xl.xlcloud.service;


import com.xl.xlcloud.dto.FileDTO;
import com.xl.xlcloud.dto.PlayRecordDTO;
import com.xl.xlcloud.dto.ResultMsgDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface FileService {

    ResultMsgDTO listFiles(String rootPath) throws IOException;

//    void downloadFileWithoutAuth(String rootPath, HttpServletRequest request, HttpServletResponse response);

    void downloadFileWithAuth(String filePath, HttpServletResponse response);

    void playVideoWithAuth(String filePath, HttpServletRequest request, HttpServletResponse response);

    ResultMsgDTO getPlayRecord(int userId, String filePath);

    void updatePlayRecord(PlayRecordDTO playRecordDTO);
}
