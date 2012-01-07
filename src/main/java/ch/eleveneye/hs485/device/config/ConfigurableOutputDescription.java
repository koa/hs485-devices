package ch.eleveneye.hs485.device.config;

import ch.eleveneye.hs485.device.physically.Actor;

public class ConfigurableOutputDescription {
	private String									labeledName;
	private int											actorNr;
	private Class<? extends Actor>	implementingActor;

	public int getActorNr() {
		return actorNr;
	}

	public Class<? extends Actor> getImplementingActor() {
		return implementingActor;
	}

	public String getLabeledName() {
		return labeledName;
	}

	public void setActorNr(final int actorNr) {
		this.actorNr = actorNr;
	}

	public void setImplementingActor(final Class<? extends Actor> implementingActor) {
		this.implementingActor = implementingActor;
	}

	public void setLabeledName(final String labeledName) {
		this.labeledName = labeledName;
	}

}
