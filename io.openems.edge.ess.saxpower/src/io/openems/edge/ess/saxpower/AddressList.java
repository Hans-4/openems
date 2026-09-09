package io.openems.edge.ess.saxpower;

public enum AddressList {
    BATTERY_POWER(40029),
    BATTERY_POWER_SCALE_FACTOR(40030),

    BATTERY_POWER_TARGET(40049),
    TIMEOUT(40050),
    CONTROL_MODE(40051),
    BATTERY_POWER_TARGET_SCALE_FACTOR(40052),
    BATTERY_MAX_POWER_REFERENCE(40053),

    GRID_POWER(40072),
    GRID_POWER_L1(40073),
    GRID_POWER_L2(40074),
    GRID_POWER_L3(40075),
    GRID_POWER_SCALE_FACTOR(40076),

    CURRENT_SOC(40102);

    private final int address;

    AddressList(int address) {
        this.address = address;
    }

    public int getAddress() {
        return address;
    }
}
