package ch.eleveneye.hs485.devices.test;

import java.io.IOException;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eleveneye.hs485.api.HS485;
import ch.eleveneye.hs485.device.Dimmer;
import ch.eleveneye.hs485.device.Registry;
import ch.eleveneye.hs485.device.SwitchingActor;
import ch.eleveneye.hs485.device.config.PairMode;
import ch.eleveneye.hs485.device.physically.Actor;
import ch.eleveneye.hs485.device.physically.PairedSensorDevice;
import ch.eleveneye.hs485.device.physically.PhysicallyDevice;

public class RegistryTest {

	private static HS485	bus;
	private static Logger	log	= LoggerFactory.getLogger(RegistryTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// final URL resourceURL =
		// RegistryTest.class.getResource("/ch/eleveneye/hs485/config/logging.properties");
		// PropertyConfigurator.configure(resourceURL);
		// Logger.getRootLogger().setLevel(Level.INFO);
		// bus = new HS485("/dev/ttyUSB0", 3);
	}

	@Test
	public void testRegistry() throws IOException, InterruptedException {
		if (true)
			return;
		final Registry registry = new Registry(bus);

		// registry.getDevice(0xf77).setInputPairMode(1, PairMode.JOINT);
		// registry.getSensor(0xf77, 2).addActor(registry.getActor(0x23d, 0));
		// registry.getDevice(0xf77).commit();

		final Collection<PhysicallyDevice> physicallyDevices = registry.listPhysicalDevices();
		for (final PhysicallyDevice physicallyDevice : physicallyDevices) {
			// Device device = registry.getDevice(0xf77);
			if (physicallyDevice instanceof PairedSensorDevice) {
				final PairedSensorDevice pairedDevice = (PairedSensorDevice) physicallyDevice;
				for (int i = 0; i < pairedDevice.getInputPairCount(); i++)
					pairedDevice.setInputPairMode(i, PairMode.JOINT);
				physicallyDevice.commit();

			}
			// System.out.println(device);
			// device.dumpVariables();

			/*
			 * Collection<Sensor> sensors = device.listSensors(); for (Sensor sensor :
			 * sensors) { System.out.println(" -" + sensor); for (Actor act :
			 * sensor.listAssignedActors()) System.out.println(" -" + act); }
			 * System.out.println("---");
			 */
			final Collection<Actor> physicallyActors = physicallyDevice.listActors();
			for (final Actor physicallyActor : physicallyActors)
				if (physicallyActor instanceof Dimmer) {
					final Dimmer dimmer = (Dimmer) physicallyActor;
					log.info(" -" + physicallyActor + ", " + Integer.toHexString(dimmer.getDimmValue()));
				} else if (physicallyActor instanceof SwitchingActor)
					log.info(" -" + physicallyActor + ", " + (((SwitchingActor) physicallyActor).isOn() ? "On" : "Off"));
		}
		/*
		 * for (Device device : devices) { Collection<Actor> actors =
		 * device.listActors(); for (Actor actor : actors) { actor.setOff(); } }
		 */

	}

}
