package ch.eleveneye.hs485.device.virtual;

public class KeyData implements EventData {
	public static enum Event {
		PRESS, HOLD, RELEASE
	}

	public static enum Key {
		TOGGLE, UP, DOWN
	}

	long eventTime;

	Event event;

	Key key;

	public KeyData(Key key, Event event) {
		super();
		this.key = key;
		this.event = event;
		this.eventTime = System.currentTimeMillis();
	}

	public Event getEvent() {
		return event;
	}

	public Key getKey() {
		return key;
	}

	@Override
	public long getEventTime() {
		return eventTime;
	}
}
