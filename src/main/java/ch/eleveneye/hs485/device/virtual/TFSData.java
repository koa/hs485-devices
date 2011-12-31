package ch.eleveneye.hs485.device.virtual;

import ch.eleveneye.hs485.api.data.TFSValue;

public class TFSData implements EventData {
	long			eventTime;

	TFSValue	value;

	public TFSData(final TFSValue value) {
		this.value = value;
		eventTime = System.currentTimeMillis();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TFSData other = (TFSData) obj;
		if (eventTime != other.eventTime)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public long getEventTime() {
		return eventTime;
	}

	public int getHumidity() {
		return value.getHumidity();
	}

	public double getTemperatur() {
		return value.getTemperatur();
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (int) (eventTime ^ eventTime >>> 32);
		result = PRIME * result + (value == null ? 0 : value.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
