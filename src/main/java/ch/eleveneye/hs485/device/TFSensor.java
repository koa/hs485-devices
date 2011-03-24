package ch.eleveneye.hs485.device;

import java.io.IOException;

import ch.eleveneye.hs485.api.data.TFSValue;

public interface TFSensor {
	public TFSValue readTF() throws IOException;
}
