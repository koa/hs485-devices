package ch.eleveneye.hs485.device.virtual;

public class DimmActorData implements EventData {
	public static final int	MAX_DIMM_VALUE	= 0x0f;

	int											dimmValue;

	long										eventTime;

	public DimmActorData(final int dimmValue) {
		this.dimmValue = dimmValue;
		this.eventTime = System.currentTimeMillis();
	}

	public int getDimmValue() {
		return dimmValue;
	}

	public long getEventTime() {
		return eventTime;
	}
}
