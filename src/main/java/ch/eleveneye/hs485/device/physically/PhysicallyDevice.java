package ch.eleveneye.hs485.device.physically;

import java.io.IOException;

import ch.eleveneye.hs485.device.Device;
import ch.eleveneye.hs485.device.Registry;
import ch.eleveneye.hs485.device.config.PairMode;
import ch.eleveneye.hs485.memory.ModuleType;

public interface PhysicallyDevice extends Device {

	public void commit() throws IOException;

	public Actor getActor(int actorNr) throws IOException;

	public int getActorCount();

	public int getAddress();

	public int getInputPairCount();

	public PairMode getInputPairMode(int pairNr) throws IOException;

	public PhysicallySensor getSensor(int sensorNr) throws IOException;

	public void init(int deviceAddr, Registry registry, ModuleType config);

	public void setInputPairMode(int pairNr, PairMode mode) throws IOException;

	// public void dumpVariables() throws IOException;

}
