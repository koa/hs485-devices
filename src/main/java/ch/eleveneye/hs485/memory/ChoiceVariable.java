package ch.eleveneye.hs485.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ChoiceVariable extends Variable {
	int length;

	List<ChoiceEntry> entries;

	public ChoiceVariable() {
		entries = new ArrayList<ChoiceEntry>(3);
	}

	public void addChoiceEntry(final ChoiceEntry entry) {
		entries.add(entry);
	}

	public ChoiceEntry getEntryByName(final String name) {
		for (final ChoiceEntry entry : entries)
			if (entry.getName().equals(name))
				return entry;
		return null;
	}

	public ChoiceEntry getEntryByValue(final int value) {
		for (final ChoiceEntry entry : entries)
			if (entry.getValue() == value)
				return entry;
		return null;
	}

	public int getLength() {
		return length;
	}

	public void setLength(final int length) {
		this.length = length;
	}

	public Collection<ChoiceEntry> listEntries() {
		return Collections.unmodifiableCollection(entries);
	}
}
