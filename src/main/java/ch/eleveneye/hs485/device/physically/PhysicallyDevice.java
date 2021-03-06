package ch.eleveneye.hs485.device.physically;

import java.io.IOException;
import java.util.List;

import ch.eleveneye.hs485.device.Device;
import ch.eleveneye.hs485.device.Registry;
import ch.eleveneye.hs485.device.config.ConfigurableInputDescription;
import ch.eleveneye.hs485.device.config.ConfigurableOutputDescription;
import ch.eleveneye.hs485.memory.ModuleType;

public interface PhysicallyDevice extends Device {

	void commit() throws IOException;

	Actor getActor(int actorNr) throws IOException;

	int getActorCount();

	int getAddress();

	PhysicallySensor getSensor(int sensorNr) throws IOException;

	void init(int deviceAddr, Registry registry, ModuleType config);

	List<ConfigurableInputDescription> listConfigurableInputs();

	List<ConfigurableOutputDescription> listConfigurableOutputs();

	void reset() throws IOException;

	void rollback() throws IOException;

	// void dumpVariables() throws IOException;

}
