package ch.eleveneye.hs485.device;

import java.io.IOException;
import java.util.Collection;

import ch.eleveneye.hs485.device.physically.Actor;

public interface KeySensor extends Sensor {
	public void addActor(Actor target) throws IOException;

	public Collection<Actor> listAssignedActors() throws IOException;

	public void removeActor(Actor target) throws IOException;

	public void removeAllActors() throws IOException;

}
