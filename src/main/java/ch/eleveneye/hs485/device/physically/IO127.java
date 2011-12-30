package ch.eleveneye.hs485.device.physically;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ch.eleveneye.hs485.api.data.HwVer;
import ch.eleveneye.hs485.api.data.SwVer;
import ch.eleveneye.hs485.device.ActorType;
import ch.eleveneye.hs485.device.KeySensor;
import ch.eleveneye.hs485.device.Sensor;
import ch.eleveneye.hs485.device.TimedActor;
import ch.eleveneye.hs485.device.config.TimeMode;
import ch.eleveneye.hs485.device.utils.AbstractActor;
import ch.eleveneye.hs485.device.utils.AbstractDevice;
import ch.eleveneye.hs485.device.utils.AbstractSensor;
import ch.eleveneye.hs485.memory.ArrayVariable;
import ch.eleveneye.hs485.memory.ChoiceEntry;
import ch.eleveneye.hs485.memory.ChoiceVariable;
import ch.eleveneye.hs485.memory.ModuleType;
import ch.eleveneye.hs485.memory.NumberVariable;

public class IO127 extends AbstractDevice {

	private final class IO127Actor extends AbstractActor implements TimedActor {
		private IO127Actor(final int actorNr) {
			super(actorNr);
		}

		public int getModuleAddr() {
			return deviceAddr;
		}

		public TimeMode getTimeMode() throws IOException {
			switch (readVariable("output[" + (actorNr - 12) + "].timer-mode")) {
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
			return readVariable("output-time[" + (actorNr - 12) + "].time");
		}

		public boolean getToggleBit() throws IOException {
			return readVariable("output[" + (actorNr - 12) + "].toggle") != 0;
		}

		public boolean isOn() throws IOException {
			return bus.readActor(deviceAddr, (byte) actorNr) > 0;
		}

		public void setOff() throws IOException {
			bus.writeActor(deviceAddr, (byte) actorNr, (byte) 0x00);

		}

		public void setOn() throws IOException {
			bus.writeActor(deviceAddr, (byte) actorNr, (byte) 0x01);
		}

		public void setTimeMode(final TimeMode value) throws IOException {
			final String variableName = "output[" + (actorNr - 12) + "].timer-mode";
			switch (value) {
			case NONE:
				writeVariable(variableName, 0xff);
				break;
			case STAIRCASE:
				writeVariable(variableName, 0x01);
				break;
			case AUTO_OFF:
				writeVariable(variableName, 0x02);
				break;
			}
		}

		public void setTimeValue(final int value) throws IOException {
			writeVariable("output-time[" + (actorNr - 12) + "].time", value);
		}

		public void setToggleBit(final boolean value) throws IOException {
			writeVariable("output[" + (actorNr - 12) + "].toggle", value ? 0xff : 0x00);
		}

		public void toggle() throws IOException {
			bus.writeActor(deviceAddr, (byte) actorNr, (byte) 0xff);
		}

		@Override
		public String toString() {
			return "IO127-Actor " + Integer.toHexString(getModuleAddr()) + ":" + (actorNr - 11);
		}
	}

	private final class IO127Sensor extends AbstractSensor implements IndependentConfigurableSensor, KeySensor {

		private IO127Sensor(final int sensorNr) {
			super(sensorNr);
		}

		public void addActor(final Actor target) throws IOException {
			addInputTargetRaw(sensorNr, target.getModuleAddr(), target.getActorNr());
		}

		@Override
		public InputMode getInputMode() throws IOException {
			switch (readVariable("input[" + sensorNr + "].type")) {
			case 1:
				return InputMode.UP;
			case 2:
				return InputMode.DOWN;
			default:
				return InputMode.TOGGLE;
			}
		}

		public int getModuleAddr() {
			return deviceAddr;
		}

		public Collection<Actor> listAssignedActors() throws IOException {
			return listAssignedActorsRaw(sensorNr);
		}

		public void removeActor(final Actor target) throws IOException {
			removeInputTargetRaw(sensorNr, target.getModuleAddr(), target.getActorNr());
		}

		@Override
		public void setInputMode(final InputMode inputMode) throws IOException {
			switch (inputMode) {
			case UP:
				writeVariable("input[" + sensorNr + "].type", 0x01);
				break;
			case DOWN:
				writeVariable("input[" + sensorNr + "].type", 0x02);
				break;
			case TOGGLE:
				writeVariable("input[" + sensorNr + "].type", 0xff);
				break;
			}

		}

		@Override
		public String toString() {
			return "IO127-" + Integer.toHexString(deviceAddr) + "-" + (sensorNr + 1);
		}
	}

	private static final int										ACTOR_COUNT					= 7;

	private static final Map<String, ActorType>	actors							= new TreeMap<String, ActorType>();

	private static final int										SENSOR_COUNT				= 12;

	private static final String									SENSOR_TYPE_PUSH		= "push";

	private static final String									SENSOR_TYPE_SWITCH	= "switch";

	static {
		for (int i = 0; i < IO127.ACTOR_COUNT; i += 1)
			IO127.actors.put(Integer.toString(i + 1), ActorType.HIGH_VOLTAGE);
	}

	public static List<ModuleType> getAvailableConfig() {
		final List<ModuleType> versions = new LinkedList<ModuleType>();
		final ModuleType io127v1 = new ModuleType();
		io127v1.setHwVer(new HwVer((byte) 7, (byte) 0));
		io127v1.setSwVer(new SwVer((byte) 1, (byte) 0));
		io127v1.setName("HS485 IO127");
		io127v1.setEepromSize(512);
		io127v1.setImplementingClass(IO127.class);
		io127v1.setWidth(4);

		final ModuleType io127v101 = new ModuleType();
		io127v101.setHwVer(new HwVer((byte) 7, (byte) 0));
		io127v101.setSwVer(new SwVer((byte) 1, (byte) 1));
		io127v101.setName("HS485 IO127");
		io127v101.setEepromSize(512);
		io127v101.setImplementingClass(IO127.class);
		io127v101.setWidth(4);

		final ArrayVariable inputConfig = new ArrayVariable();
		inputConfig.setAddress(0);
		inputConfig.setCount(IO127.SENSOR_COUNT);
		inputConfig.setStep(1);
		inputConfig.setName("input");
		inputConfig.setReload(true);
		final ChoiceVariable inputType = new ChoiceVariable();
		inputType.setAddress(0);
		inputType.setLength(1);
		inputType.setName("type");
		inputType.addChoiceEntry(new ChoiceEntry(0xff, "toggle", ""));
		inputType.addChoiceEntry(new ChoiceEntry(0x01, "up", ""));
		inputType.addChoiceEntry(new ChoiceEntry(0x02, "down", ""));
		inputConfig.addComponent(inputType);
		final ChoiceVariable sensorType = new ChoiceVariable();
		sensorType.setAddress(0x0028);
		sensorType.setLength(1);
		sensorType.setName("sensor-type");
		sensorType.addChoiceEntry(new ChoiceEntry(0xff, IO127.SENSOR_TYPE_PUSH, ""));
		sensorType.addChoiceEntry(new ChoiceEntry(0x01, IO127.SENSOR_TYPE_SWITCH, ""));
		inputConfig.addComponent(sensorType);
		io127v1.addVariable(inputConfig);
		io127v101.addVariable(inputConfig);

		final ArrayVariable outputTime = new ArrayVariable();
		outputTime.setAddress(0x0c);
		outputTime.setCount(IO127.ACTOR_COUNT);
		outputTime.setStep(2);
		outputTime.setReload(true);
		outputTime.setName("output-time");
		final NumberVariable time = new NumberVariable();
		time.setName("time");
		time.setAddress(0);
		time.setLength(2);
		time.setMinValue(0);
		time.setMaxValue(65534);
		time.setDefaultValue(0);
		outputTime.addComponent(time);
		io127v1.addVariable(outputTime);
		io127v101.addVariable(outputTime);

		final ArrayVariable outputConfig = new ArrayVariable();
		outputConfig.setAddress(0x1a);
		outputConfig.setCount(7);
		outputConfig.setStep(1);
		outputConfig.setReload(true);
		outputConfig.setName("output");
		final ChoiceVariable outputToggle = new ChoiceVariable();
		outputToggle.setAddress(0);
		outputToggle.setLength(1);
		outputToggle.setName("toggle");
		outputToggle.addChoiceEntry(new ChoiceEntry(0xff, "on", ""));
		outputToggle.addChoiceEntry(new ChoiceEntry(0x00, "off", ""));
		outputConfig.addComponent(outputToggle);
		final ChoiceVariable outputTimer = new ChoiceVariable();
		outputTimer.setAddress(0x21 - 0x1a);
		outputTimer.setLength(1);
		outputTimer.setName("timer-mode");
		outputTimer.addChoiceEntry(new ChoiceEntry(0xff, "none", ""));
		outputTimer.addChoiceEntry(new ChoiceEntry(0x01, "staircase", ""));
		outputTimer.addChoiceEntry(new ChoiceEntry(0x02, "auto-off", ""));
		outputConfig.addComponent(outputTimer);
		io127v1.addVariable(outputConfig);
		io127v101.addVariable(outputConfig);

		final ArrayVariable targetConfig = AbstractDevice.defaultTargetConfig();
		io127v1.addVariable(targetConfig);
		io127v101.addVariable(targetConfig);

		versions.add(io127v1);
		versions.add(io127v101);

		return versions;
	}

	private LinkedList<Actor>							actorList;

	private LinkedList<PhysicallySensor>	sensorList;

	public Actor getActor(final int actorNr) throws IOException {
		loadActors();
		return actorList.get(actorNr - 12);
	}

	@Override
	public int getActorCount() {
		return ACTOR_COUNT;
	}

	public PhysicallySensor getSensor(final int sensorNr) throws IOException {
		loadSensorList();
		for (final PhysicallySensor physicallySensor : sensorList)
			if (physicallySensor.getSensorNr() == sensorNr)
				return physicallySensor;
		return null;
	}

	public synchronized Collection<Actor> listActors() throws IOException {
		loadActors();
		return new ArrayList<Actor>(actorList);
	}

	public synchronized Collection<Sensor> listSensors() throws IOException {
		loadSensorList();
		return new ArrayList<Sensor>(sensorList);
	}

	@Override
	public String toString() {
		return "IO127-" + super.toString();
	}

	private synchronized void loadActors() {
		if (actorList == null) {
			actorList = new LinkedList<Actor>();
			for (int i = 12; i < 19; i++)
				actorList.add(new IO127Actor(i));
		}
	}

	private void loadSensorList() throws IOException {
		if (sensorList == null) {
			sensorList = new LinkedList<PhysicallySensor>();
			for (int i = 0; i < IO127.SENSOR_COUNT; i += 1)
				sensorList.add(new IO127Sensor(i));
		}
	}
}
