package ch.eleveneye.hs485.device.config;

public class ConfigurableOutputDescription {
	private String	labeledName;
	private int			actorNr;

	public int getActorNr() {
		return actorNr;
	}

	public String getLabeledName() {
		return labeledName;
	}

	public void setActorNr(final int actorNr) {
		this.actorNr = actorNr;
	}

	public void setLabeledName(final String labeledName) {
		this.labeledName = labeledName;
	}

}
