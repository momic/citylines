package org.citylines.db.line;

/**
 *
 * @author zlaja
 */
public class CarrierLine {
    Long id;
    String name;
    String carrier;
    String station;
    String gate;
    String phone;

    public CarrierLine(Long id, String name, String carrier, String station, String gate, String phone) {
        this.id = id;
        this.name = name;
        this.carrier = carrier;
        this.station = station;
        this.gate = gate;
        this.phone = phone;
    }
}
