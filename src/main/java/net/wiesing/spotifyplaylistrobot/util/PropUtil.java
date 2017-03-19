package net.wiesing.spotifyplaylistrobot.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import net.wiesing.spotifyplaylistrobot.model.Constants;

/**
 * Provide the access to the property file in a simple way.
 * @author Michael Wiesing
 */
public class PropUtil {

	private final Properties prop = new Properties();
	private final String filename = Constants.propFile;

	/**
	 * Reads a property.
	 * 
	 * @param key
	 *            Key
	 * @return Value as string
	 */
	public String getProperty(String key) {
		return prop.getProperty(key);
	}

	/**
	 * Writes a property.
	 * 
	 * @param key
	 *            Key
	 * @param value
	 *            Value as string
	 */
	public void setProperty(String key, String value) {
		prop.setProperty(key, value);
	}

	/**
	 * Writes the properties to file. Should be called at the stop of
	 * application.
	 */
	public void writeProperty() {
		try {
			OutputStream output = new FileOutputStream(filename);
			prop.store(output, null);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads the properties from file. Should be called at the start of
	 * application.
	 */
	public void readProperty() {
		InputStream input;
		try {
			input = new FileInputStream(filename);
			prop.load(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
