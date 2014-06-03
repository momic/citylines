package org.citylines.db.dao;

import static org.citylines.db.DBManager.TABLE_CITY;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import org.citylines.model.location.CurrentLocationParams;
import org.citylines.model.location.LocationParams;
import java.util.Arrays;

/**
 *
 * @author zlaja
 */
public class LocationDAO extends DAO {

    public LocationDAO(Context context, boolean writeable) {
        super(context, writeable);
    }
    /**
     * Get location items for spinner
     * 
     * @param lp
     * @return Cursor location items
     */
    public Cursor getLocationItems(LocationParams lp) {
        return queryDB(lp.getSqlSelect(), lp.getSqlTables(), lp.getWhereClause(), 
                lp.getWhereArgs(), null, null, lp.getSortOrder(), null);
    }       
    
    /**
     * Sets new current city,
     * if new id is different with current one.
     * 
     * @param cityId new city id
     * @return int rows affected count
     */    
    public int setCurrentCity(long cityId) {
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

        
        return queryDB(sqlSelect, sqlTables.toString(), lp.getWhereClause(), 
                null, group, null, order, null);

    }
    
}
