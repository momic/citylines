package org.citylines.db.location;

/**
 *
 * @author zlaja
 */
public class SimilarLocationParams extends LocationParams {

    public SimilarLocationParams(LocationLevel locationLevel, Object constraint) {
        super(locationLevel, constraint);
        init(locationLevel);
        this.prepare(locationLevel, constraint);
    }

    public SimilarLocationParams(LocationLevel locationLevel) {
        this(locationLevel, null);
    }
    
    private void init(LocationLevel locationLevel) {
        switch (locationLevel) {
            case LOCATION_LEVEL_STATE: 
                break;
            case LOCATION_LEVEL_MUNICIPALITY:
                setSqlTables(getSqlTables() + 
                        " AS L INNER JOIN State AS S ON (S.id=L.stateId)");
                this.setSqlSelect(new String[] 
                        {"L.id AS _id", "L.name", "S.name AS stateName"});
                break;
            case LOCATION_LEVEL_CITY:
                setSqlTables(getSqlTables() + 
                        " AS L INNER JOIN Municipality AS M ON (M.id=L.municipalityId)");
                this.setSqlSelect(new String[] 
                        {"L.id AS _id", "L.name", "M.name AS municipalityName"});
        }
    }

    @Override
    public final void prepare(LocationLevel locationLevel, Object constraint) {
        this.setWhereClause("L.name LIKE '%' || ? || '%'");
        this.setWhereArgs(new String[] {(String) constraint});
    }
    
}
