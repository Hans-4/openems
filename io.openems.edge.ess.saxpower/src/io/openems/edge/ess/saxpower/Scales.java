package io.openems.edge.ess.saxpower;

import java.util.List;

class Scales {
    List<RegisterRange> essScaleFactorChannelList = List.of(
            new RegisterRange (AddressList.BATTERY_POWER.getAddress(), AddressList.BATTERY_POWER.getAddress(), AddressList.BATTERY_POWER_SCALE_FACTOR.getAddress()),
            new RegisterRange(AddressList.BATTERY_POWER_TARGET.getAddress(), AddressList.BATTERY_POWER_TARGET.getAddress(), AddressList.BATTERY_POWER_TARGET_SCALE_FACTOR.getAddress()),
            new RegisterRange(AddressList.GRID_POWER.getAddress(), AddressList.GRID_POWER_L3.getAddress(), AddressList.GRID_POWER_SCALE_FACTOR.getAddress())
    );
}