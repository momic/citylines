package org.citylines.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.citylines.db.line.AcceptableLines;
import org.citylines.db.line.CarrierLine;
import org.citylines.db.line.CarrierLineDeparture;
import org.citylines.db.location.CurrentLocationParams;
import org.citylines.db.location.LocationParams;
import org.citylines.dialog.date.SelectDateFragment;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author zlaja
 */
public class DBFactory extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "citylines.db";
    private static final int DATABASE_VERSION = 1;
    
    private static final String INPUT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String OUTPUT_DATETIME_FORMAT = "HH:mm";
    
    public static final String TABLE_STATE = "State";
    public static final String TABLE_MUNICIPALITY = "Municipality";
    public static final String TABLE_CITY = "City";
    
    public static final String TABLE_TIMETABLE = "Timetable";
    
    private static DBFactory instance = null;
    public static DBFactory getInstance(Context context) {
        if(instance == null) {
            instance = new DBFactory(context.getApplicationContext());
        }
        return instance;
    }    

    protected DBFactory(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    /**
     * Get carriers
     * 
     * @return cursor
     */
    public Cursor getCarriers() {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        
        String [] sqlSelect = {"id AS _id", "name"};
        String sqlTables = "Carrier";
        
        qb.setTables(sqlTables);
        Cursor c = qb.query(db, sqlSelect, null, null, null, null, null);
        
        c.moveToFirst();
        return c;
    }    
    
    /**
     * Get location items for spinner
     * 
     * @param lp
     * @return Cursor location items
     */
    public Cursor getLocationItems(LocationParams lp) {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                
        qb.setTables(lp.getSqlTables());
        Cursor c = qb.query(db, lp.getSqlSelect(), lp.getWhereClause(), 
                lp.getWhereArgs(), null, null, lp.getSortOrder());
        
        c.moveToFirst();
        return c;
    }       
    
    /**
     * Sets new current city,
     * if new id is different with current one.
     * 
     * @param cityId new city id
     * @return int rows affected count
     */    
    public int setCurrentCity(long cityId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put("current", 0);
        String whereClause = "current=1 AND id<>?";
        String[] whereArgs = {Long.toString(cityId)};
        int rowsAffected = db.update(TABLE_CITY, args, whereClause, whereArgs);
        
        if (rowsAffected>0) {
            args.clear();
            args.put("current", 1);
            whereClause = "id=?";
            db.update(TABLE_CITY, args, whereClause, whereArgs);
        }
        
        return rowsAffected;
    }
   
    /**
     * Gets `rowId`, `id` and `name` of current location
     * @param lp
     * @return Cursor - current location record
     */
    public Cursor getCurrentLocation(CurrentLocationParams lp) {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String [] sqlSelect = {"L.id", "L.name"};
        if (lp.isWithRowId()) {
            int length = sqlSelect.length;
            sqlSelect = Arrays.copyOf(sqlSelect, length+1);
            sqlSelect[length] = "count(L.id)-1 AS rowId";
        }
        
        StringBuilder sqlTables = new StringBuilder(lp.getSqlTables());
        sqlTables.append(" AS L");
        if (lp.isWithRowId()) {
            // left join section
            sqlTables.append(" LEFT JOIN (SELECT LI.id, LI.name FROM ");
                // inner from section
                sqlTables.append(lp.getSqlTables());
                sqlTables.append(" AS LI ");
            
                // inner join section
                sqlTables.append(lp.getFilterByParent());
            sqlTables.append(") AS L1 ON (L1.name <= L.name) ");
            // inner join section
            sqlTables.append(lp.getSqlTablesAppend());
        }

        String group = lp.isWithRowId() ? "L.id" : null;
        String order = "L.name ASC";

        qb.setTables(sqlTables.toString());
        Cursor c = qb.query(db, sqlSelect, lp.getWhereClause(), 
                null, group, null, order);

        c.moveToFirst();
        return c;
    }
 
    /**
     * Get acceptable lines for selected departure, destination and date
     * 
     * @param departureCityId
     * @param destinationCityId
     * @param date
     * @return
     * @throws ParseException 
     */
    public AcceptableLines getAcceptableLines(Long departureCityId, Long destinationCityId, CharSequence date) throws ParseException {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                
        String[] sqlSelect = new String[] {
            "T.carrierLineId", "CR.name AS carrierName", "CL.name AS carrierLineName",
            "S.cityId", "C.name AS cityName", "C.contactPhone",
            "T.stationId", "S.name AS stationName", 
            "datetime(strftime('%s', T.departureOffset, CLD.departureTime) - strftime('%s', '00:00:00'), 'unixepoch') AS departureTime", 
            "datetime(strftime('%s', T.returnOffset, CLD.returnTime) - strftime('%s', '00:00:00'), 'unixepoch') AS returnTime",
            "T.gate"
        };
        qb.setTables(new StringBuilder(TABLE_TIMETABLE).append(" AS T ")
                .append("INNER JOIN (")
                    .append("SELECT T.carrierLineId, COUNT(DISTINCT S.cityId) AS cityCount, ")
                    .append("MIN(T.departureOffset) AS minOffset, MAX(T.departureOffset) AS maxOffset ")
                    .append("FROM Timetable AS T ")
                    .append("INNER JOIN Station AS S ON (S.id = T.stationId) ")
                    .append("WHERE S.cityId IN (")
                    .append(Long.toString(departureCityId))
                    .append(",")
                    .append(Long.toString(destinationCityId))
                    .append(") GROUP BY T.carrierLineId")
                .append(") AS ACL ON (ACL.carrierLineId = T.carrierLineId AND ACL.cityCount > 1) ")
                .append("INNER JOIN CarrierLineDepartures AS CLD ON (CLD.carrierLineId = T.carrierLineId) ")
                .append("INNER JOIN CarrierLine AS CL ON (CL.id = T.carrierLineId) ")
                .append("INNER JOIN Carrier AS CR ON (CR.id = CL.carrierId) ")
                .append("INNER JOIN Station AS S ON (S.id = T.stationId) ")
                .append("INNER JOIN City AS C ON (C.id = S.cityId)")
                .toString()
        );
        
        Cursor c = qb.query(db, sqlSelect, "S.cityId IN (?,?) AND T.departureOffset IN (ACL.minOffset, ACL.maxOffset)", 
                new String[] {Long.toString(departureCityId), Long.toString(destinationCityId)}, 
                null, null, "T.carrierLineId, T.departureOffset, S.cityId, T.stationId");
        
        if ((c.getCount() <= 0) || (date == null) || ("".equals(date.toString()))) {
            return null;
        }
        
        c.moveToFirst();
        
        final List<String> departureTimes = new LinkedList<String>();
        final List<String> arrivalTimes = new LinkedList<String>();
        final List<CarrierLineDeparture> timetable = new ArrayList<CarrierLineDeparture>();
        final List<CarrierLine> carrierLines = new ArrayList<CarrierLine>();
        final Map<Long, List<CarrierLineDeparture>> carrierLineDepartures = new HashMap<Long, List<CarrierLineDeparture>>();
        
        String departureStation = null;
        String departureGate = null;
        
        final DateTimeFormatter df = DateTimeFormat.forPattern(INPUT_DATETIME_FORMAT);
        final DateTimeFormatter dfOnDate = DateTimeFormat.forPattern(SelectDateFragment.DATE_TIME_FORMAT);
        final DateTimeFormatter dfOutput = DateTimeFormat.forPattern(OUTPUT_DATETIME_FORMAT);

        Long carrierLineId = c.getLong(c.getColumnIndex("carrierLineId"));
        boolean isDeparture = (c.getInt(c.getColumnIndex("cityId")) == departureCityId);
        boolean lastRowIsDeparture;
        String carrier = c.getString(c.getColumnIndex("carrierName"));
        String carrierLineName = c.getString(c.getColumnIndex("carrierLineName"));
        String contactPhone = c.getString(c.getColumnIndex("contactPhone"));
        DateTime onDate =  dfOnDate.withZoneUTC().parseDateTime(date.toString());
        
        String dateTimeColumn = (isDeparture) ? "departureTime" : "returnTime";
        DateTime result =  df.withZoneUTC().parseDateTime(c.getString(c.getColumnIndex(dateTimeColumn)));
        DateTime carrierLineTime = onDate.plus(result.getMillis());
        String sCarrierLineTime = carrierLineTime.toString(dfOutput);
        
        if (isDeparture) {            
            departureTimes.add(sCarrierLineTime);
            
            departureGate = c.getString(c.getColumnIndex("gate"));
            departureStation = c.getString(c.getColumnIndex("stationName"));
            carrierLines.add(new CarrierLine(carrierLineId, carrierLineName, carrier, 
                    departureStation, departureGate, contactPhone));
        } else {
            arrivalTimes.add(sCarrierLineTime);
        }

        while (c.moveToNext()) {
            if (!carrierLineId.equals(c.getLong(c.getColumnIndex("carrierLineId")))) {
                Iterator<String> departureTimesIterator = departureTimes.iterator();
                Iterator<String> arrivalTimesIterator = departureTimes.iterator();
                while (departureTimesIterator.hasNext()) {
                    timetable.add(new CarrierLineDeparture(departureTimesIterator.next(), 
                            arrivalTimesIterator.next()));
                }
                carrierLineDepartures.put(carrierLineId, timetable);
                
                departureTimes.clear();
                arrivalTimes.clear();
                timetable.clear();
                
                carrierLineId = c.getLong(c.getColumnIndex("carrierLineId"));
                carrier = c.getString(c.getColumnIndex("carrierName"));
                carrierLineName = c.getString(c.getColumnIndex("carrierLineName"));
                contactPhone = c.getString(c.getColumnIndex("contactPhone"));                
            }
            
            result =  df.withZoneUTC().parseDateTime(c.getString(c.getColumnIndex(dateTimeColumn)));
            carrierLineTime = onDate.plus(result.getMillis());
            sCarrierLineTime = carrierLineTime.toString(dfOutput);
            lastRowIsDeparture = isDeparture;
            isDeparture = (c.getInt(c.getColumnIndex("cityId")) == departureCityId);
            
            if (isDeparture) {            
                departureTimes.add(sCarrierLineTime);

                if (isDeparture != lastRowIsDeparture) {
                    departureGate = c.getString(c.getColumnIndex("gate"));
                    departureStation = c.getString(c.getColumnIndex("stationName"));
                    carrierLines.add(
                            new CarrierLine(carrierLineId, carrierLineName, 
                                    carrier, departureStation, departureGate, contactPhone));
                }
            } else {
                arrivalTimes.add(sCarrierLineTime);
            }
        }
        
        Iterator<String> departureTimesIterator = departureTimes.iterator();
        Iterator<String> arrivalTimesIterator = arrivalTimes.iterator();
        while (departureTimesIterator.hasNext()) {
            timetable.add(new CarrierLineDeparture(departureTimesIterator.next(), 
                    arrivalTimesIterator.next()));
        }
        carrierLineDepartures.put(carrierLineId, timetable);      
        
        return new AcceptableLines(carrierLines, carrierLineDepartures);
    }    
}


