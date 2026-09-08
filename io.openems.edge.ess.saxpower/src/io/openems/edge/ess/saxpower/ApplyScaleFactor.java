package io.openems.edge.ess.saxpower;

import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import java.util.function.Function;

public class ApplyScaleFactor {
    Scales scales = new Scales();

    private final Function<Integer, Integer> getRegisterValue;

    public ApplyScaleFactor(Function<Integer, Integer> getRegisterValue) {
        this.getRegisterValue = getRegisterValue;
    }

    public ElementToChannelConverter createScalingConverter(int dataRegisterAddress) {
        return new ElementToChannelConverter(val ->
                this.channelConverter(val, dataRegisterAddress)
        );
    }

    private Object channelConverter(Object val, int dataRegisterAddress) {
        if (val == null) {
            return null;
        }
        int value = ((Number)val).intValue();

        Integer scaleFactorRegisterAddress = null;
        for (RegisterRange range : this.scales.essScaleFactorChannelList) {
            if (range.contains(dataRegisterAddress)) {
                scaleFactorRegisterAddress = range.getScaleFactorRegisterAddress();
                break;
            }
        }

        if (scaleFactorRegisterAddress != null) {
            int scaleFactorValue = this.getRegisterValue.apply(scaleFactorRegisterAddress);
            int scaleFactor = (int) Math.pow(10, scaleFactorValue);
            int scaledValue = value * scaleFactor;
            return encode_int16(scaledValue);
        }
        return value;
    }

    private int encode_int16(int raw) {
        if (raw > 32767) {
            return raw - 65536;
        } else {
            return raw;
        }
    }
}