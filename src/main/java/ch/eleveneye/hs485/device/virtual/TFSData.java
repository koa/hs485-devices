package ch.eleveneye.hs485.device.virtual;

import ch.eleveneye.hs485.protocol.data.TFSValue;

public class TFSData implements EventData {
	TFSValue value;

	long eventTime;

	public TFSData(TFSValue value) {
		this.value = value;
		this.eventTime = System.currentTimeMillis();
	}

	public int getHumidity() {
		return value.getHumidity();
	}

	public double getTemperatur() {
		return value.getTemperatur();
	}

	@Override
	public String toString() {
		return value.toString();
	}

	public long getEventTime() {
		return eventTime;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (int) (eventTime ^ (eventTime >>> 32));
		result = PRIME * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
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
}
