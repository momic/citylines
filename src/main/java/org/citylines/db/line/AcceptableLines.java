package org.citylines.db.line;

import java.util.List;
import java.util.Map;

/**
 *
 * @author zlaja
 */
public class AcceptableLines {
    
    private final List<CarrierLine> carrierLines;
    private final Map<Long, List<CarrierLineDeparture>> carrierLineDepartures;

    public AcceptableLines(List<CarrierLine> carrierLines, Map<Long, List<CarrierLineDeparture>> carrierLineDepartures) {
        this.carrierLines = carrierLines;
        this.carrierLineDepartures = carrierLineDepartures;
    }

    public List<CarrierLine> getCarrierLines() {
        return carrierLines;
    }

    public Map<Long, List<CarrierLineDeparture>> getCarrierLineDepartures() {
        return carrierLineDepartures;
    }
    
    
    
}
