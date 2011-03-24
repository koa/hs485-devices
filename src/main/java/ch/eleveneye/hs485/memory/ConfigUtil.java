package ch.eleveneye.hs485.memory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ConfigUtil {
	private static XStream	xstream	= new XStream(new DomDriver());

	@SuppressWarnings("unchecked")
	public static List<ModuleType> readConfig(final InputStream in) {
		synchronized (ConfigUtil.xstream) {
			return (List<ModuleType>) ConfigUtil.xstream.fromXML(in);
		}
	}

	public static void writeConfig(final OutputStream out, final List<ModuleType> modules) {
		synchronized (ConfigUtil.xstream) {
			ConfigUtil.xstream.toXML(modules, out);
		}
	}
}
