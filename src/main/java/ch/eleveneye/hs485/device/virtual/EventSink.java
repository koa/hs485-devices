package ch.eleveneye.hs485.device.virtual;

public interface EventSink<E extends EventData> {
	public void takeEvent(E data);

	public String getRoleName();
}
