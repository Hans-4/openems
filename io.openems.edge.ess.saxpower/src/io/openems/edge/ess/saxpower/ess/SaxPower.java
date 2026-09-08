package io.openems.edge.ess.saxpower.ess;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.exceptions.OpenemsError;
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.modbusslave.ModbusSlave;
import io.openems.edge.common.modbusslave.ModbusSlaveNatureTable;
import io.openems.edge.common.modbusslave.ModbusType;
import io.openems.edge.ess.api.AsymmetricEss;
import io.openems.edge.ess.api.ManagedAsymmetricEss;
import io.openems.edge.ess.api.ManagedSinglePhaseEss;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.api.SinglePhaseEss;
import io.openems.edge.ess.api.SymmetricEss;

public interface SaxPower extends ManagedSinglePhaseEss, ManagedAsymmetricEss, ManagedSymmetricEss, SinglePhaseEss,
        AsymmetricEss, SymmetricEss, OpenemsComponent, ModbusComponent, ModbusSlave {

    enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        //Address 40030
        POWER_SCALE_FACTOR(Doc.of(OpenemsType.INTEGER)
                .accessMode(AccessMode.READ_ONLY)
        ),

        //Address 40049
        POWER_TARGET(Doc.of(OpenemsType.INTEGER)
                .accessMode(AccessMode.READ_WRITE)
                .unit(Unit.PERCENT)
        ),

        //Address 40050
        TIMEOUT(Doc.of(OpenemsType.INTEGER)
                .accessMode(AccessMode.READ_WRITE)
                .unit(Unit.SECONDS)
        ),

        //Address 40051
        CONTROL_MODE(Doc.of(OpenemsType.INTEGER)
                .accessMode(AccessMode.READ_WRITE)
                .unit(Unit.SECONDS)
        ),

        //Address 40052
        SCALEFACTOR_POWER_TARGET(Doc.of(OpenemsType.INTEGER)
                .accessMode(AccessMode.READ_ONLY)
        ),

        //Address 40053
        REFERENCE_MAXIMUM_POWER(Doc.of(OpenemsType.INTEGER)
                .accessMode(AccessMode.READ_ONLY)
                .unit(Unit.WATT)
        );

        private final Doc doc;

        ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }
    }

    default WriteChannel<Integer> getActivePowerSetPointChannel() {
        return this.channel(ChannelId.POWER_TARGET);
    }

    default void setActivePowerSetPoint(Integer value) throws OpenemsError.OpenemsNamedException {
        this.getActivePowerSetPointChannel().setNextWriteValue(value);
    }

    @Override
    default void applyPower(int activePowerL1, int reactivePowerL1, int activePowerL2, int reactivePowerL2,
            int activePowerL3, int reactivePowerL3) throws OpenemsError.OpenemsNamedException {
        ManagedSinglePhaseEss.super.applyPower(activePowerL1, reactivePowerL1, activePowerL2, reactivePowerL2,
                activePowerL3, reactivePowerL3);
    }

    /**
     * Used for Modbus/TCP Api Controller. Provides a Modbus table for the Channels
     * of this Component.
     *
     * @param accessMode filters the Modbus-Records that should be shown
     * @return the {@link ModbusSlaveNatureTable}
     */
    static ModbusSlaveNatureTable getModbusSlaveNatureTable(AccessMode accessMode) {
        return ModbusSlaveNatureTable.of(SaxPower.class, accessMode, 40049)
                .channel(0, ChannelId.POWER_TARGET, ModbusType.UINT16) // 40049
                .build();
    }
}