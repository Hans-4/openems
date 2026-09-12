package io.openems.edge.ess.saxpower;

import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckScaleFactorMap {
    private final Scales scales = new Scales();
    private final Logger log = LoggerFactory.getLogger(CheckScaleFactorMap.class);

    /**
     * Compares well known scale factor with read scale factor.
     * @param address Address of the scale factor
     * @return var
     */
    public ElementToChannelConverter getValue(int address) {
        return new ElementToChannelConverter(
                element -> this.checkFactor(address, element),
                channel -> channel);
    }

    private Object checkFactor(int address, Object val) {
        if (val == null) {
            return null;
        }
        int value = ((Number)val).intValue();

        int wellKnownScaleFactor = this.scales.getWellKnownScaleFactorMap().get(address);

        if (wellKnownScaleFactor != value) {
            this.log.warn("Read scale factor {} does not match well known {}. Please create an issue on the github page", value, wellKnownScaleFactor);
        }

        return val;
    }
}
