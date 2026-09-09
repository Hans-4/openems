package io.openems.edge.ess.saxpower.ess;

import io.openems.common.channel.AccessMode;
import io.openems.common.exceptions.OpenemsError;
import io.openems.common.referencetarget.GenerateTargetsFromReferences;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.element.SignedWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC16WriteRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.modbusslave.ModbusSlave;
import io.openems.edge.common.modbusslave.ModbusSlaveTable;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.common.type.Phase.SinglePhase;
import io.openems.edge.common.type.TypeUtils;
import io.openems.edge.ess.api.AsymmetricEss;
import io.openems.edge.ess.api.ManagedAsymmetricEss;
import io.openems.edge.ess.api.ManagedSinglePhaseEss;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.api.SinglePhaseEss;
import io.openems.edge.ess.api.SymmetricEss;
import io.openems.edge.ess.power.api.Power;
import io.openems.edge.common.sum.GridMode;
import io.openems.edge.ess.saxpower.AddressList;
import io.openems.edge.ess.saxpower.ApplyScaleFactor;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import java.time.Duration;
import java.time.Instant;

import static io.openems.edge.common.channel.ChannelUtils.setValue;
import static org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY;
import static org.osgi.service.component.annotations.ReferencePolicy.STATIC;
import static org.osgi.service.component.annotations.ReferencePolicyOption.GREEDY;

@Designate(ocd = io.openems.edge.ess.saxpower.ess.Config.class, factory = true)
@Component(//
    name = "Ess.SaxPower", //
    immediate = true, //
    configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@GenerateTargetsFromReferences("Modbus")
public class SaxPowerImpl extends AbstractOpenemsModbusComponent
        implements SaxPower, ManagedSinglePhaseEss, SinglePhaseEss, ManagedAsymmetricEss, AsymmetricEss, ManagedSymmetricEss, SymmetricEss, OpenemsComponent, ModbusComponent, ModbusSlave {

    private static final int MAX_APPARENT_POWER = 4600; //230V * 20A


    private final int powerAddress = AddressList.BATTERY_POWER.getAddress();
    private final int powerScaleFactorAddress = AddressList.BATTERY_POWER_SCALE_FACTOR.getAddress();

    private final int powerTarget =  AddressList.BATTERY_POWER_TARGET.getAddress();
    private final int timeout = AddressList.TIMEOUT.getAddress();
    private final int controlMode = AddressList.CONTROL_MODE.getAddress();
    private final int scaleFactorPowerTarget = AddressList.BATTERY_POWER_TARGET_SCALE_FACTOR.getAddress();
    private final int maxPowerReference = AddressList.BATTERY_MAX_POWER_REFERENCE.getAddress();

    private final int currentSoc = AddressList.CURRENT_SOC.getAddress();


    @Reference
    private ConfigurationAdmin cm;
    @Reference
    private Power power;

    private Config config;

    private ControlMode controlModeHandler;

    @Override
    @Reference(//
            name = "Modbus", //
            policy = STATIC, //
            policyOption = GREEDY, //
            cardinality = MANDATORY //
    )
    protected void setModbus(BridgeModbus modbus) {
        super.setModbus(modbus);
    }

    public SaxPowerImpl() {
        super(//
                OpenemsComponent.ChannelId.values(), //
                ModbusComponent.ChannelId.values(), //
                SymmetricEss.ChannelId.values(), //
                ManagedSymmetricEss.ChannelId.values(), //
                AsymmetricEss.ChannelId.values(), //
                ManagedAsymmetricEss.ChannelId.values(), //
                SinglePhaseEss.ChannelId.values(), //
                ManagedSinglePhaseEss.ChannelId.values(), //
                SaxPower.ChannelId.values() //
        );
    }

    @Activate
    private void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException {
        this.config = config;
        SinglePhase phase = config.phase();

        if (super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm, //
                "Modbus", config.modbus_id())) {
            return;
        }

        this.controlModeHandler = new ControlMode(this, 1, config.timeout());

        SinglePhaseEss.initializeCopyPhaseChannel(this, phase);

        this._setCapacity(this.config.capacity());
        this._setMaxApparentPower(MAX_APPARENT_POWER);

        this.getGridModeChannel().setNextValue(GridMode.ON_GRID);


        this.getSocChannel().onSetNextValue(soc -> {
            final Integer allowedCharge;
            final Integer allowedDischarge;
            if (soc.isDefined()) {
                allowedCharge = soc.get() >= 100 ? 0 : -this.config.maxChargePower();
                allowedDischarge = soc.get() <= config.minSoc() ? 0 : this.config.maxDischargePower();
            } else {
                allowedCharge = null;
                allowedDischarge = null;
            }
            setValue(this, ManagedSymmetricEss.ChannelId.ALLOWED_CHARGE_POWER, allowedCharge);
            setValue(this, ManagedSymmetricEss.ChannelId.ALLOWED_DISCHARGE_POWER, allowedDischarge);
        });
    }

    @Override
    @Deactivate
    protected void deactivate() {
        this.lastWrite = null;
        super.deactivate();
    }

    private final ApplyScaleFactor applyScaleFactor = new ApplyScaleFactor();

    @Override
    protected ModbusProtocol defineModbusProtocol() {
        return new ModbusProtocol(this,
                new FC3ReadRegistersTask(this.powerAddress, Priority.HIGH,
                        m(SymmetricEss.ChannelId.ACTIVE_POWER, new SignedWordElement(this.powerAddress), this.applyScaleFactor.createScalingConverter(this.powerAddress, 1)),
                        m(SaxPower.ChannelId.POWER_SCALE_FACTOR, new SignedWordElement(this.powerScaleFactorAddress)),
                        new DummyRegisterElement(40031,40048),
                        m(SaxPower.ChannelId.POWER_TARGET, new SignedWordElement(this.powerTarget), this.applyScaleFactor.createScalingConverter(this.powerTarget, 1)),
                        m(SaxPower.ChannelId.TIMEOUT, new UnsignedWordElement(this.timeout)),
                        m(SaxPower.ChannelId.CONTROL_MODE,  new UnsignedWordElement(this.controlMode)),
                        m(SaxPower.ChannelId.SCALEFACTOR_POWER_TARGET, new SignedWordElement(this.scaleFactorPowerTarget)),
                        m(SaxPower.ChannelId.REFERENCE_MAXIMUM_POWER, new UnsignedWordElement(this.maxPowerReference)),
                        new DummyRegisterElement(40054, 40101),
                        m(SymmetricEss.ChannelId.SOC, new SignedWordElement(this.currentSoc))
                ),

                new FC16WriteRegistersTask(this.powerTarget,
                        m(SaxPower.ChannelId.POWER_TARGET, new SignedWordElement(this.powerTarget)),
                        m(SaxPower.ChannelId.TIMEOUT, new UnsignedWordElement(this.timeout)),
                        m(SaxPower.ChannelId.CONTROL_MODE, new UnsignedWordElement(this.controlMode))
                )
        );
    }

    static io.openems.edge.common.channel.ChannelId activePowerChannelId(SinglePhase phase) {
        return switch (phase) {
            case L1 -> AsymmetricEss.ChannelId.ACTIVE_POWER_L1;
            case L2 -> AsymmetricEss.ChannelId.ACTIVE_POWER_L2;
            case L3 -> AsymmetricEss.ChannelId.ACTIVE_POWER_L3;
        };
    }

    private Instant lastWrite = null;

    @Override
    public void applyPower(int activePower, int reactivePower) throws OpenemsError.OpenemsNamedException {
        final var now = Instant.now();

        // Recommend write interval 500 ms
        if (this.lastWrite == null || Duration.between(this.lastWrite, now).toMillis() > 500) {

            this.controlModeHandler.check();

            final var maxPowerReferenceValue = this.getReferenceMaximumPower().get();
            if (maxPowerReferenceValue == null || maxPowerReferenceValue <= 0) {
                return;
            }
            final int maxPowerReference = maxPowerReferenceValue;

            int percent = activePower * 1000 / maxPowerReference;
            var setPoint = (int) TypeUtils.fitWithin(-10000, 10000, percent);
            setPowerTarget(setPoint);

            this.lastWrite = now;
        }
    }

    @Override
    public Power getPower() {
        return this.power;
    }

    @Override
    public int getPowerPrecision() {
        return 1;
    }

    @Override
    public SinglePhase getPhase() {
        return this.config.phase();
    }


    @Override
    public ModbusSlaveTable getModbusSlaveTable(AccessMode accessMode) {
        return new ModbusSlaveTable(
                OpenemsComponent.getModbusSlaveNatureTable(accessMode),
                SymmetricEss.getModbusSlaveNatureTable(accessMode),
                ManagedSymmetricEss.getModbusSlaveNatureTable(accessMode),
                AsymmetricEss.getModbusSlaveNatureTable(accessMode),
                ManagedAsymmetricEss.getModbusSlaveNatureTable(accessMode)
        );
    }

    @Override
    public String debugLog() {
        return "SoC:" + this.getSoc().asString() + "|L:" + this.getActivePower().asString();
    }
}
