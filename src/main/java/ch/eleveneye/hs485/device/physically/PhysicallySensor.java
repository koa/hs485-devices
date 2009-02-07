package ch.eleveneye.hs485.device.physically;

import ch.eleveneye.hs485.device.Sensor;

public interface PhysicallySensor extends Sensor {

	public int getModuleAddr();

	public int getSensorNr();

}
