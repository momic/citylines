package org.citylines.model.location;

/**
 *
 * @author zlaja
 */
public enum LocationLevel {

    LOCATION_LEVEL_STATE("state"),
    LOCATION_LEVEL_MUNICIPALITY("municipality"),
    LOCATION_LEVEL_CITY("city");

    private final String id;

    LocationLevel(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public LocationLevel getNext() {
        return this.ordinal() < LocationLevel.values().length - 1
         ? LocationLevel.values()[this.ordinal() + 1]
         : null;
    }        
}