package ch.eleveneye.hs485.device.config;

public class ConfigurableInputDescription {
	private String	labeledName;
	private int			sensorNr;

	public String getLabeledName() {
		return labeledName;
	}

	public int getSensorNr() {
		return sensorNr;
	}

	public void setLabeledName(final String labeledName) {
		this.labeledName = labeledName;
	}

	public void setSensorNr(final int sensorNr) {
		this.sensorNr = sensorNr;
	}

}
