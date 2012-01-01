package ch.eleveneye.hs485.device;

import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eleveneye.hs485.api.HS485;
import ch.eleveneye.hs485.api.HS485Factory;
import ch.eleveneye.hs485.api.data.HwVer;
import ch.eleveneye.hs485.api.data.SwVer;
import ch.eleveneye.hs485.device.physically.Actor;
import ch.eleveneye.hs485.device.physically.HS485D;
import ch.eleveneye.hs485.device.physically.HS485S;
import ch.eleveneye.hs485.device.physically.IO127;
import ch.eleveneye.hs485.device.physically.PhysicallyDevice;
import ch.eleveneye.hs485.device.physically.PhysicallySensor;
import ch.eleveneye.hs485.device.physically.TFS;
import ch.eleveneye.hs485.memory.ModuleType;

public class Registry {

	public static class ModuleDescription {
		private int	width;

		/**
		 * @return the width
		 */
		public int getWidth() {
			return width;
		}

		/**
		 * @param width
		 *          the width to set
		 */
		public void setWidth(final int width) {
			this.width = width;
		}
	}

	protected static List<ModuleType>	configRegistry	= new LinkedList<ModuleType>();
	private static Logger							log							= LoggerFactory.getLogger(Registry.class);

	static {
		Registry.configRegistry.addAll(HS485D.getAvailableConfig());
		Registry.configRegistry.addAll(HS485S.getAvailableConfig());
		Registry.configRegistry.addAll(IO127.getAvailableConfig());
		Registry.configRegistry.addAll(TFS.getAvailableConfig());
	}

	public static Collection<ModuleType> listAvailableModules() {
		return Collections.unmodifiableList(Registry.configRegistry);
	}

	public static Map<String, Set<Class<PhysicallyDevice>>> listModuleClasses() {
		final Map<String, Set<Class<PhysicallyDevice>>> ret = new HashMap<String, Set<Class<PhysicallyDevice>>>();
		for (final ModuleType nextModule : Registry.configRegistry) {
			Set<Class<PhysicallyDevice>> set = ret.get(nextModule.getName());
			if (set == null) {
				set = new HashSet<Class<PhysicallyDevice>>();
				ret.put(nextModule.getName(), set);
			}
			set.add(nextModule.getImplementingClass());
		}
		return ret;
	}

	public static Map<String, ModuleDescription> listModuleDescription() {
		final Map<String, ModuleDescription> ret = new HashMap<String, ModuleDescription>();
		final ModuleDescription centralDescription = new ModuleDescription();
		centralDescription.setWidth(1);
		ret.put("Zentrale", centralDescription);
		for (final ModuleType nextModule : Registry.configRegistry) {
			ModuleDescription moduleDescription = ret.get(nextModule.getName());
			if (moduleDescription == null) {
				moduleDescription = new ModuleDescription();
				ret.put(nextModule.getName(), moduleDescription);
			}
			moduleDescription.setWidth(nextModule.getWidth());
		}
		return ret;
	}

	protected static ModuleType findModule(final HwVer hwVer, final SwVer swVer) {
		for (final ModuleType mod : Registry.configRegistry)
			if (mod.getHwVer().equals(hwVer) && mod.getSwVer().equals(swVer))
				return mod;
		return null;
	}

	protected Map<Integer, PhysicallyDevice>	foundDevices	= null;

	HS485																			bus;

	public Registry() throws UnsupportedCommOperationException, IOException {
		bus = HS485Factory.getInstance().getHS485();
	}

	public Registry(final HS485 bus) {
		this.bus = bus;
	}

	public void commit() throws IOException {
		for (final PhysicallyDevice dev : listPhysicalDevices())
			dev.commit();
	}

	public synchronized <T> T doInTransaction(final Callable<T> callable) throws IOException {
		try {
			final T ret = callable.call();
			commit();
			return ret;
		} catch (final Throwable e) {
			log.warn("Error in Transaction", e);
			rollback();
			throw new RuntimeException(e);
		}
	}

	public synchronized Actor getActor(final int address, final int actorNr) throws IOException {
		loadPhysicallyDevices();
		return getPhysicallyDevice(address).getActor(actorNr);
	}

	public HS485 getBus() {
		return bus;
	}

	public PhysicallyDevice getPhysicallyDevice(final int address) throws IOException {
		loadPhysicallyDevices();
		PhysicallyDevice device = foundDevices.get(address);
		if (device == null) {
			device = identifyModule(address);
			foundDevices.put(address, device);
		}
		return device;
	}

	public synchronized PhysicallySensor getPhysicallySensor(final int address, final int sensorNr) throws IOException {
		loadPhysicallyDevices();
		return getPhysicallyDevice(address) == null ? null : foundDevices.get(address).getSensor(sensorNr);
	}

	public synchronized Collection<PhysicallyDevice> listPhysicalDevices() throws IOException {
		loadPhysicallyDevices();
		return foundDevices.values();
	}

	public synchronized Collection<Actor> listPhysicallyActors() throws IOException {
		final LinkedList<Actor> ret = new LinkedList<Actor>();
		for (final PhysicallyDevice dev : listPhysicalDevices())
			for (final Actor act : dev.listActors())
				ret.add(act);
		return ret;
	}

	public synchronized Collection<PhysicallySensor> listPhysicallySensors() throws IOException {
		final LinkedList<PhysicallySensor> ret = new LinkedList<PhysicallySensor>();
		for (final PhysicallyDevice dev : listPhysicalDevices())
			for (final Sensor sen : dev.listSensors())
				ret.add((PhysicallySensor) sen);
		return ret;
	}

	public synchronized void reloadDevices() {
		foundDevices = null;
		bus.removeHandlers();
	}

	public synchronized void resetAllDevices() throws IOException {
		for (final PhysicallyDevice dev : listPhysicalDevices())
			dev.reset();
		bus.removeHandlers();
	}

	public void rollback() throws IOException {
		for (final PhysicallyDevice dev : listPhysicalDevices())
			dev.rollback();

	}

	private PhysicallyDevice identifyModule(final Integer clientAddr) throws IOException {
		PhysicallyDevice moduleImpl = null;
		final HwVer hwVer = bus.readHwVer(clientAddr);
		final SwVer swVer = bus.readSwVer(clientAddr);
		try {
			final ModuleType moduleDescr = Registry.findModule(hwVer, swVer);
			if (moduleDescr == null)
				Registry.log.warn("Keine Implementation für Modul: " + Integer.toHexString(clientAddr) + ", Version: " + hwVer + ":" + swVer + " gefunden");
			else {
				Registry.log.info("Typ: " + moduleDescr.getName());
				moduleImpl = (PhysicallyDevice) moduleDescr.getImplementingClass().newInstance();
				moduleImpl.init(clientAddr, this, moduleDescr);
			}
		} catch (final InstantiationException e) {
			Registry.log.error("Konnte Klasse für Modul: " + Integer.toHexString(clientAddr) + ", Version: " + hwVer + ":" + swVer + " nicht instanzieren",
					e);
		} catch (final IllegalAccessException e) {
			Registry.log.error("Konnte Klasse für Modul: " + Integer.toHexString(clientAddr) + ", Version: " + hwVer + ":" + swVer + " nicht instanzieren",
					e);
		}
		return moduleImpl;
	}

	private synchronized void loadPhysicallyDevices() throws IOException {
		if (foundDevices == null) {
			foundDevices = new TreeMap<Integer, PhysicallyDevice>();
			final List<Integer> clients = bus.listClients();
			for (final Integer clientAddr : clients) {
				final PhysicallyDevice moduleImpl = identifyModule(clientAddr);
				if (moduleImpl != null)
					foundDevices.put(clientAddr, moduleImpl);
			}
		}
	}

}
