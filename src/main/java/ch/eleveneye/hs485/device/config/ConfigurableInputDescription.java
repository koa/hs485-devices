package ch.eleveneye.hs485.device.config;

import ch.eleveneye.hs485.device.Sensor;

public class ConfigurableInputDescription {
	private String									labeledName;
	private int											sensorNr;
	private Class<? extends Sensor>	implementionSensor;

	public Class<? extends Sensor> getImplementionSensor() {
		return implementionSensor;
	}

	public String getLabeledName() {
		return labeledName;
	}

	public int getSensorNr() {
		return sensorNr;
	}

	public void setImplementionSensor(final Class<? extends Sensor> implementionSensor) {
		this.implementionSensor = implementionSensor;
	}

	public void setLabeledName(final String labeledName) {
		this.labeledName = labeledName;
	}

	public void setSensorNr(final int sensorNr) {
		this.sensorNr = sensorNr;
	}

}
