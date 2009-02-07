package ch.eleveneye.hs485.device.config;

import java.util.Map;

import javax.swing.JPanel;

import ch.eleveneye.hs485.device.ActorType;
import ch.eleveneye.hs485.device.SensorType;

public interface ConfigData extends Cloneable {
	public ConfigData clone() throws CloneNotSupportedException;

	public Map<String, ActorType> connectableActors();

	public Map<String, SensorType> connectableSensors();

	public Map<String, ActorType> fixedActors();

	public Map<String, SensorType> fixedSensors();

	public void showUI(JPanel panel);
}
