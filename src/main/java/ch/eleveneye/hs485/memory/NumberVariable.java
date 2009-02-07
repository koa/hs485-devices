package ch.eleveneye.hs485.memory;

public class NumberVariable extends Variable {
	int length;

	int minValue = -1;

	int maxValue = -1;

	int defaultValue = -1;

	public int getDefaultValue() {
		return defaultValue;
	}

	public int getLength() {
		return length;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public int getMinValue() {
		return minValue;
	}

	public void setDefaultValue(final int defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setLength(final int length) {
		this.length = length;
	}

	public void setMaxValue(final int maxValue) {
		this.maxValue = maxValue;
	}

	public void setMinValue(final int minValue) {
		this.minValue = minValue;
	}
}
