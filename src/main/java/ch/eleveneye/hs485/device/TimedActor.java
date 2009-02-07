package ch.eleveneye.hs485.device;

import java.io.IOException;

import ch.eleveneye.hs485.device.config.TimeMode;

public interface TimedActor {
	public int getTimeValue() throws IOException;

	public void setTimeValue(int value) throws IOException;

	public TimeMode getTimeMode() throws IOException;

	public void setTimeMode(TimeMode value) throws IOException;
}
