package ch.eleveneye.hs485.device;

import java.io.IOException;
import java.util.Collection;

import ch.eleveneye.hs485.device.config.ConfigData;
import ch.eleveneye.hs485.device.physically.Actor;

public interface Device {
	public ConfigData getConfig() throws IOException;

	public Collection<Actor> listActors() throws IOException;

	public Collection<Sensor> listSensors() throws IOException;

	public void setConfig(ConfigData newConfig) throws IOException;
}
