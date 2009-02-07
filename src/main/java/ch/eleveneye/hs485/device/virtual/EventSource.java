package ch.eleveneye.hs485.device.virtual;

import java.util.Collection;

public interface EventSource<E extends EventData> {
	public void addSink(EventSink<E> sink);

	public void removeSink(EventSink<E> sink);

	public void removeAllSinks();

	public Collection<EventSink<E>> listAllSinks();

	public String getRoleName();
}
