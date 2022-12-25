package com.xl.xlcloud.util.assist;

import cn.hutool.core.io.StreamProgress;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据流进度条
 */
@Slf4j
public class StreamProgressImpl implements StreamProgress {

    private final String fileName;

    public StreamProgressImpl(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void start() {
        log.info("start progress {}", fileName);
    }

    @Override
    public void progress(long progressSize) {
        log.debug("progress {}, progressSize={}", fileName, progressSize);
    }

    @Override
    public void finish() {
        log.info("finish progress {}", fileName);
    }
}
