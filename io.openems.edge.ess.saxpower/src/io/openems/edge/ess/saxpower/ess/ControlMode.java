package io.openems.edge.ess.saxpower.ess;

import io.openems.common.exceptions.OpenemsError;

public class ControlMode {
    private final SaxPower ess;
    private final int controlMode;
    private final int timeout;

    public ControlMode(SaxPower ess, int controlMode, int timeout) {
        this.ess = ess; //
        this.controlMode = controlMode; //
        this.timeout = timeout; //
    }

    /**
     * Checks if control mode is 1 and sets it to 1 if not.
     */
    public void check() throws OpenemsError.OpenemsNamedException {
        Integer actualMode = this.ess.getControlMode().get();
        Integer actualTimeout = this.ess.getTimeout().get();

        if (actualMode == null || actualMode != this.controlMode) {
            this.ess.setControlMode(this.controlMode);
        }
        if (actualTimeout == null || actualTimeout != this.timeout) {
            this.ess.setTimeout(this.timeout);
        }
    }
}
