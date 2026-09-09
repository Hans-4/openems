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

    /**
     * Checks if the given address is within this range.
     *
     * @param address the address to check
     * @return true if the address is within the range
     */
    public boolean contains(int address) {
        return address >= this.start && address <= this.end;
    }


    public int getScaleFactorRegisterAddress() {
        return this.scale;
    }
}
