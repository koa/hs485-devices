package ch.eleveneye.hs485.device;

import java.io.IOException;

public interface SwitchingActor {
	public boolean isOn() throws IOException;

	public void setOff() throws IOException;

	public void setOn() throws IOException;

	public void toggle() throws IOException;

	public boolean getToggleBit() throws IOException;

	public void setToggleBit(boolean value) throws IOException;
}
