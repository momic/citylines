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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.citylines.db.line.CarrierLine;
import org.citylines.db.line.CarrierLineDeparture;
import org.citylines.db.location.CurrentLocationParams;
import org.citylines.db.location.LocationParams;
import org.citylines.db.station.Station;
import org.citylines.dialog.date.SelectDateFragment;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
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
    public static final String TABLE_STATION = "Station";
    
    final DateTimeFormatter df = DateTimeFormat.forPattern(INPUT_DATETIME_FORMAT);
    final DateTimeFormatter dfOnDate = DateTimeFormat.forPattern(SelectDateFragment.DATE_TIME_FORMAT);
    final DateTimeFormatter dfOutput = DateTimeFormat.forPattern(OUTPUT_DATETIME_FORMAT);
    
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
    public List<CarrierLine> getAcceptableLines(Long departureCityId, Long destinationCityId, CharSequence date) throws ParseException {
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
        
        String departureStation = null;
        String departureGate = null;
        String arrivalStation = null;
        
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
        } else {
            arrivalTimes.add(sCarrierLineTime);
            arrivalStation = c.getString(c.getColumnIndex("stationName"));
        }

        while (c.moveToNext()) {
            if (!carrierLineId.equals(c.getLong(c.getColumnIndex("carrierLineId")))) {
                Iterator<String> departureTimesIterator = departureTimes.iterator();
                Iterator<String> arrivalTimesIterator = arrivalTimes.iterator();
                while (departureTimesIterator.hasNext()) {
                    timetable.add(new CarrierLineDeparture(departureTimesIterator.next(), 
                            arrivalTimesIterator.next()));
                }
                carrierLines.add(new CarrierLine(carrierLineId, carrierLineName, carrier, 
                        departureStation, arrivalStation, departureGate, contactPhone, timetable));
                
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
                }
            } else {
                arrivalTimes.add(sCarrierLineTime);
                
                if (isDeparture != lastRowIsDeparture) {
                    arrivalStation = c.getString(c.getColumnIndex("stationName"));
                }
            }
        }
        
        Iterator<String> departureTimesIterator = departureTimes.iterator();
        Iterator<String> arrivalTimesIterator = arrivalTimes.iterator();
        while (departureTimesIterator.hasNext()) {
            timetable.add(new CarrierLineDeparture(departureTimesIterator.next(), 
                    arrivalTimesIterator.next()));
        }
        carrierLines.add(
                new CarrierLine(carrierLineId, carrierLineName, 
                        carrier, departureStation, arrivalStation, departureGate, contactPhone, timetable));
        
        return carrierLines;
    }    

    /**
     * Get nearby stations
     * 
     * @param latitude
     * @param longitude
     * @param limit
     * @return 
     */
    public List<Station> getNerbyStations(double latitude, double longitude, String limit) {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                
        String[] sqlSelect = new String[] {
            "T.stationId", "S.name AS stationName", 
            "T.carrierLineId", "CL.name AS carrierLineName", 
            "CD.departureStation", "CA.arrivalStation", 
            "CR.name AS carrierName", 
            "datetime(strftime('%s', T.departureOffset, CLD.departureTime) - strftime('%s', '00:00:00'), 'unixepoch') AS stationDepartureTime",
            "datetime(strftime('%s', T.returnOffset, CLD.returnTime) - strftime('%s', '00:00:00'), 'unixepoch') AS stationReturnTime"
        };
        
        qb.setTables(new StringBuilder(TABLE_TIMETABLE).append(" AS T ")
                .append("INNER JOIN (")
                    // TODO: use longitude and latitude to order this select
                    .append("SELECT S.id, S.name, S.cityId ")
                    .append("FROM Station AS S ")
                    .append("INNER JOIN City AS C ON (C.id = S.cityId AND C.current=1) ")
                    .append("WHERE S.cityLineStation=1 ")
                    .append("LIMIT ").append(limit)
                .append(") AS S ON (S.id = T.stationId) ")
                // TODO: make this as separate query
                .append("INNER JOIN (")
                    .append("SELECT DISTINCT TT.carrierLineId, SS.name AS departureStation ")
                    .append("FROM Timetable AS TT ")
                    .append("INNER JOIN (")
                        .append("SELECT T1.carrierLineId, MIN(T1.departureOffset) AS minDepartureOffset ")
                        .append("FROM Timetable AS T1 ")
                        .append("INNER JOIN Station AS S1 ON (S1.id = T1.stationId) ")
                        .append("INNER JOIN City AS C1 ON (C1.id = S1.cityId AND C1.current=1) ")
                        .append("GROUP BY T1.carrierLineId ")
                    .append(") AS T2 ON (T2.carrierLineId = TT.carrierLineId AND T2.minDepartureOffset = TT.departureOffset) ")
                    .append("INNER JOIN Station AS SS ON (SS.id = TT.stationId) ")
                .append(") AS CD ON (CD.carrierLineId = T.carrierLineId) ")
                .append("INNER JOIN (")
                    .append("SELECT DISTINCT TT.carrierLineId, SS.name AS arrivalStation ")
                    .append("FROM Timetable AS TT ")
                    .append("INNER JOIN (")
                        .append("SELECT T1.carrierLineId, MAX(T1.departureOffset) AS maxDepartureOffset ")
                        .append("FROM Timetable AS T1 ")
                        .append("INNER JOIN Station AS S1 ON (S1.id = T1.stationId) ")
                        .append("INNER JOIN City AS C1 ON (C1.id = S1.cityId AND C1.current=1) ")
                        .append("GROUP BY T1.carrierLineId ")
                    .append(") AS T2 ON (T2.carrierLineId = TT.carrierLineId AND T2.maxDepartureOffset = TT.departureOffset) ")
                .append("INNER JOIN Station AS SS ON (SS.id = TT.stationId) ")
                .append(") AS CA ON (CA.carrierLineId = T.carrierLineId) ")                
                // ------------------------
                .append("INNER JOIN CarrierLineDepartures AS CLD ON (CLD.carrierLineId = T.carrierLineId) ")
                .append("INNER JOIN CarrierLine AS CL ON (CL.id = T.carrierLineId) ")
                .append("INNER JOIN Carrier AS CR ON (CR.id = CL.carrierId) ")
                .toString()
        );

        // calculate start and end time
        DateTime dtStart = new DateTime();
        LocalDate ld = new LocalDate();
        dtStart = dtStart.minus(ld.toDateTimeAtStartOfDay().getMillis());
        DateTime dtEnd = dtStart.plus(60 * 60 * 1000);
        
        Cursor c = qb.query(db, sqlSelect, "stationDepartureTime BETWEEN ? AND ?", 
                // TODO: activate this line after filling database with more data
                // new String[] {dtStart.toString(df.withZoneUTC()), dtEnd.toString(df.withZoneUTC())},
                new String[] {"1970-01-01 05:00:00", "1970-01-01 06:00:00"},
                null, null, "T.stationId, T.carrierLineId, stationDepartureTime");
                
        if ((c.getCount() <= 0)) {
            return null;
        }
        
        c.moveToFirst();
        
        Long stationId = c.getLong(c.getColumnIndex("stationId"));
        String stationName = c.getString(c.getColumnIndex("stationName"));
        
        Long carrierLineId = c.getLong(c.getColumnIndex("carrierLineId"));
        String carrierLineName = c.getString(c.getColumnIndex("carrierLineName"));
        String departureStation = c.getString(c.getColumnIndex("departureStation"));
        String arrivalStation = c.getString(c.getColumnIndex("arrivalStation"));
        String carrierName = c.getString(c.getColumnIndex("carrierName"));
                
        DateTime departureTime = df.parseDateTime(c.getString(c.getColumnIndex("stationDepartureTime")));
        DateTime returnTime = df.parseDateTime(c.getString(c.getColumnIndex("stationReturnTime")));
        
        List<CarrierLine> carrierLines = new ArrayList<CarrierLine>();
        List<CarrierLineDeparture> carrierLineDepartures = new ArrayList<CarrierLineDeparture>();
        carrierLineDepartures.add(new CarrierLineDeparture(departureTime.toString(dfOutput), returnTime.toString(dfOutput)));
        
        List<Station> stations = new ArrayList<Station>();
        while (c.moveToNext()) {
            if (stationId != c.getLong(c.getColumnIndex("stationId"))) {
                carrierLines.add(new CarrierLine(carrierLineId, carrierLineName, 
                        carrierName, departureStation, arrivalStation, 
                        null, null, carrierLineDepartures));
                
                carrierLineDepartures = new ArrayList<CarrierLineDeparture>();
                if (carrierLineId != c.getLong(c.getColumnIndex("carrierLineId"))) {
                    carrierLineId = c.getLong(c.getColumnIndex("carrierLineId"));
                    carrierLineName = c.getString(c.getColumnIndex("carrierLineName"));
                    departureStation = c.getString(c.getColumnIndex("departureStation"));
                    arrivalStation = c.getString(c.getColumnIndex("arrivalStation"));
                    carrierName = c.getString(c.getColumnIndex("carrierName"));
                }
                
                
                stations.add(new Station(stationId, stationName, carrierLines));

                carrierLines = new ArrayList<CarrierLine>();
                stationId = c.getLong(c.getColumnIndex("stationId"));
                stationName = c.getString(c.getColumnIndex("stationName"));
            }
            
            if (carrierLineId != c.getLong(c.getColumnIndex("carrierLineId"))) {                
                carrierLines.add(new CarrierLine(carrierLineId, carrierLineName, carrierName, 
                        departureStation, arrivalStation, 
                        null, null, carrierLineDepartures));
                
                carrierLineDepartures = new ArrayList<CarrierLineDeparture>();
                carrierLineId = c.getLong(c.getColumnIndex("carrierLineId"));
                carrierLineName = c.getString(c.getColumnIndex("carrierLineName"));
                departureStation = c.getString(c.getColumnIndex("departureStation"));
                arrivalStation = c.getString(c.getColumnIndex("arrivalStation"));                
                carrierName = c.getString(c.getColumnIndex("carrierName"));
            }
            
            departureTime = df.parseDateTime(c.getString(c.getColumnIndex("stationDepartureTime")));
            returnTime = df.parseDateTime(c.getString(c.getColumnIndex("stationReturnTime")));
            carrierLineDepartures.add(new CarrierLineDeparture(departureTime.toString(dfOutput), returnTime.toString(dfOutput)));
        }
        
        carrierLines.add(new CarrierLine(carrierLineId, carrierLineName, carrierName, 
                departureStation, arrivalStation, 
                null, null, carrierLineDepartures));
        stations.add(new Station(stationId, stationName, carrierLines));
        
        return stations;
    }
}


