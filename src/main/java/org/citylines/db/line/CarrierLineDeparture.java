package org.citylines.db.line;

/**
 *
 * @author zlaja
 */
public class CarrierLineDeparture {
    // time of departure from CarrierLine departure station
    // or departure time from carrierLine station for city lines
    private final String departureTime;    
    // time of arrival at CarrierLine destination station
    // or return time to carrierLine station for city lines
    private final String arrivalTime;

    public CarrierLineDeparture(String departureTime, String arrivalTime) {
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }
}
