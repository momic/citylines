package org.citylines.db;

import android.content.Context;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 *
 * @author zlaja
 */
public class DBManager extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "citylines.db";
    private static final int DATABASE_VERSION = 1;
    
    public static final String TABLE_STATE = "State";
    public static final String TABLE_MUNICIPALITY = "Municipality";
    public static final String TABLE_CITY = "City";
    
    public static final String TABLE_TIMETABLE = "Timetable";
    public static final String TABLE_STATION = "Station";
    
    public static final String TABLE_HOLIDAY_CALENDAR = "HolidayCalendar";
    
    private static DBManager instance = null;
    public static DBManager getInstance(Context context) {
        if(instance == null) {
            instance = new DBManager(context.getApplicationContext());
        }
        return instance;
    }    

    private DBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}


