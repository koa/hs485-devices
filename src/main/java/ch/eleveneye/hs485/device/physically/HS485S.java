package ch.eleveneye.hs485.device.physically;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.eleveneye.hs485.api.MessageHandler;
import ch.eleveneye.hs485.api.data.HwVer;
import ch.eleveneye.hs485.api.data.KeyMessage;
import ch.eleveneye.hs485.api.data.SwVer;
import ch.eleveneye.hs485.device.ActorType;
import ch.eleveneye.hs485.device.KeyActor;
import ch.eleveneye.hs485.device.KeySensor;
import ch.eleveneye.hs485.device.Sensor;
import ch.eleveneye.hs485.device.TimedActor;
import ch.eleveneye.hs485.device.config.ConfigurableInputDescription;
import ch.eleveneye.hs485.device.config.ConfigurableOutputDescription;
import ch.eleveneye.hs485.device.config.PairMode;
import ch.eleveneye.hs485.device.config.TimeMode;
import ch.eleveneye.hs485.device.utils.AbstractActor;
import ch.eleveneye.hs485.device.utils.AbstractDevice;
import ch.eleveneye.hs485.device.utils.AbstractSensor;
import ch.eleveneye.hs485.memory.ArrayVariable;
import ch.eleveneye.hs485.memory.ChoiceEntry;
import ch.eleveneye.hs485.memory.ChoiceVariable;
import ch.eleveneye.hs485.memory.ModuleType;
import ch.eleveneye.hs485.memory.NumberVariable;

public class HS485S extends AbstractDevice implements PairedSensorDevice {

	private final class HS485SActor extends AbstractActor implements TimedActor, KeyActor {
		private HS485SActor(final int actorNr) {
			super(actorNr);
		}

		public int getModuleAddr() {
			return deviceAddr;
		}

		public TimeMode getTimeMode() throws IOException {
			switch (readVariable("output[" + actorNr + "].timer-mode")) {
			case 0xff:
			case 0x00:
				return TimeMode.NONE;
			case 0x01:
				return TimeMode.STAIRCASE;
			case 0x02:
				return TimeMode.AUTO_OFF;
			}
			return null;
		}

		public int getTimeValue() throws IOException {
			return readVariable("output-time[" + actorNr + "].time");
		}

		public boolean getToggleBit() throws IOException {
			final int mask = 1 << actorNr;
			final int value = readVariable("toggle-value");
			return (value & mask) != 0;
		}

		public boolean isOn() throws IOException {
			return bus.readActor(deviceAddr, (byte) actorNr) > 0;
		}

		public void sendKeyMessage(final KeyMessage keyMessage) throws IOException {
			final KeyMessage sendMessage = new KeyMessage(keyMessage);
			sendMessage.setTargetAddress(deviceAddr);
			sendMessage.setTargetActor(actorNr);
			bus.sendKeyMessage(sendMessage);
		}

		public void setOff() throws IOException {
			bus.writeActor(deviceAddr, (byte) actorNr, (byte) 0x00);

		}

		public void setOn() throws IOException {
			bus.writeActor(deviceAddr, (byte) actorNr, (byte) 0x01);
		}

		public void setTimeMode(final TimeMode value) throws IOException {
			switch (value) {
			case NONE:
				writeVariable("output[" + actorNr + "].timer-mode", 0xff);
				break;
			case STAIRCASE:
				writeVariable("output[" + actorNr + "].timer-mode", 0x01);
				break;
			case AUTO_OFF:
				writeVariable("output[" + actorNr + "].timer-mode", 0x02);
				break;
			}
		}

		public void setTimeValue(final int value) throws IOException {
			writeVariable("output-time[" + actorNr + "].time", value);
		}

		public void setToggleBit(final boolean value) throws IOException {
			final int oldValue = readVariable("toggle-value");
			final int mask = 1 << actorNr;
			writeVariable("toggle-value", oldValue & (0xff ^ mask) | (value ? mask : 0));
		}

		public void toggle() throws IOException {
			bus.writeActor(deviceAddr, (byte) actorNr, (byte) 0xff);
		}

		@Override
		public String toString() {
			return "S-" + super.toString();
		}
	}

	private final class HS485SSensor extends AbstractSensor implements PairableSensor, KeySensor {
		private HS485SSensor(final int sensorNr) {
			super(sensorNr);
		}

		public void addActor(final Actor target) throws IOException {
			final String variableName = "input[" + sensorNr + "].direct-output";
			final int directValue = readVariable(variableName);
			final int otherOutput = target.getActorNr() == 1 ? 2 : 1;
			if (target.getModuleAddr() == deviceAddr && directValue != otherOutput)
				writeVariable(variableName, target.getActorNr());
			else
				addInputTargetRaw(sensorNr, target.getModuleAddr(), target.getActorNr());
		}

		public int getModuleAddr() {
			return deviceAddr;
		}

		public boolean isPaired() throws IOException {
			return isInputPaired();
		}

		public Collection<Actor> listAssignedActors() throws IOException {
			loadActorList();
			final Collection<Actor> ret = listAssignedActorsRaw(sensorNr);
			final int directValue = readVariable("input[" + sensorNr + "].direct-output");
			switch (directValue) {
			case 0xff:
				ret.add(getActor(sensorNr));
				break;
			case 0x1:
			case 0x2:
				ret.add(getActor(directValue));
				break;
			}
			return ret;
		}

		public void registerHandler(final MessageHandler handler) throws IOException {
			bus.addKeyHandler(deviceAddr, (byte) sensorNr, handler);
			if (handler != null)
				addInputTargetRaw(sensorNr, bus.listOwnAddresse()[0], 1);
			else
				removeInputTargetRaw(sensorNr, bus.listOwnAddresse()[0], 1);
		}

		public void removeActor(final Actor target) throws IOException {
			final String variableName = "input[" + sensorNr + "].direct-output";
			if (target.getModuleAddr() == deviceAddr && readVariable(variableName) == target.getActorNr())
				writeVariable(variableName, 0xfe);
			removeInputTargetRaw(sensorNr, target.getModuleAddr(), target.getActorNr());
		}

		@Override
		public String toString() {
			try {
				return "S-" + Integer.toHexString(deviceAddr) + (isPaired() ? "" : "-" + (sensorNr + 1));
			} catch (final IOException e) {
				return "S-" + Integer.toHexString(deviceAddr) + "-" + (sensorNr + 1);
			}
		}
	}

	private static final Map<String, ActorType> actors = new HashMap<String, ActorType>();

	static {
		HS485S.actors.put("1", ActorType.HIGH_VOLTAGE);
		HS485S.actors.put("2", ActorType.HIGH_VOLTAGE);
	}

	public static List<ModuleType> getAvailableConfig() {
		final List<ModuleType> versions = new LinkedList<ModuleType>();
		final ModuleType hs485s = new ModuleType();
		hs485s.setHwVer(new HwVer((byte) 1, (byte) 1));
		hs485s.setSwVer(new SwVer((byte) 2, (byte) 0));
		hs485s.setName("HS485S");
		hs485s.setEepromSize(512);
		hs485s.setImplementingClass(HS485S.class);
		hs485s.setWidth(2);

		final ChoiceVariable inputType = new ChoiceVariable();
		inputType.setAddress(0);
		inputType.setLength(1);
		inputType.setName("input-type");
		inputType.addChoiceEntry(new ChoiceEntry(0x00, "toggle", ""));
		inputType.addChoiceEntry(new ChoiceEntry(0x01, "up/down", ""));
		inputType.addChoiceEntry(new ChoiceEntry(0xff, "toggle", ""));
		hs485s.addVariable(inputType);

		final ArrayVariable inputConfig = new ArrayVariable();
		inputConfig.setAddress(0x12);
		inputConfig.setCount(2);
		inputConfig.setStep(1);
		inputConfig.setName("input");
		inputConfig.setReload(true);
		final ChoiceVariable sensorType = new ChoiceVariable();
		sensorType.setAddress(0);
		sensorType.setLength(1);
		sensorType.setName("sensor-type");
		sensorType.addChoiceEntry(new ChoiceEntry(0xff, "push-w-led", ""));
		sensorType.addChoiceEntry(new ChoiceEntry(0x01, "push-wo-led", ""));
		sensorType.addChoiceEntry(new ChoiceEntry(0x02, "switch-wo-led", ""));
		sensorType.addChoiceEntry(new ChoiceEntry(0x03, "switch-w-led", ""));
		inputConfig.addComponent(sensorType);
		final ChoiceVariable directConnection = new ChoiceVariable();
		directConnection.setAddress(0x10 - 0x12);
		directConnection.setLength(1);
		directConnection.setName("direct-output");
		directConnection.addChoiceEntry(new ChoiceEntry(0xfe, "none", ""));
		directConnection.addChoiceEntry(new ChoiceEntry(0x00, "output1", ""));
		directConnection.addChoiceEntry(new ChoiceEntry(0x01, "output2", ""));
		inputConfig.addComponent(directConnection);
		hs485s.addVariable(inputConfig);

		final ArrayVariable outputTime = new ArrayVariable();
		outputTime.setAddress(0x03);
		outputTime.setCount(2);
		outputTime.setStep(2);
		outputTime.setReload(true);
		outputTime.setName("output-time");
		final NumberVariable time = new NumberVariable();
		time.setName("time");
		time.setAddress(0);
		time.setLength(2);
		time.setMinValue(0);
		time.setMaxValue(65534);
		time.setDefaultValue(0xffff);
		outputTime.addComponent(time);
		hs485s.addVariable(outputTime);

		final ArrayVariable outputConfig = new ArrayVariable();
		outputConfig.setAddress(0x7);
		outputConfig.setCount(2);
		outputConfig.setStep(1);
		outputConfig.setReload(true);
		outputConfig.setName("output");
		final ChoiceVariable outputTimer = new ChoiceVariable();
		outputTimer.setAddress(0);
		outputTimer.setLength(1);
		outputTimer.setName("timer-mode");
		outputTimer.addChoiceEntry(new ChoiceEntry(0xff, "none", ""));
		outputTimer.addChoiceEntry(new ChoiceEntry(0x01, "staircase", ""));
		outputTimer.addChoiceEntry(new ChoiceEntry(0x02, "auto-off", ""));
		outputConfig.addComponent(outputTimer);
		hs485s.addVariable(outputConfig);

		final NumberVariable toggleValue = new NumberVariable();
		toggleValue.setName("toggle-value");
		toggleValue.setAddress(0x0a);
		toggleValue.setLength(1);
		toggleValue.setReload(true);
		hs485s.addVariable(toggleValue);

		final ArrayVariable targetConfig = AbstractDevice.defaultTargetConfig();
		hs485s.addVariable(targetConfig);

		versions.add(hs485s);

		return versions;
	}

	private List<Actor>										actorList;

	private Collection<PhysicallySensor>	sensorList;

	@Override
	public void clearAllInputTargets() throws IOException {
		super.clearAllInputTargets();
		writeVariable("input[0].direct-output", 0xfe);
		writeVariable("input[1].direct-output", 0xfe);
	}

	public synchronized Actor getActor(final int actorNr) throws IOException {
		loadActorList();
		return actorList.get(actorNr);
	}

	public int getActorCount() {
		return 2;
	}

	public int getInputPairCount() {
		return 1;
	}

	public PairMode getInputPairMode(final int pairNr) throws IOException {
		return isInputPaired() ? PairMode.JOINT : PairMode.SPLIT;
	}

	public PhysicallySensor getSensor(final int sensorNr) throws IOException {
		loadSensorList();
		for (final PhysicallySensor physicallySensor : sensorList)
			if (physicallySensor.getSensorNr() == sensorNr)
				return physicallySensor;
		return null;
	}

	private boolean isInputPaired() throws IOException {
		return readVariable("input-type") == 0x01;
	}

	public synchronized Collection<Actor> listActors() throws IOException {
		loadActorList();
		return new ArrayList<Actor>(actorList);
	}

	public List<ConfigurableInputDescription> listConfigurableInputs() {
		final ArrayList<ConfigurableInputDescription> inputs = new ArrayList<ConfigurableInputDescription>(2);
		final ConfigurableInputDescription t1 = new ConfigurableInputDescription();
		t1.setLabeledName(toString() + ": T1");
		t1.setSensorNr(0);
		t1.setImplementionSensor(HS485SSensor.class);
		inputs.add(t1);
		final ConfigurableInputDescription t2 = new ConfigurableInputDescription();
		t2.setLabeledName(toString() + ": T2");
		t2.setSensorNr(1);
		t2.setImplementionSensor(HS485SSensor.class);
		inputs.add(t2);
		return inputs;
	}

	public List<ConfigurableOutputDescription> listConfigurableOutputs() {
		final ArrayList<ConfigurableOutputDescription> outputs = new ArrayList<ConfigurableOutputDescription>(2);
		final ConfigurableOutputDescription a1 = new ConfigurableOutputDescription();
		a1.setLabeledName(toString() + ": Rel 1");
		a1.setActorNr(0);
		a1.setImplementingActor(HS485SActor.class);
		outputs.add(a1);
		final ConfigurableOutputDescription a2 = new ConfigurableOutputDescription();
		a2.setLabeledName(toString() + ": Rel 2");
		a2.setActorNr(1);
		a2.setImplementingActor(HS485SActor.class);
		outputs.add(a2);
		return outputs;
	}

	public synchronized Collection<Sensor> listSensors() throws IOException {
		loadSensorList();
		return new ArrayList<Sensor>(sensorList);
	}

	private void loadActorList() {
		if (actorList == null)
			actorList = Arrays.asList(new Actor[] { new HS485SActor(0), new HS485SActor(1) });
	}

	private void loadSensorList() throws IOException {
		if (sensorList == null)
			if (isInputPaired())
				sensorList = Arrays.asList(new PhysicallySensor[] { new HS485SSensor(0) });
			else
				sensorList = Arrays.asList(new PhysicallySensor[] { new HS485SSensor(0), new HS485SSensor(1) });
	}

	public synchronized void setInputPairMode(final int pairNr, final PairMode mode) throws IOException {
		writeVariable("input-type", mode == PairMode.JOINT ? 0x01 : 0x00);
		sensorList = null;
	}

	@Override
	public String toString() {
		return "S-" + Integer.toHexString(deviceAddr);
	}

}
