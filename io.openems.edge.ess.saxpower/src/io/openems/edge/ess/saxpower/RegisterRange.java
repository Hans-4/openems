package io.openems.edge.ess.saxpower;

public class RegisterRange {
    private final int start;
    private final int end;
    private final int scale;

    public RegisterRange(int start, int end, int scale) {
        this.start = start;
        this.end = end;
        this.scale = scale;
    }

    public boolean contains(int address) {
        return address >= start && address <= end;
    }


    public int getScaleFactorRegisterAddress() {
        return scale;
    }
}
