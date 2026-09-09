package io.openems.edge.ess.saxpower.ess;

import io.openems.common.channel.AccessMode;
import io.openems.common.test.DummyConfigurationAdmin;
import io.openems.edge.bridge.modbus.test.DummyModbusBridge;
import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.edge.common.test.ComponentTest;
import io.openems.edge.common.type.Phase;
import io.openems.edge.ess.api.AsymmetricEss;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.api.SymmetricEss;
import org.junit.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SaxPowerImplTest {

    @Test
    public void test() throws Exception {
        new ComponentTest(new SaxPowerImpl())
                .addReference("cm", new DummyConfigurationAdmin())
                .addReference("setModbus", new DummyModbusBridge("modbus0"))
                .activate(MyConfig.create()
                        .setId("ess0")
                        .setModbusId("modbus0")
                        .setModbusUnitId(64)
                        .setCapacity(7000)
                        .build()
                )
                .next(new TestCase()
                        .output(SymmetricEss.ChannelId.CAPACITY, 7000)
                )
                .deactivate();
    }

    @Test
    public void testLimits() throws Exception {
        new ComponentTest(new SaxPowerImpl())
                .addReference("cm", new DummyConfigurationAdmin())
                .addReference("setModbus", new DummyModbusBridge("modbus0"))
                .activate(MyConfig.create()
                        .setId("ess0")
                        .setModbusId("modbus0")
                        .setModbusUnitId(64)
                        .setCapacity(7000)
                        .setMaxDischargePower(4600)
                        .setMaxChargePower(1400)
                        .setMinSoc(10)
                        .build()
                )
                .next(new TestCase()
                        .input(SymmetricEss.ChannelId.SOC, 80)
                        .output(ManagedSymmetricEss.ChannelId.ALLOWED_CHARGE_POWER, -1400)
                        .output(ManagedSymmetricEss.ChannelId.ALLOWED_DISCHARGE_POWER, 4600)
                )
                .next(new TestCase()
                        .input(SymmetricEss.ChannelId.SOC, 100)
                        .output(ManagedSymmetricEss.ChannelId.ALLOWED_CHARGE_POWER, 0)
                        .output(ManagedSymmetricEss.ChannelId.ALLOWED_DISCHARGE_POWER, 4600)
                )
                .next(new TestCase()
                        .input(SymmetricEss.ChannelId.SOC, 10)
                        .output(ManagedSymmetricEss.ChannelId.ALLOWED_CHARGE_POWER, -1400)
                        .output(ManagedSymmetricEss.ChannelId.ALLOWED_DISCHARGE_POWER, 0)
                )
                .next(new TestCase()
                        .input(SymmetricEss.ChannelId.SOC, null)
                        .output(ManagedSymmetricEss.ChannelId.ALLOWED_CHARGE_POWER, null)
                        .output(ManagedSymmetricEss.ChannelId.ALLOWED_DISCHARGE_POWER, null)
                )
                .deactivate();
    }

    @Test
    public void testActivePowerChannelId() {
        assertEquals(
                AsymmetricEss.ChannelId.ACTIVE_POWER_L1,
                SaxPowerImpl.activePowerChannelId(Phase.SinglePhase.L1)
        );

        assertEquals(
                AsymmetricEss.ChannelId.ACTIVE_POWER_L2,
                SaxPowerImpl.activePowerChannelId(Phase.SinglePhase.L2)
        );

        assertEquals(
                AsymmetricEss.ChannelId.ACTIVE_POWER_L3,
                SaxPowerImpl.activePowerChannelId(Phase.SinglePhase.L3)
        );
    }

    @Test
    public void testApplyPower() throws Exception {
        var sut = new SaxPowerImpl();
        new ComponentTest(sut)
                .addReference("cm", new DummyConfigurationAdmin())
                .addReference("setModbus", new DummyModbusBridge("modbus0"))
                .activate(MyConfig.create()
                        .setId("ess0")
                        .setModbusId("modbus0")
                        .setModbusUnitId(100)
                        .setCapacity(7000)
                        .build()
                );

        var lastWriteField = SaxPowerImpl.class.getDeclaredField("lastWrite");
        lastWriteField.setAccessible(true);

        sut.applyPower(1000, 0);
        assertEquals(
                Integer.valueOf(21),
                sut.getTargetPowerChannel().getNextWriteValue().orElse(null)
        );

        lastWriteField.set(sut, Instant.now());
        sut.applyPower(2000, 0);
        assertEquals(
                Integer.valueOf(21),
                sut.getTargetPowerChannel().getNextWriteValue().orElse(null)
        );

        lastWriteField.set(sut, Instant.now().minusSeconds(6));
        sut.applyPower(-1000, 0);
        assertEquals(
                Integer.valueOf(0),
                sut.getTargetPowerChannel().getNextWriteValue().orElse(null)
        );

        sut.deactivate();
    }

    @Test
    public void testDebugLog() throws Exception {
        var sut = new SaxPowerImpl();
        new ComponentTest(sut)
                .addReference("cm", new DummyConfigurationAdmin())
                .addReference("setModbus", new DummyModbusBridge("modbus0"))
                .activate(MyConfig.create()
                        .setId("ess0")
                        .setModbusId("modbus0")
                        .setModbusUnitId(100)
                        .setCapacity(7000)
                        .build()
                )

                .next(new TestCase()
                        .input(SymmetricEss.ChannelId.SOC, 80)
                        .input(SymmetricEss.ChannelId.ACTIVE_POWER, 1500)
                );

        String log = sut.debugLog();
        assertEquals("SoC:80 %|L:1500 W", log);

        sut.deactivate();
    }

    @Test
    public void testGetModbusSlaveTable() throws Exception {
        var sut = new SaxPowerImpl();
        new ComponentTest(sut)
                .addReference("cm", new DummyConfigurationAdmin())
                .addReference("setModbus", new DummyModbusBridge("modbus0"))
                .activate(MyConfig.create()
                        .setId("ess0")
                        .setModbusId("modbus0")
                        .setModbusUnitId(100)
                        .setCapacity(7000)
                        .build()
                );

        assertNotNull(sut.getModbusSlaveTable(AccessMode.READ_ONLY));
        assertNotNull(sut.getModbusSlaveTable(AccessMode.READ_WRITE));
        assertNotNull(sut.getModbusSlaveTable(AccessMode.WRITE_ONLY));

        sut.deactivate();
    }
}
