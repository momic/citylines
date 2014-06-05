package org.citylines.db.dao;

import static org.citylines.db.DBManager.TABLE_TIMETABLE;
import org.citylines.model.station.Station;
import org.citylines.model.line.CarrierLine;
import org.citylines.model.line.CarrierLineDeparture;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import android.content.Context;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zlaja
 */
public class StationDAO extends DAO {
    
    private static final int MILIS_IN_HOUR = 60 * 60 * 1000;

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    
    public StationDAO(Context context) {
        super(context);
    }
    
    private Cursor fetchNearbyStations(double latitude, double longitude, String limit) {
        String[] sqlSelect = new String[] {
            "T.stationId", "S.name AS stationName", 
            "S.departureStationDistance", "S.returnStationDistance", 
            "T.carrierLineId", "CL.name AS carrierLineName", 
            "CD.departureStation", "CA.arrivalStation", 
            "CR.name AS carrierName", 
            "datetime(strftime('%s', T.departureOffset, CLD.departureTime) - strftime('%s', '00:00:00'), 'unixepoch') AS stationDepartureTime",
            "datetime(strftime('%s', T.returnOffset, CLD.returnTime) - strftime('%s', '00:00:00'), 'unixepoch') AS stationReturnTime"
        };
        
        // user location
        final double coslat = Math.cos(deg2rad(latitude));
        final double sinlat = Math.sin(deg2rad(latitude));
        final double coslng = Math.cos(deg2rad(longitude));
        final double sinlng = Math.sin(deg2rad(longitude));
        
        String fromClause = new StringBuilder(TABLE_TIMETABLE).append(" AS T ")
                .append("INNER JOIN (")
                    // Calculate partial distance using cos and sin columns
                    // and order query using partial distance
                    // because SQLite does not support trig functions
                    .append("SELECT S.id, S.name, ")
                    .append("S.sinDepLat * ").append(sinlat).append(" + S.cosDepLat * ").append(coslat)
                    .append(" * (").append(coslng).append(" * S.cosDepLong + ").append(sinlng)
                    .append(" * S.sinDepLong) AS departureStationDistance, ")
                
                    .append("S.sinRetLat * ").append(sinlat).append(" + S.cosRetLat * ").append(coslat)
                    .append(" * (").append(coslng).append(" * S.cosRetLong + ").append(sinlng)
                    .append(" * S.sinRetLong) AS returnStationDistance ")
                
                    .append("FROM Station AS S ")
                    .append("INNER JOIN City AS C ON (C.id = S.cityId AND C.current=1) ")
                    .append("WHERE S.cityLineStation=1 ")
                    .append("ORDER BY departureStationDistance DESC ")
                    .append("LIMIT ").append(limit)
                .append(") AS S ON (S.id = T.stationId) ")
                // TODO: Gain performance by adding redundant fields to CarrierLineDepartures
                // DepartureStationID, DepartureTime, ArrivalStationID, ArrivalTime
                // and removing these two joins
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
                .toString();

        // calculate start and end time
        DateTime dtStart = new DateTime();
        LocalDate ld = new LocalDate();
        dtStart = dtStart.minus(ld.toDateTimeAtStartOfDay().getMillis());
        DateTime dtEnd = dtStart.plus(MILIS_IN_HOUR);
        
        return queryDB(sqlSelect, fromClause, "stationDepartureTime BETWEEN ? AND ?", 
                new String[] {dtStart.toString(DB_DATETIME_FORMATTER.withZoneUTC()), 
                                dtEnd.toString(DB_DATETIME_FORMATTER.withZoneUTC())},
                null, null, "S.departureStationDistance DESC, T.stationId, T.carrierLineId, stationDepartureTime", null);        
    }
    
    private List<Station> prepareNearbyStations(Cursor c) {
        Long stationId = c.getLong(c.getColumnIndex("stationId"));
        String stationName = c.getString(c.getColumnIndex("stationName"));
        
        Double departureStationDistance = null;
        if (!c.isNull(c.getColumnIndex("departureStationDistance"))) {
            departureStationDistance = c.getDouble(c.getColumnIndex("departureStationDistance"));
        }
        
        Double returnStationDistance = null;
        if (!c.isNull(c.getColumnIndex("returnStationDistance"))) {
            returnStationDistance = c.getDouble(c.getColumnIndex("returnStationDistance"));
        }
        
        Long carrierLineId = c.getLong(c.getColumnIndex("carrierLineId"));
        String carrierLineName = c.getString(c.getColumnIndex("carrierLineName"));
        String departureStation = c.getString(c.getColumnIndex("departureStation"));
        String arrivalStation = c.getString(c.getColumnIndex("arrivalStation"));
        String carrierName = c.getString(c.getColumnIndex("carrierName"));
                
        DateTime departureTime = DB_DATETIME_FORMATTER.parseDateTime(c.getString(c.getColumnIndex("stationDepartureTime")));
        DateTime returnTime = DB_DATETIME_FORMATTER.parseDateTime(c.getString(c.getColumnIndex("stationReturnTime")));
        
        List<CarrierLine> carrierLines = new ArrayList<CarrierLine>();
        List<CarrierLineDeparture> carrierLineDepartures = new ArrayList<CarrierLineDeparture>();
        carrierLineDepartures.add(new CarrierLineDeparture(departureTime.toString(OUTPUT_DATETIME_FORMATTER), returnTime.toString(OUTPUT_DATETIME_FORMATTER)));
        
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
                
                
                stations.add(new Station(stationId, stationName, 
                        departureStationDistance, returnStationDistance, carrierLines));

                carrierLines = new ArrayList<CarrierLine>();
                stationId = c.getLong(c.getColumnIndex("stationId"));
                stationName = c.getString(c.getColumnIndex("stationName"));
                
                if (!c.isNull(c.getColumnIndex("departureStationDistance"))) {
                    departureStationDistance = c.getDouble(c.getColumnIndex("departureStationDistance"));
                } else {
                    departureStationDistance = null;
                }

                if (!c.isNull(c.getColumnIndex("returnStationDistance"))) {
                    returnStationDistance = c.getDouble(c.getColumnIndex("returnStationDistance"));
                } else {
                    returnStationDistance = null;
                }
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
            
            departureTime = DB_DATETIME_FORMATTER.parseDateTime(c.getString(c.getColumnIndex("stationDepartureTime")));
            returnTime = DB_DATETIME_FORMATTER.parseDateTime(c.getString(c.getColumnIndex("stationReturnTime")));
            carrierLineDepartures.add(new CarrierLineDeparture(departureTime.toString(OUTPUT_DATETIME_FORMATTER), returnTime.toString(OUTPUT_DATETIME_FORMATTER)));
        }
        
        carrierLines.add(new CarrierLine(carrierLineId, carrierLineName, carrierName, 
                departureStation, arrivalStation, 
                null, null, carrierLineDepartures));
        stations.add(new Station(stationId, stationName, 
                departureStationDistance, returnStationDistance, carrierLines));
        
        return stations;
        
    }
    
    /**
     * Get nearby stations
     * 
     * @param latitude - device latitude
     * @param longitude - device longitude
     * @param limit - stations limit to fetch
     * @return List<Station> - list of stations
     */
    public List<Station> getNerbyStations(double latitude, double longitude, String limit) {
        // fech nearby stations
        Cursor c = fetchNearbyStations(latitude, longitude, limit);
        
        // check resultset
        if ((c.getCount() <= 0)) {
            return null;
        }
        
        // prepare data
        return prepareNearbyStations(c);
    }
}
