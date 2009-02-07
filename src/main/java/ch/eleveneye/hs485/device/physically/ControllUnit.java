package ch.eleveneye.hs485.device.physically;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;

import ch.eleveneye.hs485.device.ActorType;
import ch.eleveneye.hs485.device.Device;
import ch.eleveneye.hs485.device.Registry;
import ch.eleveneye.hs485.device.Sensor;
import ch.eleveneye.hs485.device.SensorType;
import ch.eleveneye.hs485.device.config.ConfigData;
import ch.eleveneye.hs485.memory.ModuleType.ConfigBuilder;

public class ControllUnit implements Device {
	protected static class ControllUnitConfigBuilder implements ConfigBuilder {

		private static final int CONTROLL_UNIT_ADDRESS = 3;

		public Collection<Integer> listAvailableModules(Registry bus)
				throws IOException {
			return Arrays
					.asList(new Integer[] { ControllUnitConfigBuilder.CONTROLL_UNIT_ADDRESS });
		}

		public ConfigData makeNewConfigData() {
			return new ControllUnitConfigData();
		}

	}

	protected static class ControllUnitConfigData implements ConfigData {

		private static final Insets DEFAULT_INSETS = new Insets(2, 2, 2, 2);

		Map<String, TempRegulatorEntry> regulators = new HashMap<String, TempRegulatorEntry>();

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
			HashMap<String, ActorType> ret = new HashMap<String, ActorType>();
			for (Map.Entry<String, TempRegulatorEntry> regulatorEntry : regulators
					.entrySet()) {
				ret.put("t:" + regulatorEntry.getKey(), ActorType.TEMPERATURE);
			}
			return ret;
		}

		public Map<String, SensorType> fixedSensors() {
			HashMap<String, SensorType> ret = new HashMap<String, SensorType>();
			for (Map.Entry<String, TempRegulatorEntry> regulatorEntry : regulators
					.entrySet()) {
				ret.put("t:" + regulatorEntry.getKey(), SensorType.TWO_WIRE);
			}
			return ret;
		}

		/**
		 * @return the regulators
		 */
		public Map<String, TempRegulatorEntry> getRegulators() {
			return regulators;
		}

		public void showUI(JPanel panel) {
			panel.removeAll();
			panel.setLayout(new BorderLayout());
			ControllUnitPanel controllUnitPanel = new ControllUnitPanel(this);
			panel.add(controllUnitPanel);
		}

	}

	protected static class TempRegulatorEntry {
		private String actorAddress;
		private double hysterese;
		private double temp;

		/**
		 * @return the actorAddress
		 */
		public String getActorAddress() {
			return actorAddress;
		}

		/**
		 * @return the hysterese
		 */
		public double getHysterese() {
			return hysterese;
		}

		/**
		 * @return the temp
		 */
		public double getTemp() {
			return temp;
		}

		/**
		 * @param actorAddress
		 *            the actorAddress to set
		 */
		public void setActorAddress(String actorAddress) {
			this.actorAddress = actorAddress;
		}

		/**
		 * @param hysterese
		 *            the hysterese to set
		 */
		public void setHysterese(double hysterese) {
			this.hysterese = hysterese;
		}

		/**
		 * @param temp
		 *            the temp to set
		 */
		public void setTemp(double temp) {
			this.temp = temp;
		}
	}

	public static ConfigBuilder getConfigBuilder() {
		return null;
	}

	private ConfigData config;

	public ConfigData getConfig() throws IOException {
		return config;
	}

	public Collection<Actor> listActors() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Sensor> listSensors() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setConfig(ConfigData config) throws IOException {
		this.config = config;
	}

}
