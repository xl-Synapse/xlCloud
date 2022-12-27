package com.xl.xlcloud.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.xl.xlcloud.dto.FileDTO;
import com.xl.xlcloud.dto.PlayRecordDTO;
import com.xl.xlcloud.dto.ResultMsgDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public interface FileService {

    ResultMsgDTO listFiles(String filePath) throws UnsupportedEncodingException, JsonProcessingException;

    void downloadFile(String filePath, HttpServletResponse response);

}
