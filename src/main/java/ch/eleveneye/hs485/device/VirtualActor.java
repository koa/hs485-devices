/**
 *
 */
package ch.eleveneye.hs485.device;

import ch.eleveneye.hs485.device.physically.Actor;
import ch.eleveneye.hs485.device.virtual.EventSource;

public class VirtualActor implements Actor {

	int												actorNr;

	int												moduleAddr;

	private final EventSource	eventSource;

	public VirtualActor(final int actorNr, final int moduleAddr, final EventSource eventSource) {
		this.actorNr = actorNr;
		this.moduleAddr = moduleAddr;
		this.eventSource = eventSource;
	}

	public int getActorNr() {
		return actorNr;
	}

	public EventSource getEventSource() {
		return eventSource;
	}

	public int getModuleAddr() {
		return moduleAddr;
	}

}