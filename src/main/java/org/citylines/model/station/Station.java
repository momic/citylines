package org.citylines.model.station;

import java.text.DecimalFormat;
import java.util.List;
import org.citylines.model.line.CarrierLine;

/**
 *
 * @author zlaja
 */
public class Station {
    
    private final Long id;
    private final String name;
    private final Double departureStationDistance;
    private final Double returnStationDistance;    
    private final List<CarrierLine> carrierLines;
    
    private final static String DASH = "-";
    private final static int EARTH_RADIUS = 6371;
    private final DecimalFormat df = new DecimalFormat("#.###");
    

    public Station(Long id, String name, 
            Double departureStationDistance, Double returnStationDistance, 
            List<CarrierLine> carrierLines) {
        this.id = id;
        this.name = name;
        this.departureStationDistance = departureStationDistance;
        this.returnStationDistance = returnStationDistance;        
        this.carrierLines = carrierLines;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<CarrierLine> getCarrierLines() {
        return carrierLines;
    }
    
    public String getDepartureStationDistance() {
        return (departureStationDistance != null) ? df.format(Math.acos(departureStationDistance) * EARTH_RADIUS) : DASH;
    }
    
    public String getReturnStationDistance() {
        return (returnStationDistance != null) ? df.format(Math.acos(returnStationDistance) * EARTH_RADIUS) : DASH;
    }    
}
