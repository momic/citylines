package org.citylines.model.calendar;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author zlaja
 */
public class DepartureDate {
    
    private static final int MILLIS_IN_SECOND = 1000;
    
    private static final String INPUT_DATETIME_FORMAT = "dd.MM.yyyy";
    public static final DateTimeFormatter INPUT_DATETIME_FORMATTER = DateTimeFormat.forPattern(INPUT_DATETIME_FORMAT);
    
    private final LocalDateTime localDateTime;
    private final Boolean nationalHoliday;
        
    /**
     * 
     * @param localDateTime
     * @param nationalHoliday
     */
    public DepartureDate(LocalDateTime localDateTime, Boolean nationalHoliday) {
        this.localDateTime = localDateTime;
        this.nationalHoliday = nationalHoliday;
    }

    public String toString(DateTimeFormatter dtf) {
        return localDateTime.toString(dtf);
    }

    public String getDateInputString() {
        return toString(INPUT_DATETIME_FORMATTER);
    }
    
    public Boolean isNationalHoliday() {
        return nationalHoliday;
    }

    public String getOffset() {
        return Integer.toString(localDateTime.getMillisOfDay() / MILLIS_IN_SECOND);
    }
    
    public DepartureDate plus(int seconds) {
        return new DepartureDate(localDateTime.plusSeconds(seconds), nationalHoliday);
    }
    
    public LocalDateTime withSecodsOfDay(int seconds) {
        return localDateTime.withMillisOfDay(seconds * MILLIS_IN_SECOND);
    }
}
