package org.citylines.db.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import org.citylines.db.DBManager;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author zlaja
 */
public class DAO {
    
    private static final String DB_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String OUTPUT_DATETIME_FORMAT = "HH:mm";    

    public static final DateTimeFormatter DB_DATETIME_FORMATTER = DateTimeFormat.forPattern(DB_DATETIME_FORMAT);
    public static final DateTimeFormatter OUTPUT_DATETIME_FORMATTER = DateTimeFormat.forPattern(OUTPUT_DATETIME_FORMAT);
    
    protected DBManager dbManager;
    
    protected SQLiteDatabase db;
    protected SQLiteQueryBuilder qb;
    protected boolean writeable;

    public DAO(Context context) {
        this(context, false);
    }
    
    public DAO(Context context, boolean writeable) {
        this.dbManager = DBManager.getInstance(context);
        this.qb = new SQLiteQueryBuilder();
        this.writeable = writeable;
        open();
    }
    
    public final void open() throws SQLException {
        this.db = (writeable) ? dbManager.getWritableDatabase() 
                : dbManager.getReadableDatabase();
    }
    
    public void close() {
        // Possibly this shold be replaced with db.close();
        dbManager.close();
    }
    
    public Cursor queryDB(String[] columns, String fromClause, String whereClause, String[] whereParams, String group, String having, String order, String limit) {
        qb.setTables(fromClause);
        
        Cursor c = qb.query(db, columns, whereClause, whereParams, group, having, order, limit);
        c.moveToFirst();
        
        return c;        
    }   
}
        
