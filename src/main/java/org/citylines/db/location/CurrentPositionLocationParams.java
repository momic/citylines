package org.citylines.db.location;

/**
 *
 * @author zlaja
 */
public class CurrentPositionLocationParams extends CurrentLocationParams {

    public CurrentPositionLocationParams(LocationLevel locationLevel, Object constraint) {
        super(locationLevel, constraint);
        this.withRowId = true;
    }
}
