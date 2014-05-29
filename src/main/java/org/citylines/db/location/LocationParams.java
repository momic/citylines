package org.citylines.db.location;

import org.citylines.db.DBFactory;

/**
 *
 * @author zlaja
 */
public abstract class LocationParams implements SqlQerySections {
    
    // instance vars
    private String[] sqlSelect = null;
    private String sqlTables = null;
    
    private String whereClause = null;
    private String[] whereArgs = null;
    
    private String sortOrder;
    
    private LocationLevel locationLevel;
    private Object constraint;
    
    public LocationParams(LocationLevel locationLevel, Object constraint) {
        this.locationLevel = locationLevel;
        this.constraint = constraint;
        init(locationLevel);
    }
    
    /**
     * 
     * @param locationLevel 
     */
    private void init(LocationLevel locationLevel) {
        switch (locationLevel) {
            case LOCATION_LEVEL_STATE: 
                this.sqlTables = DBFactory.TABLE_STATE;
                break;
            case LOCATION_LEVEL_MUNICIPALITY:
                this.sqlTables = DBFactory.TABLE_MUNICIPALITY;
                break;
            case LOCATION_LEVEL_CITY:
                this.sqlTables = DBFactory.TABLE_CITY;
        }
    }    

    //
    // Setters
    //
    
    public void setConstraint(Object constraint) {
        this.constraint = constraint;
        this.prepare(this.locationLevel, constraint);
    }
    
    public void setLocationLevel(LocationLevel locationLevel) {
        this.locationLevel = locationLevel;
        this.init(locationLevel);
        this.prepare(locationLevel, this.constraint);
    }
    
    public void setWhereArgs(String[] whereArgs) {
        this.whereArgs = whereArgs;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public void setSqlSelect(String[] sqlSelect) {
        this.sqlSelect = sqlSelect;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setSqlTables(String sqlTables) {
        this.sqlTables = sqlTables;
    }    
    
    
    //
    // Getters
    //    

    @Override
    public String getSqlTables() {
        return sqlTables;
    }
    
    @Override
    public String getWhereClause() {
        return whereClause;
    }

    @Override
    public String[] getWhereArgs() {
        return whereArgs;
    }

    @Override
    public String[] getSqlSelect() {
        return sqlSelect;
    }

    @Override
    public String getSortOrder() {
        return sortOrder;
    }
}
