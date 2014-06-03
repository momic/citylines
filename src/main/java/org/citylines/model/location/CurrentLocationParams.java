package org.citylines.model.location;

/**
 *
 * @author zlaja
 */
public class CurrentLocationParams extends LocationParams {
    
    private String sqlTablesAppend = "";
    private String filterByParent = "";    
    private Long parentId = null;
    protected boolean withRowId = false;

    public CurrentLocationParams(LocationLevel locationLevel, Object constraint) {
        super(locationLevel, constraint);
        this.prepare(locationLevel, constraint);
    }

    public CurrentLocationParams(LocationLevel locationLevel) {
        this(locationLevel, null);
    }
    

    @Override
    public final void prepare(LocationLevel locationLevel, Object constraint) {
        if (constraint != null) {
            parentId = (Long) constraint;
        }        
        switch (locationLevel) {
            case LOCATION_LEVEL_STATE:
                this.sqlTablesAppend = new StringBuilder("INNER JOIN Municipality AS M ON (M.stateId=L.id) ")
                        .append("INNER JOIN City AS C ON (C.current=1 AND C.municipalityId = M.Id)").toString();
                break;
            case LOCATION_LEVEL_MUNICIPALITY:
                this.sqlTablesAppend = "INNER JOIN City AS C ON (C.current=1 AND C.municipalityId = L.Id)";
                this.filterByParent = (parentId == null) ? 
                        new StringBuilder("INNER JOIN (")
                            .append("SELECT MM.stateId FROM Municipality AS MM ")
                            .append("INNER JOIN City AS C ON (C.current=1 AND C.municipalityId = MM.Id)")
                        .append(") AS MM1 ON (MI.stateId=MM1.stateId)").toString() :
                        "WHERE LI.stateId=" + Long.toString(parentId);
                break;
            case LOCATION_LEVEL_CITY:
                this.filterByParent = (parentId == null) ?
                        "INNER JOIN City AS C ON (C.current=1 AND C.municipalityId = LI.municipalityId)" :
                        "WHERE LI.municipalityId=" + Long.toString(parentId);
                this.setWhereClause("L.current=1");
        }
    }

    public String getSqlTablesAppend() {
        return sqlTablesAppend;
    }

    public String getFilterByParent() {
        return filterByParent;
    }

    public boolean isWithRowId() {
        return withRowId;
    }
}
