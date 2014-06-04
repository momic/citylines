package org.citylines.db.dao.factory;

import android.content.Context;
import org.citylines.db.dao.CarrierLineDAO;
import org.citylines.db.dao.DAO;
import org.citylines.db.dao.LocationDAO;
import org.citylines.db.dao.StationDAO;

/**
 *
 * @author zlaja
 */
public class DAOFactory {
    public static DAO build(DAOType dao, Context context) {
        return DAOFactory.build(dao, context, false);
    }
    
    public static DAO build(DAOType dao, Context context, boolean writeable) {
        DAO result = null;
        switch (dao) {
            case CARRIER_LINE_DAO:
                result = new CarrierLineDAO(context);
                break;
            case LOCATION_DAO:
                result = new LocationDAO(context, writeable);
                break;
            case STATION_DAO:
                result = new StationDAO(context);
                break;
        }
        return result;
    }
}
