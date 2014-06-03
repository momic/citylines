package org.citylines.model.location;

/**
 *
 * @author zlaja
 */
public interface SqlQerySections {
    
    /**
     * Prepare SQL sections
     * 
     * @param locationLevel STATE|MUNICIPALITY|CITY
     * @param constraint Object - parentId or where constraint
     */
    public void prepare(LocationLevel locationLevel, Object constraint);
    
    // Get prepared SQL sections
    public String[] getSqlSelect();
    public String getSqlTables();
    
    public String getWhereClause();
    public String[] getWhereArgs();
    
    public String getSortOrder();
}
