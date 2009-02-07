package ch.eleveneye.hs485.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ArrayVariable extends Variable {
	int count;

	int step;

	String name;

	List<Variable> components;

	public ArrayVariable() {
		components = new ArrayList<Variable>();
	}

	public void addComponent(final Variable var) {
		components.add(var);
	}

	public Variable getComponentByName(final String name) {
		for (final Variable comp : components)
			if (comp.getName().equals(name))
				return comp;
		return null;
	}

	public Variable getComponentByOffset(final int offset) {
		for (final Variable comp : components)
			if (comp.getAddress() == offset)
				return comp;
		return null;
	}

	public int getCount() {
		return count;
	}

	@Override
	public String getName() {
		return name;
	}

	public int getStep() {
		return step;
	}

	public void setCount(final int count) {
		this.count = count;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	public void setStep(final int step) {
		this.step = step;
	}

	public Collection<Variable> listComponents() {
		return Collections.unmodifiableCollection(components);
	}
}
