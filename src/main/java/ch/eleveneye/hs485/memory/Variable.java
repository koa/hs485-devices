package ch.eleveneye.hs485.memory;

public abstract class Variable {
	int address;

	String name;

	String description;

	boolean reload;

	public int getAddress() {
		return address;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public boolean isReload() {
		return reload;
	}

	public void setAddress(final int address) {
		this.address = address;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setReload(final boolean reload) {
		this.reload = reload;
	}
}
