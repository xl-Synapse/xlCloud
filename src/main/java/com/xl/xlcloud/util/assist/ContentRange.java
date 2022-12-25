package com.xl.xlcloud.util.assist;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ContentRange {
    /**
     * 第一个字节的位置
     */
    private final long start;
    /**
     * 最后一个字节的位置
     */
    private long end;
    /**
     * 内容完整的长度/总长度
     */
    private final long length;

    /**
     * Validate range.
     *
     * @return true if the range is valid, otherwise false
     */
    public boolean validate() {
        if (end >= length) {
            end = length - 1;
        }
        return (start >= 0) && (end >= 0) && (start <= end) && (length > 0);
    }

    @Override
    public String toString() {
        return "firstBytePos=" + start +
                ", lastBytePos=" + end +
                ", fileLength=" + length;
    }
}