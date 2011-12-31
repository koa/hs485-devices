/**
 * 
 */
package ch.eleveneye.hs485.device;

import ch.eleveneye.hs485.device.physically.Actor;
import ch.eleveneye.hs485.device.virtual.EventSource;

class VirtualActor implements Actor {

	int actorNr;

	int moduleAddr;

	EventSource eventSource;

	public VirtualActor(int actorNr, int moduleAddr, EventSource eventSource) {
		this.actorNr = actorNr;
		this.moduleAddr = moduleAddr;
		this.eventSource = eventSource;
	}

	@Override
	public int getActorNr() {
		return actorNr;
	}

	@Override
	public int getModuleAddr() {
		return moduleAddr;
	}

	public EventSource getEventSource() {
		return eventSource;
	}

}