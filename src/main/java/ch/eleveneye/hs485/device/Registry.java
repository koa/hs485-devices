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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eleveneye.hs485.api.HS485;
import ch.eleveneye.hs485.api.HS485Factory;
import ch.eleveneye.hs485.api.data.HwVer;
import ch.eleveneye.hs485.api.data.SwVer;
import ch.eleveneye.hs485.device.physically.Actor;
import ch.eleveneye.hs485.device.physically.ControllUnit;
import ch.eleveneye.hs485.device.physically.HS485D;
import ch.eleveneye.hs485.device.physically.HS485S;
import ch.eleveneye.hs485.device.physically.IO127;
import ch.eleveneye.hs485.device.physically.PhysicallyDevice;
import ch.eleveneye.hs485.device.physically.PhysicallySensor;
import ch.eleveneye.hs485.device.physically.TFS;
import ch.eleveneye.hs485.device.virtual.DimmActorData;
import ch.eleveneye.hs485.device.virtual.EventData;
import ch.eleveneye.hs485.device.virtual.EventSink;
import ch.eleveneye.hs485.device.virtual.KeyData;
import ch.eleveneye.hs485.device.virtual.SwitchActorData;
import ch.eleveneye.hs485.device.virtual.utils.DefaultEventSource;
import ch.eleveneye.hs485.event.EventHandler;
import ch.eleveneye.hs485.memory.ModuleType;
import ch.eleveneye.hs485.memory.ModuleType.ConfigBuilder;

public class Registry {

	public static class ModuleDescription {
		private ConfigBuilder	configBuilder;
		private int						width;

		/**
		 * @return the configBuilder
		 */
		public ConfigBuilder getConfigBuilder() {
			return configBuilder;
		}

		/**
		 * @return the width
		 */
		public int getWidth() {
			return width;
		}

		public void setConfigBuilder(final ConfigBuilder configBuilder) {
			this.configBuilder = configBuilder;
		}

		/**
		 * @param width
		 *          the width to set
		 */
		public void setWidth(final int width) {
			this.width = width;
		}
	}

	protected static final class VirtualKeyHandler implements EventHandler {
		private final DefaultEventSource<KeyData>	source;

		protected VirtualKeyHandler(final DefaultEventSource<KeyData> source) {
			this.source = source;
		}

		public void doEvent(final byte eventCode) throws IOException {
			final byte keyType = (byte) (eventCode >> 6 & 0x3);
			final byte eventType = (byte) (eventCode & 0x3);
			KeyData.Key key = null;
			switch (keyType) {
			case KEY_TYPE_DOWN:
				key = KeyData.Key.DOWN;
				break;
			case KEY_TYPE_UP:
				key = KeyData.Key.UP;
				break;
			case KEY_TYPE_TOGGLE:
				key = KeyData.Key.TOGGLE;
				break;
			}
			KeyData.Event event = null;
			switch (eventType) {
			case EVENT_TYPE_HOLDED:
				event = KeyData.Event.HOLD;
				break;
			case EVENT_TYPE_PRESSED:
				event = KeyData.Event.PRESS;
				break;
			case EVENT_TYPE_RELEASED:
				event = KeyData.Event.RELEASE;
				break;
			}
			source.fireEvent(new KeyData(key, event));
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
		centralDescription.setConfigBuilder(ControllUnit.getConfigBuilder());
		centralDescription.setWidth(1);
		ret.put("Zentrale", centralDescription);
		for (final ModuleType nextModule : Registry.configRegistry) {
			ModuleDescription moduleDescription = ret.get(nextModule.getName());
			if (moduleDescription == null) {
				moduleDescription = new ModuleDescription();
				ret.put(nextModule.getName(), moduleDescription);
			}
			moduleDescription.setWidth(nextModule.getWidth());
			moduleDescription.setConfigBuilder(nextModule.getConfigBuilder());
		}
		return ret;
	}

	protected static ModuleType findModule(final HwVer hwVer, final SwVer swVer) {
		for (final ModuleType mod : Registry.configRegistry)
			if (mod.getHwVer().equals(hwVer) && mod.getSwVer().equals(swVer))
				return mod;
		return null;
	}

	private static void takeKeyEvent(final SwitchingActor keyActor, final KeyData keyData) throws IOException {
		if (keyData.getEvent() == KeyData.Event.PRESS)
			switch (keyData.getKey()) {
			case UP:
				keyActor.setOn();
				break;
			case DOWN:
				keyActor.setOff();
				break;
			case TOGGLE:
				keyActor.toggle();
				break;
			}
	}

	private static void takeSwitchEvent(final SwitchingActor switchActor, final SwitchActorData switchData) throws IOException {
		switch (switchData.getEvent()) {
		case ON:
			switchActor.setOn();
			break;
		case OFF:
			switchActor.setOff();
			break;
		case TOGGLE:
			switchActor.toggle();
			break;
		}
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

	public synchronized Actor getActor(final int address, final int actorNr) throws IOException {
		loadPhysicallyDevices();
		return getPhysicallyDevice(address).getActor(actorNr);
	}

	public HS485 getBus() {
		return bus;
	}

	public synchronized EventSink getEventSink(final Actor actor) throws IOException {
		if (actor instanceof Dimmer) {
			final Dimmer dimmer = (Dimmer) actor;
			return new EventSink<EventData>() {

				public String getRoleName() {
					return "input";
				}

				public void takeEvent(final EventData data) {
					try {
						if (data instanceof DimmActorData) {
							final DimmActorData dimmData = (DimmActorData) data;
							dimmer.setDimmValue(dimmData.getDimmValue());
						} else if (data instanceof SwitchActorData)
							Registry.takeSwitchEvent(dimmer, (SwitchActorData) data);
						else if (data instanceof KeyData)
							Registry.takeKeyEvent(dimmer, (KeyData) data);
					} catch (final IOException e) {
						throw new RuntimeException(e);
					}
				}

			};
		} else if (actor instanceof SwitchingActor) {
			final SwitchingActor swi = (SwitchingActor) actor;
			return new EventSink<EventData>() {

				public String getRoleName() {
					return "input";
				}

				public void takeEvent(final EventData data) {
					try {
						if (data instanceof SwitchActorData)
							Registry.takeSwitchEvent(swi, (SwitchActorData) data);
						else if (data instanceof KeyData)
							Registry.takeKeyEvent(swi, (KeyData) data);
					} catch (final IOException e) {
						throw new RuntimeException(e);
					}
				}
			};
		}
		return null;
	}

	// public synchronized EventSource getEventSource(final PhysicallySensor
	// sensor) throws IOException {
	// if (sensor instanceof KeySensor) {
	// final KeySensor keySens = (KeySensor) sensor;
	// final Set<Integer> ownAddresses = new TreeSet<Integer>();
	// for (final int address : bus.listOwnAddresse())
	// ownAddresses.add(address);
	//
	// for (final Actor act : keySens.listAssignedActors())
	// if (act instanceof VirtualActor) {
	// final VirtualActor virtActor = (VirtualActor) act;
	// final Map<Byte, VirtualActor> moduleMap =
	// virtualKeyActors.get(virtActor.getModuleAddr());
	// if (moduleMap == null)
	// continue;
	// if (moduleMap.get(virtActor.getActorNr()) == null)
	// continue;
	// return virtActor.getEventSource();
	// }
	// for (final int address : ownAddresses) {
	// Map<Byte, VirtualActor> moduleMap = virtualKeyActors.get(address);
	// if (moduleMap == null) {
	// moduleMap = new HashMap<Byte, VirtualActor>();
	// virtualKeyActors.put(address, moduleMap);
	// }
	// for (int actorNr = 0; actorNr < 255; actorNr++)
	// if (!moduleMap.containsKey(new Byte((byte) actorNr))) {
	// final DefaultEventSource<KeyData> eventSource = new
	// DefaultEventSource<KeyData>(sensor.toString());
	// final VirtualActor newActor = new VirtualActor(actorNr, address,
	// eventSource);
	// moduleMap.put(new Byte((byte) actorNr), newActor);
	// keySens.addActor(newActor);
	// bus.addKeyHandler(address, (byte) actorNr, new
	// VirtualKeyHandler(eventSource));
	// }
	// }
	//
	// } else if (sensor instanceof TFSensor) {
	// final TFSensor tfSensor = (TFSensor) sensor;
	// final DefaultEventSource<TFSData> source = new
	// DefaultEventSource<TFSData>(sensor.toString());
	// final Thread runner = new Thread(new Runnable() {
	// public void run() {
	// try {
	// while (!Thread.interrupted()) {
	// Thread.sleep(30 * 1000);
	// try {
	// source.fireEvent(new TFSData(tfSensor.readTF()));
	// } catch (final IOException e) {
	// Registry.log.warn("Fehler bei Kommunikation mit Sensor " + sensor, e);
	// }
	// }
	// } catch (final InterruptedException e) {
	// Registry.log.warn("Polling-Thread von Sensor " + sensor +
	// " wurde unterbrochen", e);
	// }
	// }
	// });
	// runner.setDaemon(true);
	// runner.start();
	// return source;
	// }
	// return null;
	// }

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

	public synchronized void resetDevices() {
		foundDevices = null;
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
