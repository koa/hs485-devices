package ch.eleveneye.hs485.device.virtual;

public class SwitchActorData implements EventData {
	public static enum SwitchEvent {
		ON, OFF, TOGGLE
	}

	protected long eventTime;

	protected SwitchEvent event;

	public SwitchActorData(SwitchEvent event) {
		this.event = event;
		this.eventTime = System.currentTimeMillis();
	}

	public SwitchEvent getEvent() {
		return event;
	}

	public long getEventTime() {
		return eventTime;
	}
}
