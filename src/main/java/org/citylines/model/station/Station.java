package org.citylines.model.station;

import java.util.List;
import org.citylines.model.line.CarrierLine;

/**
 *
 * @author zlaja
 */
public class Station {
    
    final Long id;
    final String name;
    List<CarrierLine> carrierLines;

    public Station(Long id, String name, List<CarrierLine> carrierLines) {
        this.id = id;
        this.name = name;
        this.carrierLines = carrierLines;
    }
    
}
