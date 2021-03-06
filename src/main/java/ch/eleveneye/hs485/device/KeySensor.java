package ch.eleveneye.hs485.device;

import java.io.IOException;
import java.util.Collection;

import ch.eleveneye.hs485.api.MessageHandler;
import ch.eleveneye.hs485.device.physically.Actor;

public interface KeySensor extends Sensor {
	void addActor(Actor target) throws IOException;

	Collection<Actor> listAssignedActors() throws IOException;

	void registerHandler(MessageHandler handler) throws IOException;

	void removeActor(Actor target) throws IOException;

	void removeAllActors() throws IOException;

}
