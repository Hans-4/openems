package io.openems.edge.ess.saxpower;

import java.util.List;
import java.util.Map;

class Scales {
    List<RegisterRange> essScaleFactorChannelList = List.of(
            new RegisterRange (AddressList.BATTERY_POWER.getAddress(), AddressList.BATTERY_POWER.getAddress(), AddressList.BATTERY_POWER_SCALE_FACTOR.getAddress()),
            new RegisterRange(AddressList.BATTERY_POWER_TARGET.getAddress(), AddressList.BATTERY_POWER_TARGET.getAddress(), AddressList.BATTERY_POWER_TARGET_SCALE_FACTOR.getAddress()),
            new RegisterRange(AddressList.GRID_POWER.getAddress(), AddressList.GRID_POWER_L3.getAddress(), AddressList.GRID_POWER_SCALE_FACTOR.getAddress())
    );

    Map<Integer, Integer> wellKnownScaleFactorMap = Map.of(
            AddressList.BATTERY_POWER_SCALE_FACTOR.getAddress(), 0,
            AddressList.BATTERY_POWER_TARGET_SCALE_FACTOR.getAddress(), -2,
            AddressList.GRID_POWER_SCALE_FACTOR.getAddress(), 1
    );
}