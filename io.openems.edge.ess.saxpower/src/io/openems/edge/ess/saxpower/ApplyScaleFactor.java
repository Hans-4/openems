package io.openems.edge.ess.saxpower;

import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;

public class ApplyScaleFactor {
    private final Scales scales = new Scales();

    /**
     * Creates a scaling converter for the given data register address.
     *
     * @param dataRegisterAddress the address of the data register
     * @param multiplier the number the value gets multiplied with
     * @return the ElementToChannelConverter
     */
    public ElementToChannelConverter createScalingConverter(int dataRegisterAddress, int multiplier) {
        return new ElementToChannelConverter(
                val -> this.channelConverter(val, dataRegisterAddress, multiplier),
                val -> val);
    }

    private Object channelConverter(Object val, int dataRegisterAddress, int multiplier) {
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
            return (value * finalScaleFactor) * multiplier;
        } else {
            return null;
        }
    }
}