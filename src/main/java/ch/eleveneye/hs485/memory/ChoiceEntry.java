package ch.eleveneye.hs485.memory;

public class ChoiceEntry {
	int value;

	String name;

	String description;

	public ChoiceEntry() {
	}

	public ChoiceEntry(final int value, final String name,
			final String description) {
		this.value = value;
		this.name = name;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setValue(final int value) {
		this.value = value;
	}
}
