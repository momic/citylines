package org.citylines.db.line;

import java.util.List;

/**
 *
 * @author zlaja
 */
public class CarrierLine {
    private final Long id;
    private final String name;
    private final String carrier;
    private final String departureStation;
    private final String arrivalStation;
    private final String gate;
    private final String phone;
    
    private final List<CarrierLineDeparture> departures;
    
    private final static String DASH = "-";

    public CarrierLine(Long id, String name, String carrier, String departureStation, String arrivalStation, String gate, String phone, List<CarrierLineDeparture> departures) {
        this.id = id;
        this.name = name;
        this.carrier = carrier;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.gate = gate;
        this.phone = phone;
        this.departures = departures;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<CarrierLineDeparture> getDepartures() {
        return departures;
    }

    public String getDepartureStation() {
        return departureStation;
    }

    public String getArrivalStation() {
        return arrivalStation;
    }

    public String getCarrier() {
        return carrier;
    }

    public String getGate() {
        return (gate != null) ? gate : DASH;
    }

    public String getPhone() {
        return (phone != null) ? phone : DASH;
    }
}
