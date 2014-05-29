package org.citylines.db.location;

/**
 *
 * @author zlaja
 */
public class ParentLocationParams extends LocationParams {

    // constructor
    public ParentLocationParams(LocationLevel locationLevel, Object constraint) {
        super(locationLevel, constraint);
        init();
        this.prepare(locationLevel, constraint);
    }
    
    private void init() {
        setSqlSelect(new String[] {"id AS _id", "name"});
        setSortOrder("name ASC");
    }
    
    @Override
    public final void prepare(LocationLevel locationLevel, Object constraint) {
        if (constraint != null) {
            switch (locationLevel) {
                case LOCATION_LEVEL_STATE: 
                    break;
                case LOCATION_LEVEL_MUNICIPALITY:
                    this.setWhereClause("stateId=?");
                    break;
                case LOCATION_LEVEL_CITY:
                    this.setWhereClause("municipalityId=?");
            }            
            
            Long parentId = (Long) constraint;
            this.setWhereArgs(new String[] {Long.toString(parentId)});
        }
    }
}
