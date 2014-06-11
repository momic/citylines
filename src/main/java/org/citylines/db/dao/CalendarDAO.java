package org.citylines.db.dao;

import android.content.Context;
import android.database.Cursor;
import static org.citylines.db.DBManager.TABLE_HOLIDAY_CALENDAR;
import org.joda.time.LocalDateTime;

/**
 *
 * @author zlaja
 */
public class CalendarDAO extends DAO {

    public CalendarDAO(Context context) {
        super(context);
    }
    
    public Boolean isNationalHoliday(LocalDateTime date) {
        final String fromDateCurrentYear = new StringBuilder("CASE WHEN HC.yearly ")
                .append("THEN date('now','start of year', '+' || strftime('%m', HC.fromDate, '-1 month') || ' month', '+' || strftime('%d', HC.fromDate, '-1 day') || ' day') ")
                .append("ELSE HC.fromDate ")
                .append("END AS fromDateCurrentYear").toString();
        
        final String untilDateCurrentYear = new StringBuilder("CASE WHEN HC.yearly ")
                .append("THEN date('now','start of year', '+' || strftime('%m', HC.untilDate, '-1 month') || ' month', '+' || strftime('%d', HC.untilDate, '-1 day') || ' day') ")
                .append("ELSE HC.untilDate ")
                .append("END AS untilDateCurrentYear").toString();
        
        final String[] columns = new String[] {fromDateCurrentYear, untilDateCurrentYear};
        
        final String fromClause = new StringBuilder(TABLE_HOLIDAY_CALENDAR).append(" AS HC ")
                .append("INNER JOIN Municipality AS M ON (M.stateId = HC.stateId) ")
                .append("INNER JOIN City AS C ON (C.current = 1 AND C.municipalityid = M.id)")
                .toString();
        
        final String whereClause = "date(?) BETWEEN fromDateCurrentYear AND untilDateCurrentYear";

        final String[] whereParams = new String[] {date.toString(DB_DATETIME_FORMATTER)};
        
        
        final Cursor c = queryDB(columns, fromClause, whereClause, whereParams, null, null, null, null);        
        return c.getCount() > 0;
        
    }
    
}
