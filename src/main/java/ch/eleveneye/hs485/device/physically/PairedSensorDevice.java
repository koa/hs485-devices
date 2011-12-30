package ch.eleveneye.hs485.device.physically;

import java.io.IOException;

import ch.eleveneye.hs485.device.config.PairMode;

public interface PairedSensorDevice {

	public int getInputPairCount();

	public PairMode getInputPairMode(int pairNr) throws IOException;

	public void setInputPairMode(int pairNr, PairMode mode) throws IOException;

}
