package io.openems.edge.ess.saxpower.gridmeter;

import io.openems.common.test.DummyConfigurationAdmin;
import io.openems.common.types.MeterType;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.bridge.modbus.api.task.Task;
import io.openems.edge.bridge.modbus.test.DummyModbusBridge;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.common.test.AbstractComponentTest;
import io.openems.edge.common.test.ComponentTest;
import io.openems.edge.meter.api.ElectricityMeter;
import org.junit.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SaxPowerEssGridMeterImplTest {

    @Test
    public void test() throws Exception {
        new ComponentTest(new SaxPowerEssGridMeterImpl())
                .addReference("cm", new DummyConfigurationAdmin())
                .addReference("setModbus", new DummyModbusBridge("modbus0"))
                .activate(MyConfig.create()
                        .setId("ess0")
                        .setModbusId("modbus0")
                        .setType(MeterType.GRID)
                        .setModbusUnitId(100)
                        .build()
                )
                .next(new AbstractComponentTest.TestCase())
                .deactivate();
    }

    @Test
    public void testDefineModbusProtocol() throws Exception {
        var sut = new SaxPowerEssGridMeterImpl();
        new ComponentTest(sut)
                .addReference("cm", new DummyConfigurationAdmin())
                .addReference("setModbus", new DummyModbusBridge("modbus0"))
                .activate(MyConfig.create()
                        .setId("ess0")
                        .setModbusId("modbus0")
                        .setType(MeterType.GRID)
                        .setModbusUnitId(100)
                        .build()
                );

        ModbusProtocol protocol = sut.defineModbusProtocol();

        List<Task> tasks = protocol.getTaskManager().getTasks();

        assertEquals(1, tasks.size());

        FC3ReadRegistersTask readTask = tasks.stream()
                .filter(t -> t instanceof FC3ReadRegistersTask)
                .map(t -> (FC3ReadRegistersTask) t)
                .findFirst()
                .orElseThrow();

        assertEquals(40072, readTask.getStartAddress());
        assertEquals(Priority.HIGH, readTask.getPriority());

        sut.deactivate();
    }

    @Test
    public void testDebugLog() throws Exception {
        var sut = new SaxPowerEssGridMeterImpl();
        new ComponentTest(sut)
                .addReference("cm", new DummyConfigurationAdmin())
                .addReference("setModbus", new DummyModbusBridge("modbus0"))
                .activate(MyConfig.create()
                        .setId("ess0")
                        .setModbusId("modbus0")
                        .setType(MeterType.GRID)
                        .setModbusUnitId(100)
                        .build()
                )

                .next(new AbstractComponentTest.TestCase()
                        .input(ElectricityMeter.ChannelId.ACTIVE_POWER, 1500)
                        .input(ElectricityMeter.ChannelId.ACTIVE_POWER_L1, 500)
                        .input(ElectricityMeter.ChannelId.ACTIVE_POWER_L2, 500)
                        .input(ElectricityMeter.ChannelId.ACTIVE_POWER_L3, 500)
                );

        String log = sut.debugLog();
        assertEquals("L:1500 W|L1:500 W|L2:500 W|L3:500 W", log);

        sut.deactivate();
    }
}
