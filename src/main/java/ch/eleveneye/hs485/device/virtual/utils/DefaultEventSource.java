package ch.eleveneye.hs485.device.virtual.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import ch.eleveneye.hs485.device.virtual.EventData;
import ch.eleveneye.hs485.device.virtual.EventSink;
import ch.eleveneye.hs485.device.virtual.EventSource;

public class DefaultEventSource<E extends EventData> implements EventSource<E> {

	Collection<EventSink<E>> registeredSinks = new ArrayList<EventSink<E>>();

	String roleName;

	public DefaultEventSource(String roleName) {
		this.roleName = roleName;
	}

	@Override
	public void addSink(EventSink<E> sink) {
		registeredSinks.add(sink);
	}

	@Override
	public void removeSink(EventSink<E> sink) {
		registeredSinks.remove(sink);
	}

	public void fireEvent(E event) {
		for (EventSink<E> sink : registeredSinks)
			sink.takeEvent(event);
	}

	@Override
	public String getRoleName() {
		return roleName;
	}

	@Override
	public void removeAllSinks() {
		registeredSinks.clear();
	}

	@Override
	public Collection<EventSink<E>> listAllSinks() {
		return Collections.unmodifiableCollection(registeredSinks);
	}

}
