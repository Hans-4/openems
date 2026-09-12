package io.openems.edge.ess.saxpower;

import java.util.List;
import java.util.Map;

class Scales {
    private final List<RegisterRange> essScaleFactorChannelList = List.of(
            new RegisterRange(AddressList.BATTERY_POWER.getAddress(), AddressList.BATTERY_POWER.getAddress(), AddressList.BATTERY_POWER_SCALE_FACTOR.getAddress()),
            new RegisterRange(AddressList.BATTERY_POWER_TARGET.getAddress(), AddressList.BATTERY_POWER_TARGET.getAddress(), AddressList.BATTERY_POWER_TARGET_SCALE_FACTOR.getAddress()),
            new RegisterRange(AddressList.GRID_POWER.getAddress(), AddressList.GRID_POWER_L3.getAddress(), AddressList.GRID_POWER_SCALE_FACTOR.getAddress()),
            new RegisterRange(AddressList.CAPACITY.getAddress(), AddressList.CAPACITY.getAddress(), AddressList.CAPACITY_SCALE_FACTOR.getAddress()),
            new RegisterRange(AddressList.CHARGE_POWER.getAddress(), AddressList.DISCHARGE_POWER.getAddress(), AddressList.CHARGE_DISCHARGE_POWER_SCALE_FACTOR.getAddress()),
            new RegisterRange(AddressList.CURRENT_SOC.getAddress(), AddressList.CURRENT_SOC.getAddress(), AddressList.SOC_SCALE_FACTOR.getAddress())
    );

    private final Map<Integer, Integer> wellKnownScaleFactorMap = Map.of(
            AddressList.BATTERY_POWER_SCALE_FACTOR.getAddress(), 0,
            AddressList.BATTERY_POWER_TARGET_SCALE_FACTOR.getAddress(), -2,
            AddressList.GRID_POWER_SCALE_FACTOR.getAddress(), 1,
            AddressList.CAPACITY_SCALE_FACTOR.getAddress(), 0,
            AddressList.CHARGE_DISCHARGE_POWER_SCALE_FACTOR.getAddress(), 0,
            AddressList.SOC_SCALE_FACTOR.getAddress(), 0
    );

    public List<RegisterRange> getEssScaleFactorChannelList() {
        return this.essScaleFactorChannelList;
    }

    public Map<Integer, Integer> getWellKnownScaleFactorMap() {
        return this.wellKnownScaleFactorMap;
    }
}