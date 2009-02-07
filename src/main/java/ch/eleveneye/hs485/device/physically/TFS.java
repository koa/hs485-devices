package ch.eleveneye.hs485.device.physically;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.eleveneye.hs485.device.ActorType;
import ch.eleveneye.hs485.device.Registry;
import ch.eleveneye.hs485.device.Sensor;
import ch.eleveneye.hs485.device.SensorType;
import ch.eleveneye.hs485.device.TFSensor;
import ch.eleveneye.hs485.device.config.ConfigData;
import ch.eleveneye.hs485.device.config.PairMode;
import ch.eleveneye.hs485.device.utils.AbstractDevice;
import ch.eleveneye.hs485.memory.ModuleType;
import ch.eleveneye.hs485.memory.ModuleType.ConfigBuilder;
import ch.eleveneye.hs485.protocol.data.HwVer;
import ch.eleveneye.hs485.protocol.data.SwVer;
import ch.eleveneye.hs485.protocol.data.TFSValue;

public class TFS extends AbstractDevice {

	private final static class TFSConfigData implements ConfigData {
		private static final TreeMap<String, SensorType> SENSORS = new TreeMap<String, SensorType>();
		static {
			TFSConfigData.SENSORS.put("sensor", SensorType.TEMP);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#clone()
		 */
		@Override
		public ConfigData clone() throws CloneNotSupportedException {
			return (ConfigData) super.clone();
		}

		public Map<String, ActorType> connectableActors() {
			return new TreeMap<String, ActorType>();
		}

		public Map<String, SensorType> connectableSensors() {
			return new TreeMap<String, SensorType>();
		}

		public Map<String, ActorType> fixedActors() {
			return new TreeMap<String, ActorType>();
		}

		public Map<String, SensorType> fixedSensors() {
			return TFSConfigData.SENSORS;
		}

		public void showUI(JPanel panel) {
			panel.removeAll();
			panel.setLayout(new BorderLayout());
			panel.add(new JLabel("TFS kennt keine Konfigurationsparameter"),
					BorderLayout.CENTER);
		}
	}

	protected class TFSensorImpl implements TFSensor, PhysicallySensor {

		LinkedList<Actor> registeredActors;

		public TFSensorImpl() {
			registeredActors = new LinkedList<Actor>();
		}

		public int getModuleAddr() {
			return deviceAddr;
		}

		public int getSensorNr() {
			return 0;
		}

		public Collection<Actor> listAssignedActors() throws IOException {
			return registeredActors;
		}

		public TFSValue readTF() throws IOException {
			return bus.readTemp(deviceAddr);
		}

	}

	public static Collection<ModuleType> getAvailableConfig() {
		ModuleType tfsv13 = new ModuleType();
		tfsv13.setEepromSize(512);
		tfsv13.setName("TFS");
		tfsv13.setHwVer(new HwVer((byte) 4, (byte) 0));
		tfsv13.setSwVer(new SwVer((byte) 1, (byte) 3));
		tfsv13.setImplementingClass(TFS.class);
		tfsv13.setWidth(1);
		tfsv13.setConfigBuilder(new ConfigBuilder() {
			public Collection<Integer> listAvailableModules(Registry bus)
					throws IOException {
				TreeSet<Integer> ret = new TreeSet<Integer>();
				for (PhysicallyDevice device : bus.listPhysicalDevices()) {
					if (device instanceof TFS) {
						TFS dev = (TFS) device;
						ret.add(dev.deviceAddr);
					}
				}
				return ret;
			}

			public ConfigData makeNewConfigData() {
				return new TFSConfigData();
			}
		});
		return Arrays.asList(new ModuleType[] { tfsv13 });
	}

	private final TFSensorImpl sensor;

	public TFS() {
		sensor = new TFSensorImpl();
	}

	public Actor getActor(int actorNr) throws IOException {
		return null;
	}

	public ConfigData getConfig() throws IOException {
		return new TFSConfigData();
	}

	public int getInputPairCount() {
		return 0;
	}

	public PairMode getInputPairMode(int pairNr) throws IOException {
		return null;
	}

	public PhysicallySensor getSensor(int sensorNr) throws IOException {
		return sensor;
	}

	public Collection<Actor> listActors() throws IOException {
		return new ArrayList<Actor>(0);
	}

	public Collection<Sensor> listSensors() throws IOException {
		return Arrays.asList(new Sensor[] { sensor });
	}

	public void setConfig(ConfigData newConfig) throws IOException {
		// wird nicht benötigt
	}

	public void setInputPairMode(int pairNr, PairMode mode) throws IOException {
	}

	@Override
	public String toString() {
		return "TFS-" + Integer.toHexString(deviceAddr);
	}

}
