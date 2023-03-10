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
//import ws.schild.jave.process.ProcessWrapper;
//import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

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
        return false;
    }

/*    public static boolean convert2Mp4(Path path, Path targetPath) {
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

        // ???????????????
        return true;
    }*/



    private static void blockFfmpeg(BufferedReader br) throws IOException {
        String line;
        // ??????????????????????????????????????????
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }




    private static final int BUFFER_SIZE = (int) DataSize.ofKilobytes(128L).toBytes();

    private static final String BYTES_STRING = "bytes";

    /**
     * ???????????????????????????????????????????????????????????? ??????
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
     * @param response     ??????????????????
     * @param fileName     ?????????????????????
     * @param contentType  ????????????
     * @param contentRange ??????????????????
     */
    private static void setResponse(
            HttpServletResponse response, String fileName, String contentType,
            ContentRange contentRange) {
        // http???????????????206???????????????????????????
        response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
        // ?????????????????????????????????????????????
        // Accept-Ranges???bytes???????????????Range??????
        response.setHeader(HttpHeaders.ACCEPT_RANGES, BYTES_STRING);
        // inline??????????????????????????????attachment???????????????fileName????????????????????????
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "inline;filename=" + MediaContentUtils.encode(fileName));
        // Content-Range???????????????[????????????????????????]-[????????????]/[???????????????]
        // Content-Range: bytes 0-10/3103????????????bytes ??????-??????/??????
        response.setHeader(HttpHeaders.CONTENT_RANGE, toContentRange(contentRange));

        response.setContentType(contentType);
        // Content-Length: 11????????????????????????
        response.setContentLengthLong(applyAsContentLength(contentRange));
    }

    /**
     * ?????????????????????????????????
     * <pre>
     * <a href="https://www.rfc-editor.org/rfc/rfc7233#section-4.2">
     *     4.2. Content-Range - HTTP/1.1 Range Requests</a>
     * Content-Range: "bytes" first-byte-pos "-" last-byte-pos  "/" complete-length
     *
     * For example:
     * Content-Range: bytes 0-499/1234
     * </pre>
     *
     * @param range ??????????????????
     * @return ????????????????????????
     */
    private static String toContentRange(ContentRange range) {
        return BYTES_STRING + ' ' + range.getStart() + '-' + range.getEnd() + '/' + range.getLength();
    }

    /**
     * ???????????????????????????/????????????
     *
     * @param range ??????????????????
     * @return ?????????????????????/?????????
     */
    private static long applyAsContentLength(ContentRange range) {
        return range.getEnd() - range.getStart() + 1;
    }

    /**
     * <a href="https://www.jianshu.com/p/08db5ba3bc95">
     *     Spring Boot ?????? HTTP Headers</a>
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

        // ??????????????????
        long firstBytePos;
        // ??????????????????
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
        // Range?????????????????????????????????????????????????????????????????????????????????
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

        // ??????????????????
        long contentLength = applyAsContentLength(contentRange);
        log.debug("contentRange={}, contentLength={}", contentRange, contentLength);

        // ????????????
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
     * ?????????????????????????????????
     *
     * @param filePath     ???????????????
     * @param response     ????????????
     * @param contentRange ????????????
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
            // ???????????????????????????????????????
            log.warn("client stop file download, fileName={}", fileName);
        } catch (Exception e) {
            log.error("file download error, fileName={}", fileName, e);
        } finally {
            IoUtil.close(outputStream);
            IoUtil.close(inputStream);
        }
    }

}
