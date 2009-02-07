package ch.eleveneye.hs485.device.physically;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import ch.eleveneye.hs485.device.ActorType;
import ch.eleveneye.hs485.device.KeySensor;
import ch.eleveneye.hs485.device.Registry;
import ch.eleveneye.hs485.device.Sensor;
import ch.eleveneye.hs485.device.SensorType;
import ch.eleveneye.hs485.device.TimedActor;
import ch.eleveneye.hs485.device.config.ConfigData;
import ch.eleveneye.hs485.device.config.InputPairConfig;
import ch.eleveneye.hs485.device.config.PairMode;
import ch.eleveneye.hs485.device.config.TimeMode;
import ch.eleveneye.hs485.device.config.InputPairConfig.InputConfig;
import ch.eleveneye.hs485.device.utils.AbstractActor;
import ch.eleveneye.hs485.device.utils.AbstractDevice;
import ch.eleveneye.hs485.device.utils.AbstractSensor;
import ch.eleveneye.hs485.memory.ArrayVariable;
import ch.eleveneye.hs485.memory.ChoiceEntry;
import ch.eleveneye.hs485.memory.ChoiceVariable;
import ch.eleveneye.hs485.memory.ModuleType;
import ch.eleveneye.hs485.memory.NumberVariable;
import ch.eleveneye.hs485.memory.ModuleType.ConfigBuilder;
import ch.eleveneye.hs485.protocol.IMessage;
import ch.eleveneye.hs485.protocol.data.HwVer;
import ch.eleveneye.hs485.protocol.data.SwVer;

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

		public void setTimeMode(TimeMode value) throws IOException {
			String variableName = "output[" + (actorNr - 12) + "].timer-mode";
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

		public void setTimeValue(int value) throws IOException {
			writeVariable("output-time[" + (actorNr - 12) + "].time", value);
		}

		public void setToggleBit(boolean value) throws IOException {
			writeVariable("output[" + (actorNr - 12) + "].toggle", value ? 0xff
					: 0x00);
		}

		public void toggle() throws IOException {
			bus.writeActor(deviceAddr, (byte) actorNr, (byte) 0xff);
		}

		@Override
		public String toString() {
			return "IO127-Actor " + Integer.toHexString(getModuleAddr()) + ":"
					+ (actorNr - 11);
		}
	}

	public static class IO127Config implements ConfigData {

		protected InputPairConfig pairConfig[];

		@Override
		public ConfigData clone() throws CloneNotSupportedException {
			IO127Config ret = new IO127Config();
			if (pairConfig == null)
				ret.pairConfig = null;
			else {
				ret.pairConfig = new InputPairConfig[pairConfig.length];
				for (int i = 0; i < pairConfig.length; i++) {
					ret.pairConfig[i] = (InputPairConfig) pairConfig[i].clone();
				}
			}
			return ret;
		}

		public Map<String, ActorType> connectableActors() {
			return IO127.actors;
		}

		public Map<String, SensorType> connectableSensors() {
			Map<String, SensorType> ret = new TreeMap<String, SensorType>();
			for (int i = 0; i < pairConfig.length; i++) {
				pairConfig[i].appendSensors(i * 2 + 1, ret);
			}
			return ret;
		}

		public Map<String, ActorType> fixedActors() {
			return new TreeMap<String, ActorType>();
		}

		public Map<String, SensorType> fixedSensors() {
			return new TreeMap<String, SensorType>();
		}

		public void showUI(JPanel panel) {
			panel.removeAll();
			panel.setLayout(new GridBagLayout());
			for (int i = 0; i < pairConfig.length; i++) {
				JPanel pairPanel = new JPanel();
				pairConfig[i].showUI(pairPanel);
				pairPanel.setBorder(BorderFactory.createTitledBorder(null,
						"Paar " + (i + 1), TitledBorder.DEFAULT_JUSTIFICATION,
						TitledBorder.DEFAULT_POSITION, null, null));
				Insets insets = new Insets(2, 2, 2, 2);
				panel.add(pairPanel, new GridBagConstraints(i % 2, i / 2, 1, 1,
						0.1, 0.1, GridBagConstraints.WEST,
						GridBagConstraints.BOTH, insets, 0, 0));

			}
		}
	}

	private static final class IO127ConfigBuilder implements ConfigBuilder {
		public Collection<Integer> listAvailableModules(Registry bus)
				throws IOException {
			TreeSet<Integer> ret = new TreeSet<Integer>();
			for (PhysicallyDevice device : bus.listPhysicalDevices()) {
				if (device instanceof IO127) {
					IO127 dev = (IO127) device;
					ret.add(dev.deviceAddr);
				}
			}
			return ret;
		}

		public ConfigData makeNewConfigData() {
			IO127Config ret = new IO127Config();
			ret.pairConfig = new InputPairConfig[6];
			for (int i = 0; i < ret.pairConfig.length; i++) {
				InputPairConfig currentPairConfig = new InputPairConfig(null);
				currentPairConfig.setJointInput(true);
				currentPairConfig.setInputChoices(IO127.CHOICES);
				currentPairConfig.setInput1Type(0);
				currentPairConfig.setInput2Type(0);
				ret.pairConfig[i] = currentPairConfig;
			}
			return ret;
		}
	}

	private final class IO127Sensor extends AbstractSensor implements
			PairableSensor, KeySensor {

		private IO127Sensor(final int sensorNr) {
			super(sensorNr);
		}

		public void addActor(final Actor target) throws IOException {
			if (isPaired()) {
				addInputTargetRaw(sensorNr, target.getModuleAddr(), target
						.getActorNr());
				addInputTargetRaw(sensorNr + 1, target.getModuleAddr(), target
						.getActorNr());
			} else
				addInputTargetRaw(sensorNr, target.getModuleAddr(), target
						.getActorNr());
		}

		public int getModuleAddr() {
			return deviceAddr;
		}

		public boolean isPaired() throws IOException {
			return sensorNr % 2 == 0 && readInputType(sensorNr / 2) != 0xff;
		}

		public Collection<Actor> listAssignedActors() throws IOException {
			return listAssignedActorsRaw(sensorNr);
		}

		public void removeActor(final Actor target) throws IOException {
			if (isPaired()) {
				removeInputTargetRaw(sensorNr, target.getModuleAddr(), target
						.getActorNr());
				removeInputTargetRaw(sensorNr + 1, target.getModuleAddr(),
						target.getActorNr());
			} else
				removeInputTargetRaw(sensorNr, target.getModuleAddr(), target
						.getActorNr());
		}

		@Override
		public String toString() {
			try {
				return "IO127-"
						+ Integer.toHexString(deviceAddr)
						+ (isPaired() ? ("-" + (sensorNr + 1) + "/" + (sensorNr + 2))
								: ("-" + (sensorNr + 1)));
			} catch (IOException e) {
				return "IO127-" + Integer.toHexString(deviceAddr) + "-"
						+ (sensorNr + 1);
			}
		}
	}

	private static final int ACTOR_COUNT = 7;

	private static final Map<String, ActorType> actors = new TreeMap<String, ActorType>();

	private static final InputConfig[] CHOICES = new InputConfig[] {
			new InputConfig("Taster", SensorType.TWO_WIRE, SensorType.ONE_WIRE),
			new InputConfig("Schalter", SensorType.TWO_WIRE,
					SensorType.ONE_WIRE) };

	private static final int SENSOR_PAIR_COUNT = 6;

	private static final String SENSOR_TYPE_PUSH = "push";

	private static final String SENSOR_TYPE_SWITCH = "switch";

	static {
		for (int i = 0; i < IO127.ACTOR_COUNT; i += 1) {
			IO127.actors.put(Integer.toString(i + 1), ActorType.HIGH_VOLTAGE);
		}
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
		io127v1.setConfigBuilder(new IO127ConfigBuilder());

		final ModuleType io127v101 = new ModuleType();
		io127v101.setHwVer(new HwVer((byte) 7, (byte) 0));
		io127v101.setSwVer(new SwVer((byte) 1, (byte) 1));
		io127v101.setName("HS485 IO127");
		io127v101.setEepromSize(512);
		io127v101.setImplementingClass(IO127.class);
		io127v101.setWidth(4);
		io127v101.setConfigBuilder(new IO127ConfigBuilder());

		final ArrayVariable inputConfig = new ArrayVariable();
		inputConfig.setAddress(0);
		inputConfig.setCount(IO127.SENSOR_PAIR_COUNT * 2);
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
		sensorType.addChoiceEntry(new ChoiceEntry(0xff, IO127.SENSOR_TYPE_PUSH,
				""));
		sensorType.addChoiceEntry(new ChoiceEntry(0x01,
				IO127.SENSOR_TYPE_SWITCH, ""));
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

	private LinkedList<Actor> actorList;

	private LinkedList<PhysicallySensor> sensorList;

	public void consumeBusEvent(IMessage message) {
		// TODO Auto-generated method stub

	}

	public Actor getActor(final int actorNr) throws IOException {
		loadActors();
		return actorList.get(actorNr - 12);
	}

	public ConfigData getConfig() throws IOException {
		IO127Config ret = new IO127Config();
		ret.pairConfig = new InputPairConfig[6];
		for (int i = 0; i < ret.pairConfig.length; i++) {
			InputPairConfig currentPairConfig = new InputPairConfig(null);
			currentPairConfig
					.setJointInput(getInputPairMode(i) == PairMode.JOINT);
			currentPairConfig.setInputChoices(IO127.CHOICES);
			currentPairConfig.setInput1Type(readVariableResolved(
					"input[" + (i * 2) + "].sensor-type").equals(
					IO127.SENSOR_TYPE_SWITCH) ? 1 : 0);
			currentPairConfig.setInput2Type(readVariableResolved(
					"input[" + (i * 2 + 1) + "].sensor-type").equals(
					IO127.SENSOR_TYPE_SWITCH) ? 1 : 0);
			ret.pairConfig[i] = currentPairConfig;
		}
		return ret;
	}

	public int getInputPairCount() {
		return 6;
	}

	public PairMode getInputPairMode(int pairNr) throws IOException {
		return readInputType(pairNr) != 0xff ? PairMode.JOINT : PairMode.SPLIT;
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
			for (int i = 0; i < IO127.SENSOR_PAIR_COUNT; i += 1) {
				final int sensorConfig = readInputType(i);
				if (sensorConfig == 0xff) { // toggle-input
					sensorList.add(new IO127Sensor(i * 2));
					sensorList.add(new IO127Sensor(i * 2 + 1));
				} else
					sensorList.add(new IO127Sensor(i * 2));
			}
		}
	}

	private int readInputType(final int i) throws IOException {
		return readVariable("input[" + i * 2 + "].type");
	}

	public void setConfig(ConfigData newConfig) throws IOException {
		IO127Config config = (IO127Config) newConfig;
		for (int i = 0; i < config.pairConfig.length; i++) {
			InputPairConfig currentPairConfig = config.pairConfig[i];
			setInputPairMode(i,
					currentPairConfig.isJointInput() ? PairMode.JOINT
							: PairMode.SPLIT);
			int input1Config = currentPairConfig.getInput1Type();
			writeChoice("input[" + (i * 2) + "].sensor-type",
					input1Config == 0 ? IO127.SENSOR_TYPE_PUSH
							: IO127.SENSOR_TYPE_SWITCH);
			int input2Config = currentPairConfig.isJointInput() ? currentPairConfig
					.getInput1Type()
					: currentPairConfig.getInput2Type();
			writeChoice("input[" + (i * 2 + 1) + "].sensor-type",
					input2Config == 0 ? IO127.SENSOR_TYPE_PUSH
							: IO127.SENSOR_TYPE_SWITCH);
		}
	}

	public synchronized void setInputPairMode(final int pairNr,
			final PairMode mode) throws IOException {
		switch (mode) {
		case JOINT:
			writeVariable("input[" + pairNr * 2 + "].type", 0x02);
			writeVariable("input[" + (pairNr * 2 + 1) + "].type", 0x01);
			break;
		case SPLIT:
			writeVariable("input[" + pairNr * 2 + "].type", 0xff);
			writeVariable("input[" + (pairNr * 2 + 1) + "].type", 0xff);
			break;
		}
		sensorList = null;
	}

	@Override
	public String toString() {
		return "IO127-" + super.toString();
	}
}
