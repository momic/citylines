package org.citylines.model.location.factory;

import static org.citylines.model.location.LocationLevel.LOCATION_LEVEL_CITY;
import org.citylines.model.location.CurrentLocationParams;
import org.citylines.model.location.CurrentPositionLocationParams;
import org.citylines.model.location.LocationLevel;
import org.citylines.model.location.LocationParams;
import org.citylines.model.location.ParentLocationParams;
import org.citylines.model.location.SimilarLocationParams;

/**
 *
 * @author zlaja
 */
public class LocationParamsFactory {
    public static LocationParams build(
            LocationParamsType lpType, LocationLevel locationLevel, Object constraint) {
        LocationParams lp = null;
        switch (lpType) {
            case LOCATION_PARAMS_CURRENT_POSITION:
                lp = new CurrentPositionLocationParams(locationLevel, constraint);
                break;
            case LOCATION_PARAMS_CURRENT:
                lp = new CurrentLocationParams(locationLevel, constraint);
                break;
            case LOCATION_PARAMS_PARENT:
                lp = new ParentLocationParams(locationLevel, constraint);
                break;
            case LOCATION_PARAMS_SIMILAR:
                lp = new SimilarLocationParams(locationLevel, constraint);
                break;
        }
        
        return lp;
    }
    
    public static LocationParams build(LocationParamsType lpType, LocationLevel locationLevel) {
        return build(lpType, locationLevel, null);
    }
    
    public static LocationParams build(LocationParamsType lpType) {
        return build(lpType, LOCATION_LEVEL_CITY, null);
    }
}
