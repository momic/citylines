package org.citylines.db.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import org.citylines.db.DBManager;

/**
 *
 * @author zlaja
 */
public class DAO {
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
        
