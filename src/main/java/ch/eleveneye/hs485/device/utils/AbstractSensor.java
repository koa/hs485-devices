package ch.eleveneye.hs485.device.utils;

import java.io.IOException;

import ch.eleveneye.hs485.device.KeySensor;
import ch.eleveneye.hs485.device.physically.Actor;
import ch.eleveneye.hs485.device.physically.PhysicallySensor;

public abstract class AbstractSensor implements PhysicallySensor, KeySensor {

	protected int sensorNr;

	public AbstractSensor(final int sensorNr) {
		this.sensorNr = sensorNr;
	}

	@Override
	public int getSensorNr() {
		return sensorNr;
	}

	@Override
	public String toString() {
		return "Sensor " + Integer.toHexString(getModuleAddr()) + ":"
				+ sensorNr;
	}

	public int getPairNr() {
		return sensorNr / 2;
	}

	@Override
	public void removeAllActors() throws IOException {
		for (Actor actor : listAssignedActors()) {
			removeActor(actor);
		}
	}

}
