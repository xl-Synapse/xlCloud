package com.xl.xlcloud.service;


import com.xl.xlcloud.dto.FileDTO;
import com.xl.xlcloud.dto.PlayRecordDTO;
import com.xl.xlcloud.dto.ResultMsgDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface FileService {

    ResultMsgDTO listFiles(String filePath);

    void downloadFile(String filePath, HttpServletResponse response);

}
