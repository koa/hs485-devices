package ch.eleveneye.hs485.device;

import java.io.IOException;

import ch.eleveneye.hs485.api.data.KeyMessage;

public interface KeyActor {
	void sendKeyMessage(KeyMessage keyMessage) throws IOException;
}
