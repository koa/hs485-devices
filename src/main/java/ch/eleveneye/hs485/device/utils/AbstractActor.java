package ch.eleveneye.hs485.device.utils;

import ch.eleveneye.hs485.device.SwitchingActor;
import ch.eleveneye.hs485.device.physically.Actor;

public abstract class AbstractActor implements SwitchingActor, Actor {

	protected int actorNr;

	public AbstractActor(final int actorNr) {
		this.actorNr = actorNr;
	}

	@Override
	public int getActorNr() {
		return actorNr;
	}

	@Override
	public String toString() {
		return "Actor " + Integer.toHexString(getModuleAddr()) + ":" + actorNr;
	}

}
