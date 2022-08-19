package ch.eleveneye.hs485.device.utils;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eleveneye.hs485.api.HS485;
import ch.eleveneye.hs485.device.Registry;
import ch.eleveneye.hs485.device.physically.Actor;
import ch.eleveneye.hs485.device.physically.PhysicallyDevice;
import ch.eleveneye.hs485.memory.ArrayVariable;
import ch.eleveneye.hs485.memory.ChoiceEntry;
import ch.eleveneye.hs485.memory.ChoiceVariable;
import ch.eleveneye.hs485.memory.ModuleType;
import ch.eleveneye.hs485.memory.NumberVariable;
import ch.eleveneye.hs485.memory.Variable;
import ch.eleveneye.hs485.protocol.TimeoutException;

public abstract class AbstractDevice implements PhysicallyDevice {

	static protected class MemDescription {
		Variable	elementDescr;

		int				length;

		int				memAddr;

		boolean		reload;

		public MemDescription(final int memAddr, final int length, final Variable elementDescr, final boolean reload) {
			this.memAddr = memAddr;
			this.length = length;
			this.elementDescr = elementDescr;
			this.reload = reload;
		}

		public Variable getElementDescr() {
			return elementDescr;
		}

		public int getLength() {
			return length;
		}

		public int getMemAddr() {
			return memAddr;
		}

		public boolean isReload() {
			return reload;
		}
	}

	static protected class MemSegment {
		int	length;

		int	start;

		public MemSegment(final int start) {
			this.start = start;
			length = 1;
			/*
			 * if (start > 3) { this.start = 0; length = start; }
			 */
		}

		public int gapSizeTo(final MemSegment other) {
			if (other.start < start)
				return other.gapSizeTo(this);
			return other.start - getEnd();
		}

		public int getEnd() {
			return start + length;
		}

		public int getLength() {
			return length;
		}

		public int getStart() {
			return start;
		}

		public void join(final MemSegment other) {
			final int minStart = Math.min(start, other.start);
			final int maxEnd = Math.max(getEnd(), other.getEnd());
			start = minStart;
			length = maxEnd - minStart;
		}

		public void setLength(final int length) {
			this.length = length;
		}

		public void setStart(final int start) {
			this.start = start;
		}
	}

	final static int			MIN_GAP_SIZE	= 3;

	private static Logger	log						= LoggerFactory.getLogger(AbstractDevice.class);

	protected static ArrayVariable defaultTargetConfig() {
		final ArrayVariable targetConfig = new ArrayVariable();
		targetConfig.setAddress(0x80);
		targetConfig.setCount(64);
		targetConfig.setStep(6);
		targetConfig.setName("target");
		targetConfig.setReload(true);
		final NumberVariable sourceNr = new NumberVariable();
		sourceNr.setAddress(0);
		sourceNr.setName("input-nr");
		sourceNr.setLength(1);
		targetConfig.addComponent(sourceNr);
		final NumberVariable targetAddr = new NumberVariable();
		targetAddr.setAddress(1);
		targetAddr.setName("target-addr");
		targetAddr.setLength(4);
		targetConfig.addComponent(targetAddr);
		final NumberVariable targetNr = new NumberVariable();
		targetNr.setAddress(5);
		targetNr.setName("target-nr");
		targetNr.setLength(1);
		targetConfig.addComponent(targetNr);
		return targetConfig;
	}

	protected HS485				bus;

	protected ModuleType	currentConfig;

	protected byte[]			currentMemory;

	protected int					deviceAddr;

	protected byte[]			oldMemory;

	protected Registry		registry;

	protected boolean			reloadOnCommit;

	protected void addInputTargetRaw(final int inputNr, final int targetAddress, final int targetNr) throws IOException {
		int firstFree = -1;
		final int configCount = ((ArrayVariable) currentConfig.getVariableByName("target")).getCount();
		for (int i = 0; i < configCount; i += 1) {
			final int currentInput = readVariable("target[" + i + "].input-nr");
			final int currentTargetAddr = readVariable("target[" + i + "].target-addr");
			final int currentTargetNr = readVariable("target[" + i + "].target-nr");
			if (currentInput == 0xff || currentTargetAddr == -1 || currentTargetNr == 0xff) {
				if (firstFree < 0)
					firstFree = i;
			} else if (currentInput == inputNr && currentTargetAddr == targetAddress && currentTargetNr == targetNr)
				// Ziel ist schon programmiert
				return;
		}
		if (firstFree == -1)
			throw new IllegalArgumentException("Auf dem Modul " + this + " sind schon alle Konfigurations-Register belegt");
		writeVariable("target[" + firstFree + "].input-nr", inputNr);
		writeVariable("target[" + firstFree + "].target-addr", targetAddress);
		writeVariable("target[" + firstFree + "].target-nr", targetNr);
	}

	private synchronized void checkMemory() throws IOException {
		if (oldMemory == null) {
			oldMemory = bus.readModuleEEPROM(deviceAddr, currentConfig.getEepromSize());
			currentMemory = new byte[oldMemory.length];
			System.arraycopy(oldMemory, 0, currentMemory, 0, oldMemory.length);
			reloadOnCommit = false;
		}
	}

	public void clearAllInputTargets() throws IOException {
		final int configCount = ((ArrayVariable) currentConfig.getVariableByName("target")).getCount();
		for (int i = 0; i < configCount; i += 1) {
			writeVariable("target[" + i + "].input-nr", 0xff);
			writeVariable("target[" + i + "].target-addr", -1);
			writeVariable("target[" + i + "].target-nr", 0xff);
		}
	}

	public synchronized void commit() throws IOException {
		if (oldMemory == null)
			return;
		final LinkedList<MemSegment> foundSegments = new LinkedList<MemSegment>();
		for (int i = 0; i < oldMemory.length; i++)
			if (oldMemory[i] != currentMemory[i]) {
				final MemSegment diffPos = new MemSegment(i);
				if (foundSegments.isEmpty() || foundSegments.getLast().gapSizeTo(diffPos) > MIN_GAP_SIZE)
					foundSegments.add(diffPos);
				else
					foundSegments.getLast().join(diffPos);
			}
		try {
			for (final MemSegment memSegment : foundSegments) {
				int start = memSegment.getStart();
				int length = memSegment.getLength();
				while (true)
					try {
						bus.writeModuleEEPROM(deviceAddr, start, currentMemory, start, length);
						break;
					} catch (final TimeoutException ex) {
						if (start > 0) {
							start -= 1;
							length += 1;
							continue;
						} else if (length < currentMemory.length - start) {
							start = memSegment.getStart();
							length += 1;
							continue;
						} else
							throw ex;
					}
			}
			oldMemory = new byte[currentMemory.length];
			System.arraycopy(currentMemory, 0, oldMemory, 0, currentMemory.length);
			if (reloadOnCommit)
				bus.reloadModule(deviceAddr);
			reloadOnCommit = false;
		} catch (final IOException e) {
			// Gespeicherte Daten löschen und Exception weiterwerfen
			oldMemory = null;
			currentMemory = null;
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */

	public synchronized <T> T doInTransaction(final Callable<T> callable) throws IOException {
		try {
			final T ret = callable.call();
			commit();
			return ret;
		} catch (final Exception e) {
			log.warn("Error in Transaction, make rollback", e);
			rollback();
			throw new RuntimeException(e);
		}
	}

	protected void dumpVariable(final Collection<Variable> var, final String baseName, final int depth) throws IOException {
		for (final Variable variable : var) {
			final String currentName = baseName + variable.getName();
			if (variable instanceof NumberVariable) {
				final int value = readVariable(currentName);
				if (value == -1 || value == 255 || value == 65535)
					// skip default-values
					continue;
				log.info(" " + currentName + "=" + value);
			} else if (variable instanceof ChoiceVariable) {
				final ChoiceVariable choice = (ChoiceVariable) variable;
				final int value = readVariable(currentName);
				String valueStr = Integer.toHexString(value);
				final ChoiceEntry entry = choice.getEntryByValue(value);
				if (entry != null)
					valueStr = '"' + entry.getName() + '"';
				log.info(" " + currentName + "=" + valueStr);

			} else if (variable instanceof ArrayVariable) {
				final ArrayVariable array = (ArrayVariable) variable;
				for (int i = 0; i < array.getCount(); i++)
					dumpVariable(array.listComponents(), currentName + "[" + i + "].", depth + 1);
			}
		}
	}

	public void dumpVariables() throws IOException {
		final Collection<Variable> variables = currentConfig.listVariables();
		dumpVariable(variables, "", 0);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AbstractDevice other = (AbstractDevice) obj;
		if (bus == null) {
			if (other.bus != null)
				return false;
		} else if (!bus.equals(other.bus))
			return false;
		if (deviceAddr != other.deviceAddr)
			return false;
		return true;
	}

	public int getAddress() {
		return deviceAddr;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (bus == null ? 0 : bus.hashCode());
		result = PRIME * result + deviceAddr;
		return result;
	}

	public void init(final int deviceAddr, final Registry registry, final ModuleType config) {
		this.deviceAddr = deviceAddr;
		this.registry = registry;
		bus = registry.getBus();
		currentConfig = config;
		oldMemory = null;
		currentMemory = null;
	}

	protected Collection<Actor> listAssignedActorsRaw(final int sensor) throws IOException {
		final Collection<Actor> ret = new LinkedList<Actor>();
		final int configCount = ((ArrayVariable) currentConfig.getVariableByName("target")).getCount();
		for (int i = 0; i < configCount; i += 1) {
			final int currentInput = readVariable("target[" + i + "].input-nr");
			if (currentInput == sensor) {
				final int currentTargetAddr = readVariable("target[" + i + "].target-addr");
				final int currentTargetNr = readVariable("target[" + i + "].target-nr");
				if (currentTargetAddr == -1 || currentTargetNr == 0xff)
					continue;
				ret.add(registry.getActor(currentTargetAddr, currentTargetNr));
			}
		}
		return ret;
	}

	public synchronized byte[] readCurrentMemory() {
		final byte[] ret = new byte[currentMemory.length];
		System.arraycopy(currentMemory, 0, ret, 0, ret.length);
		return ret;
	}

	protected int readVariable(final String name) throws IOException {
		final MemDescription resolved = resolveName(name);
		return readVariableRaw(resolved);
	}

	private synchronized int readVariableRaw(final MemDescription resolved) throws IOException {
		checkMemory();
		final int address = resolved.getMemAddr();
		final int length = resolved.getLength();
		int ret = 0;
		for (int i = 0; i < length; i += 1)
			ret = ret << 8 | currentMemory[address + i] & 0xff;
		return ret;
	}

	protected String readVariableResolved(final String name) throws IOException {
		final MemDescription resolved = resolveName(name);
		final int rawValue = readVariableRaw(resolved);
		final Variable descr = resolved.getElementDescr();
		if (descr instanceof ChoiceVariable) {
			final ChoiceVariable choice = (ChoiceVariable) descr;
			return choice.getEntryByValue(rawValue).getName();
		}
		return rawValue + "";
	}

	protected void removeInputTargetRaw(final int inputNr, final int targetAddress, final int targetNr) throws IOException {
		final int configCount = ((ArrayVariable) currentConfig.getVariableByName("target")).getCount();
		for (int i = 0; i < configCount; i += 1) {
			final int currentInput = readVariable("target[" + i + "].input-nr");
			final int currentTargetAddr = readVariable("target[" + i + "].target-addr");
			final int currentTargetNr = readVariable("target[" + i + "].target-nr");
			if (currentInput == inputNr && currentTargetAddr == targetAddress && currentTargetNr == targetNr) {
				// Eintrag gefunden -> löschen
				writeVariable("target[" + i + "].input-nr", 0xff);
				writeVariable("target[" + i + "].target-addr", -1);
				writeVariable("target[" + i + "].target-nr", 0xff);
			}
			if (currentInput == 0xff || currentTargetAddr == -1 || targetNr == 0xff) {
				// Ungültiger Eintrag gefunden -> sicher löschen
				writeVariable("target[" + i + "].input-nr", 0xff);
				writeVariable("target[" + i + "].target-addr", -1);
				writeVariable("target[" + i + "].target-nr", 0xff);
			}
		}
	}

	public synchronized void reset() throws IOException {
		checkMemory();
		reloadOnCommit = true;
		for (int i = 0; i < currentMemory.length; i++)
			currentMemory[i] = -1;
		clearAllInputTargets();
	}

	protected MemDescription resolveName(final String name) {
		final String[] nameParts = name.split("\\.");
		ArrayVariable lastArray = null;
		int curentOffset = 0;
		boolean reload = false;
		for (final String currentPart : nameParts) {
			int index = 0;
			String partName = currentPart;
			if (currentPart.endsWith("]")) {
				final int indexStart = currentPart.indexOf('[');
				if (indexStart > 0) {
					partName = currentPart.substring(0, indexStart);
					index = Integer.parseInt(currentPart.substring(indexStart + 1, currentPart.length() - 1));
				}
			}
			Variable nextVariable;
			if (lastArray == null)
				nextVariable = currentConfig.getVariableByName(partName);
			else
				nextVariable = lastArray.getComponentByName(partName);
			if (nextVariable == null) {
				log.warn("Variable " + name + " vom Modul " + Integer.toHexString(deviceAddr) + " nicht gefunden");
				return null;
			}
			reload |= nextVariable.isReload();
			curentOffset += nextVariable.getAddress();
			if (nextVariable instanceof ArrayVariable) {
				lastArray = (ArrayVariable) nextVariable;
				if (index >= lastArray.getCount())
					throw new ArrayIndexOutOfBoundsException(
							"Array " + partName + " enthält nur " + lastArray.getCount() + " Elemente, index " + index + " ist ungültig");
				curentOffset += lastArray.getStep() * index;
			} else {
				int length = 0;
				if (nextVariable instanceof NumberVariable)
					length = ((NumberVariable) nextVariable).getLength();
				else if (nextVariable instanceof ChoiceVariable)
					length = ((ChoiceVariable) nextVariable).getLength();
				return new MemDescription(curentOffset, length, nextVariable, reload);
			}

		}
		return null;
	}

	public synchronized void rollback() throws IOException {
		checkMemory();
		reloadOnCommit = false;
		System.arraycopy(oldMemory, 0, currentMemory, 0, oldMemory.length);
	}

	@Override
	public String toString() {
		return "Device: " + Integer.toHexString(deviceAddr);

	}

	protected void writeChoice(final String name, final String value) throws IOException {
		final MemDescription resolved = resolveName(name);
		final ChoiceVariable variable = (ChoiceVariable) resolved.getElementDescr();
		writeVariableRaw(variable.getEntryByName(value).getValue(), resolved);
	}

	protected void writeVariable(final String name, final int value) throws IOException {
		writeVariableRaw(value, resolveName(name));
	}

	private synchronized void writeVariableRaw(final int value, final MemDescription resolved) throws IOException {
		checkMemory();
		final int address = resolved.getMemAddr();
		final int length = resolved.getLength();
		boolean modified = false;
		for (int i = 0; i < length; i += 1) {
			modified |= currentMemory[address + i] != (byte) (value >> (length - i - 1) * 8 & 0xff);
			currentMemory[address + i] = (byte) (value >> (length - i - 1) * 8 & 0xff);
		}
		reloadOnCommit |= resolved.isReload() && modified;
	}
}
