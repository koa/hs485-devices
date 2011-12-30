package ch.eleveneye.hs485.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ch.eleveneye.hs485.api.data.HwVer;
import ch.eleveneye.hs485.api.data.SwVer;
import ch.eleveneye.hs485.device.Device;

public class ModuleType {

	List<Variable>				variables;

	private int						eepromSize;

	private HwVer					hwVer;

	private Class<Device>	implementingClass;
	private String				name;

	private SwVer					swVer;

	private int						width;

	public ModuleType() {
		variables = new ArrayList<Variable>();
	}

	public void addVariable(final Variable var) {
		variables.add(var);
	}

	public int getEepromSize() {
		return eepromSize;
	}

	public HwVer getHwVer() {
		return hwVer;
	}

	public Class getImplementingClass() {
		return implementingClass;
	}

	public String getName() {
		return name;
	}

	public SwVer getSwVer() {
		return swVer;
	}

	public Variable getVariableByAddress(final int address) {
		for (final Variable var : variables)
			if (var.getAddress() == address)
				return var;
		return null;
	}

	public Variable getVariableByName(final String name) {
		for (final Variable var : variables)
			if (var.getName().equals(name))
				return var;
		return null;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	public Collection<Variable> listVariables() {
		return Collections.unmodifiableCollection(variables);
	}

	public void setEepromSize(final int eepromSize) {
		this.eepromSize = eepromSize;
	}

	public void setHwVer(final HwVer hwVer) {
		this.hwVer = hwVer;
	}

	public void setImplementingClass(final Class implementingClass) {
		this.implementingClass = implementingClass;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setSwVer(final SwVer swVer) {
		this.swVer = swVer;
	}

	/**
	 * @param width
	 *          the width to set
	 */
	public void setWidth(final int width) {
		this.width = width;
	}
}
