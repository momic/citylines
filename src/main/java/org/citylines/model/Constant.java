package org.citylines.model;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author zlaja
 */
public class Constant {
    public static final String DB_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String OUTPUT_DATETIME_FORMAT = "HH:mm";    
    public static final String INPUT_DATETIME_FORMAT = "dd.MM.yyyy";

    public static final DateTimeFormatter DB_DATETIME_FORMATTER = DateTimeFormat.forPattern(DB_DATETIME_FORMAT);
    public static final DateTimeFormatter OUTPUT_DATETIME_FORMATTER = DateTimeFormat.forPattern(OUTPUT_DATETIME_FORMAT);
    public static final DateTimeFormatter INPUT_DATETIME_FORMATTER = DateTimeFormat.forPattern(INPUT_DATETIME_FORMAT);
}
