package ch.eleveneye.hs485.device.physically;

import java.io.IOException;

public interface PairableSensor extends PhysicallySensor {
	public boolean isPaired() throws IOException;
}
