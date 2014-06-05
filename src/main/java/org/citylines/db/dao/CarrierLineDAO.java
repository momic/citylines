package org.citylines.db.dao;

import android.content.Context;
import android.database.Cursor;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import static org.citylines.db.DBManager.TABLE_TIMETABLE;
import org.citylines.model.line.CarrierLine;
import org.citylines.model.line.CarrierLineDeparture;
import org.joda.time.DateTime;

/**
 *
 * @author zlaja
 */
public class CarrierLineDAO extends DAO {

    public CarrierLineDAO(Context context) {
        super(context);
    }

    /**
     * Get potential lines
     * 
     * @param departureCityId
     * @param destinationCityId
     * @param date
     * @return Cursor
     */
    private Cursor fetchAcceptableLines(Long departureCityId, Long destinationCityId) {
        String[] columns = new String[] {
            "T.carrierLineId", "CR.name AS carrierName", "CL.name AS carrierLineName",
            "S.cityId", "C.name AS cityName", "C.contactPhone",
            "T.stationId", "S.name AS stationName", 
            "datetime(strftime('%s', T.departureOffset, CLD.departureTime) - strftime('%s', '00:00:00'), 'unixepoch') AS departureTime", 
            "datetime(strftime('%s', T.returnOffset, CLD.returnTime) - strftime('%s', '00:00:00'), 'unixepoch') AS returnTime",
            "T.gate"
        };
        
        String fromClause = new StringBuilder(TABLE_TIMETABLE).append(" AS T ")
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
                .toString();
        
        return queryDB(columns, fromClause, "S.cityId IN (?,?) AND T.departureOffset IN (ACL.minOffset, ACL.maxOffset)", 
                new String[] {Long.toString(departureCityId), Long.toString(destinationCityId)}, 
                null, null, "T.carrierLineId, T.departureOffset, S.cityId, T.stationId", null);
    }
    
    private List<CarrierLine> prepareAcceptableLines(Cursor c, Long departureCityId, DateTime date) {
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
        
        String dateTimeColumn = (isDeparture) ? "departureTime" : "returnTime";
        DateTime result =  DB_DATETIME_FORMATTER.withZoneUTC().parseDateTime(c.getString(c.getColumnIndex(dateTimeColumn)));
        DateTime carrierLineTime = date.plus(result.getMillis());
        String sCarrierLineTime = carrierLineTime.toString(OUTPUT_DATETIME_FORMATTER);
        
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
            
            result = DB_DATETIME_FORMATTER.withZoneUTC().parseDateTime(c.getString(c.getColumnIndex(dateTimeColumn)));
            carrierLineTime = date.plus(result.getMillis());
            sCarrierLineTime = carrierLineTime.toString(OUTPUT_DATETIME_FORMATTER);
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
                new CarrierLine(carrierLineId, carrierLineName, carrier, 
                        departureStation, arrivalStation, departureGate, contactPhone, timetable));
        
        return carrierLines;        
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
    public List<CarrierLine> getAcceptableLines(Long departureCityId, Long destinationCityId, DateTime date) throws ParseException {               
        // fetch acceptable lines
        Cursor c = fetchAcceptableLines(departureCityId, destinationCityId);
        
        // validate parameters and fetched data 
        if ((c.getCount() <= 0) || (date == null) || ("".equals(date.toString()))) {
            return null;
        }
        
        // return prepared data
        return prepareAcceptableLines(c, departureCityId, date);
    }        
}
