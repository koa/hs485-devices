package ch.eleveneye.hs485.device.virtual.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import ch.eleveneye.hs485.device.virtual.EventData;
import ch.eleveneye.hs485.device.virtual.EventSink;
import ch.eleveneye.hs485.device.virtual.EventSource;

public class DefaultEventSource<E extends EventData> implements EventSource<E> {

	Collection<EventSink<E>>	registeredSinks	= new ArrayList<EventSink<E>>();

	String										roleName;

	public DefaultEventSource(final String roleName) {
		this.roleName = roleName;
	}

	public void addSink(final EventSink<E> sink) {
		registeredSinks.add(sink);
	}

	public void fireEvent(final E event) {
		for (final EventSink<E> sink : registeredSinks)
			sink.takeEvent(event);
	}

	public String getRoleName() {
		return roleName;
	}

	public Collection<EventSink<E>> listAllSinks() {
		return Collections.unmodifiableCollection(registeredSinks);
	}

	public void removeAllSinks() {
		registeredSinks.clear();
	}

	public void removeSink(final EventSink<E> sink) {
		registeredSinks.remove(sink);
	}

}
