package org.citylines.db.dao;

import android.content.Context;

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
