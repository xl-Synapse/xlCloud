package com.xl.xlcloud.common;

public class FileCodes {
    public static final int DIRECTORY_TYPE = 0;
    public static final int FILE_TYPE = 1;
    public static final int VIDEO_TYPE = 2;
    public static final int IMAGE_TYPE = 3;

    public static final String FILE_DIRECT_LINK_PREFIX = "xlCloud:file:direct-link:";
    public static final Long FILE_DIRECT_LINK_TTL = 30L;

    public static final String LIST_FILES_CACHE_PREFIX = "xlCloud:file:listFilesCache:";
    public static final Long LIST_FILES_CACHE_TTL = 3L;

    public static final String LIST_FILES_CACHE_CONDITION_PREFIX = "xlCloud:file:listFilesCacheCondition:";
    public static final Long LIST_FILES_CACHE_CONDITION_TTL = 30L;

    public static final int LIST_FILES_SUCCESS = 10001;
    public static final int LIST_FILES_FAIL = 10002;

    public static final int GET_RECORD_SUCCESS = 10101;
    public static final int GET_RECORD_FAIL = 10102;

}
