package ch.eleveneye.hs485.device;

import java.io.IOException;

import ch.eleveneye.hs485.device.config.DimmerMode;

public interface Dimmer extends SwitchingActor {
	public int getDimmValue() throws IOException;

	public void setDimmValue(int value) throws IOException;;

	public DimmerMode getDimmerMode() throws IOException;

	public void setDimmerMode(DimmerMode value) throws IOException;

	public int getMaxValue() throws IOException;
}
