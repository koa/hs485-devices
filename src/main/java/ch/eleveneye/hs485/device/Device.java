package ch.eleveneye.hs485.device;

import java.io.IOException;
import java.util.Collection;

import ch.eleveneye.hs485.device.physically.Actor;

public interface Device {

	public Collection<Actor> listActors() throws IOException;

	public Collection<Sensor> listSensors() throws IOException;
}
