package ru.practicum.main_service;

import java.time.format.DateTimeFormatter;

public class MainCommonUtils {
    public static final String DT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern(DT_FORMAT);
    public static final String PAGE_DEFAULT_FROM = "0";
    public static final String PAGE_DEFAULT_SIZE = "10";
    public static final int MIN_LENGTH_ANNOTATION = 20;
    public static final int MAX_LENGTH_ANNOTATION = 2000;
    public static final int MIN_LENGTH_DESCRIPTION = 20;
    public static final int MAX_LENGTH_DESCRIPTION = 7000;
    public static final int MIN_LENGTH_TITLE = 3;
    public static final int MAX_LENGTH_TITLE = 120;
    public static final int MIN_LENGTH_COMMENT = 3;
    public static final int MAX_LENGTH_COMMENT = 7000;
}
