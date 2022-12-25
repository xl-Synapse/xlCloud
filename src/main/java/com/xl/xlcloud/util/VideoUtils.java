package com.xl.xlcloud.util;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.StreamProgress;
import com.xl.xlcloud.util.assist.ContentRange;
import com.xl.xlcloud.util.assist.StreamProgressImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.unit.DataSize;
import ws.schild.jave.process.ProcessWrapper;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class VideoUtils {

    public static boolean convert2Mp4(Path path, Path targetPath) {
        ProcessWrapper ffmpeg = new DefaultFFMPEGLocator().createExecutor();
//        ffmpeg.addArgument("-hwaccel");
//        ffmpeg.addArgument("cuda");

        ffmpeg.addArgument("-i");
        ffmpeg.addArgument(path.toString());
        ffmpeg.addArgument("-c:v");
        ffmpeg.addArgument("h264_nvenc");
        ffmpeg.addArgument("-b:v");
        ffmpeg.addArgument("4M");
//        ffmpeg.addArgument("libx264");


//        ffmpeg.addArgument("-crf");
//        ffmpeg.addArgument("18");

//        ffmpeg.addArgument("-preset");
//        ffmpeg.addArgument("superfast");

        ffmpeg.addArgument("-c:a");
        ffmpeg.addArgument("copy");

        ffmpeg.addArgument("-strict");
        ffmpeg.addArgument("experimental");
        ffmpeg.addArgument(targetPath.toString());

        try {
            ffmpeg.execute();
        } catch (IOException e) {
            System.out.println("can not convert");
            return false;
        }

        BufferedReader br = null;
        try {
//            br = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));
            br = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));
            blockFfmpeg(br);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // 转换成功、
        return true;
    }



    private static void blockFfmpeg(BufferedReader br) throws IOException {
        String line;
        // 该方法阻塞线程，直至合成成功
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }




    private static final int BUFFER_SIZE = (int) DataSize.ofKilobytes(128L).toBytes();

    private static final String BYTES_STRING = "bytes";

    /**
     * 设置请求响应状态、头信息、内容类型与长度 等。
     * <pre>
     * <a href="https://www.rfc-editor.org/rfc/rfc7233">
     *     HTTP/1.1 Range Requests</a>
     * 2. Range Units
     * 4. Responses to a Range Request
     *
     * <a href="https://www.rfc-editor.org/rfc/rfc2616.html">
     *     HTTP/1.1</a>
     * 10.2.7 206 Partial Content
     * 14.5 Accept-Ranges
     * 14.13 Content-Length
     * 14.16 Content-Range
     * 14.17 Content-Type
     * 19.5.1 Content-Disposition
     * 15.5 Content-Disposition Issues
     *
     * <a href="https://www.rfc-editor.org/rfc/rfc2183">
     *     Content-Disposition</a>
     * 2. The Content-Disposition Header Field
     * 2.1 The Inline Disposition Type
     * 2.3 The Filename Parameter
     * </pre>
     *
     * @param response     请求响应对象
     * @param fileName     请求的文件名称
     * @param contentType  内容类型
     * @param contentRange 内容范围对象
     */
    private static void setResponse(
            HttpServletResponse response, String fileName, String contentType,
            ContentRange contentRange) {
        // http状态码要为206：表示获取部分内容
        response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
        // 支持断点续传，获取部分字节内容
        // Accept-Ranges：bytes，表示支持Range请求
        response.setHeader(HttpHeaders.ACCEPT_RANGES, BYTES_STRING);
        // inline表示浏览器直接使用，attachment表示下载，fileName表示下载的文件名
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "inline;filename=" + MediaContentUtils.encode(fileName));
        // Content-Range，格式为：[要下载的开始位置]-[结束位置]/[文件总大小]
        // Content-Range: bytes 0-10/3103，格式为bytes 开始-结束/全部
        response.setHeader(HttpHeaders.CONTENT_RANGE, toContentRange(contentRange));

        response.setContentType(contentType);
        // Content-Length: 11，本次内容的大小
        response.setContentLengthLong(applyAsContentLength(contentRange));
    }

    /**
     * 组装内容范围的响应头。
     * <pre>
     * <a href="https://www.rfc-editor.org/rfc/rfc7233#section-4.2">
     *     4.2. Content-Range - HTTP/1.1 Range Requests</a>
     * Content-Range: "bytes" first-byte-pos "-" last-byte-pos  "/" complete-length
     *
     * For example:
     * Content-Range: bytes 0-499/1234
     * </pre>
     *
     * @param range 内容范围对象
     * @return 内容范围的响应头
     */
    private static String toContentRange(ContentRange range) {
        return BYTES_STRING + ' ' + range.getStart() + '-' + range.getEnd() + '/' + range.getLength();
    }

    /**
     * 计算内容完整的长度/总长度。
     *
     * @param range 内容范围对象
     * @return 内容完整的长度/总长度
     */
    private static long applyAsContentLength(ContentRange range) {
        return range.getEnd() - range.getStart() + 1;
    }

    /**
     * <a href="https://www.jianshu.com/p/08db5ba3bc95">
     *     Spring Boot 处理 HTTP Headers</a>
     */
    public static void download(
            String filePathStr, HttpServletRequest request, HttpServletResponse response,
            HttpHeaders headers)
            throws IOException {
        Path filePath = Paths.get(filePathStr);
        String fileName = filePath.getFileName().toString();
        if (!Files.exists(filePath)) {
            log.warn("file not exist, filePath={}", filePath);
            return;
        }
        long fileLength = Files.size(filePath);
//        long fileLength2 = filePath.toFile().length() - 1;
//        // fileLength=1184856, fileLength2=1184855
//        log.info("fileLength={}, fileLength2={}", fileLength, fileLength2);

        // 开始下载位置
        long firstBytePos;
        // 结束下载位置
        long lastBytePos;
        /*
         * 3.1. Range - HTTP/1.1 Range Requests
         * https://www.rfc-editor.org/rfc/rfc7233#section-3.1
         * Range: "bytes" "=" first-byte-pos "-" [ last-byte-pos ]
         *
         * For example:
         * bytes=0-
         * bytes=0-499
         */
        // Range：告知服务端，客户端下载该文件想要从指定的位置开始下载
        List<HttpRange> httpRanges = headers.getRange();
        if (CollectionUtils.isEmpty(httpRanges)) {
            firstBytePos = 0;
            lastBytePos = fileLength - 1;
        } else {
            HttpRange httpRange = httpRanges.get(0);
            firstBytePos = httpRange.getRangeStart(fileLength);
            lastBytePos = httpRange.getRangeEnd(fileLength);
        }
        ContentRange contentRange = new ContentRange(firstBytePos, lastBytePos, fileLength);
        String range = request.getHeader(HttpHeaders.RANGE);
        // httpRanges=[], range=null
        // httpRanges=[448135688-], range=bytes=448135688-
        log.debug("httpRanges={}, range={}", httpRanges, range);

        // 要下载的长度
        long contentLength = applyAsContentLength(contentRange);
        log.debug("contentRange={}, contentLength={}", contentRange, contentLength);

        // 文件类型
        String contentType = request.getServletContext().getMimeType(fileName);
        // mimeType=video/mp4, CONTENT_TYPE=null
        log.debug("mimeType={}, CONTENT_TYPE={}", contentType, request.getContentType());

        setResponse(response, fileName, contentType, contentRange);

        StopWatch stopWatch = new StopWatch("downloadFile");
        stopWatch.start(fileName);
        try {
            copyByBio(filePath, response, contentRange);
        } finally {
            stopWatch.stop();
            log.info("download file, fileName={}, time={} ms", fileName, stopWatch.getTotalTimeMillis());
        }
    }

    /**
     * 拷贝流，拷贝后关闭流。
     *
     * @param filePath     源文件路径
     * @param response     请求响应
     * @param contentRange 内容范围
     */
    private static void copyByBio(Path filePath, HttpServletResponse response, ContentRange contentRange) {
        String fileName = filePath.getFileName().toString();

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath.toFile(), "r");
            randomAccessFile.seek(contentRange.getStart());

            inputStream = Channels.newInputStream(randomAccessFile.getChannel());
            outputStream = new BufferedOutputStream(response.getOutputStream(), BUFFER_SIZE);

            StreamProgress streamProgress = new StreamProgressImpl(fileName);

            long transmitted = IoUtil.copy(inputStream, outputStream, BUFFER_SIZE, streamProgress);
            log.info("file download complete, fileName={}, transmitted={}", fileName, transmitted);
        } catch (ClientAbortException | IORuntimeException e) {
            // 捕获此异常表示用户停止下载
            log.warn("client stop file download, fileName={}", fileName);
        } catch (Exception e) {
            log.error("file download error, fileName={}", fileName, e);
        } finally {
            IoUtil.close(outputStream);
            IoUtil.close(inputStream);
        }
    }

}
