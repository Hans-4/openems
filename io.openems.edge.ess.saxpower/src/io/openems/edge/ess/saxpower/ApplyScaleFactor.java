package io.openems.edge.ess.saxpower;

import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;

public class ApplyScaleFactor {
    private final Scales scales = new Scales();

    /**
     * Creates a scaling converter for the given data register address.
     *
     * @param dataRegisterAddress the address of the data register
     * @return the ElementToChannelConverter
     */
    public ElementToChannelConverter createScalingConverter(int dataRegisterAddress) {
        return new ElementToChannelConverter(
                val -> this.channelConverter(val, dataRegisterAddress),
                val -> val);
    }

    private Object channelConverter(Object val, int dataRegisterAddress) {
        if (val == null) {
            return null;
        }
        int value = ((Number)val).intValue();

        Integer scaleFactorRegisterAddress = null;
        for (RegisterRange range : this.scales.getEssScaleFactorChannelList()) {
            if (range.contains(dataRegisterAddress)) {
                scaleFactorRegisterAddress = range.getScaleFactorRegisterAddress();
                break;
            }
        }

        if (scaleFactorRegisterAddress != null) {
            int scaleFactor = this.scales.getWellKnownScaleFactorMap().get(scaleFactorRegisterAddress);

            double finalScaleFactor = Math.pow(10, scaleFactor);
            return (value * finalScaleFactor) * -1;
        } else {
            return null;
        }
    }
}