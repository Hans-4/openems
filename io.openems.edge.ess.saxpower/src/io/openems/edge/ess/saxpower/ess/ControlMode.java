package io.openems.edge.ess.saxpower.ess;

import io.openems.common.exceptions.OpenemsError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlMode {
    private final SaxPower ess;
    private final int controlMode;
    private final int timeout;

    private final Logger log = LoggerFactory.getLogger(ControlMode.class);

    public ControlMode(SaxPower ess, int controlMode, int timeout) {
        this.ess = ess;
        this.controlMode = controlMode;
        this.timeout = timeout;
    }

    /**
     * Checks if control mode is 1 and sets it to 1 if not.
     */
    public void check() throws OpenemsError.OpenemsNamedException {
        Integer actualMode = this.ess.getControlMode().get();
        Integer actualTimeout = this.ess.getTimeout().get();

        if (actualMode == null || actualMode != this.controlMode) {
            this.ess.setControlMode(this.controlMode);
            this.log.info("Refresh control mode: {}", this.controlMode);
        }
        if (actualTimeout == null || actualTimeout != this.timeout) {
            this.ess.setTimeout(this.timeout);
            this.log.info("Refresh timeout: {}", this.timeout);
        }
    }
}
