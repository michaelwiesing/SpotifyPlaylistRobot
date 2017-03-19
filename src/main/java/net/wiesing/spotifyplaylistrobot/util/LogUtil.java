package net.wiesing.spotifyplaylistrobot.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.wiesing.spotifyplaylistrobot.model.Constants;

/**
 * Provide logging with log rotation in a simple way.
 * @author Michael Wiesing
 */
public class LogUtil {
	private static LogUtil instance = null;
	private static Logger logger = null;

	/**
	 * Private constructor, because singleton is desired. Consider configuration
	 * for log level and max number of files.
	 * 
	 * @param pu
	 *            Utility to read properties
	 */
	private LogUtil(PropUtil pu) {

		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		FileHandler logFile;
		try {
			int maxNumberOfLogfiles = 0;
			try {
				Integer.parseInt(pu.getProperty("maxNumberOfLogfiles"));
			} catch (NumberFormatException e) {
				logger.log(Level.WARNING, "Problem reading configuration value maxNumberOfLogfiles continue with 0!");
			}

			if (maxNumberOfLogfiles == 0) {
				// Only one big log file on error or if configured with 0
				logFile = new FileHandler(Constants.logFile, true);
			} else {
				// Log rotation with files of the defined log size until limit
				// reached
				logFile = new FileHandler(Constants.logFilePattern, Constants.logSize, maxNumberOfLogfiles, true);
			}
			logFile.setFormatter(new SingleLineFormatter());
			logger.addHandler(logFile);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Problem creating logfile!", e);
			System.exit(1);
		}

		String configuredLevel = pu.getProperty("logLevel");
		if (configuredLevel != null && configuredLevel.length() > 0) {
			// Set log level to the configure value
			setLogLevel(Level.parse(configuredLevel));
		} else {
			// Set standard log level on error
			logger.log(Level.WARNING, "Problem reading configuration value logLevel continue with INFO!");
			setLogLevel(Level.INFO);
		}
	}

	/**
	 * Provide the current instance of the LogUtil and create a new one if
	 * necessary.
	 * 
	 * @param pu
	 *            Utility to read properties
	 * @return LogUtil
	 */
	public static LogUtil getLogHelper(PropUtil pu) {
		if (instance == null) {
			instance = new LogUtil(pu);
		}
		return instance;
	}

	/**
	 * Method to switch the current used log level.
	 * 
	 * @param level
	 *            Target level
	 */
	public void setLogLevel(Level level) {
		logger.setLevel(level);
		logger.log(Level.INFO, "Changed log level to " + level + ".");

	}

	/**
	 * Log a Message.
	 * 
	 * @param level
	 *            Level
	 * @param message
	 *            Content
	 */
	public void log(Level level, String message) {
		logger.log(level, message);
	}

	/**
	 * Log a Message with an exception.
	 * 
	 * @param level
	 *            Level
	 * @param message
	 *            Content
	 * @param exception
	 *            Exception
	 */
	public void log(Level level, String message, Exception exception) {
		logger.log(level, message, exception);
	}

	/**
	 * Method that need to be called before the application exits, to close file
	 * handlers.
	 */
	public void closeLogger() {
		Handler[] handlers = logger.getHandlers();
		for (Handler h : handlers) {
			try {
				h.flush();
				h.close();
			} catch (SecurityException e) {
				logger.log(Level.WARNING, "Problem while closing logging!", e);
			}
		}

		logger = null;
	}

	/**
	 * @author Michael Wiesing
	 * 
	 *         Class used for the formatting of log entries.
	 * 
	 */
	public static class SingleLineFormatter extends Formatter {
		public SingleLineFormatter() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
		 */
		@Override
		public String format(LogRecord record) {
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS zzz");
			Throwable t = record.getThrown();
			String stacktrace = "";
			if (t != null) {
				StringWriter sw = new StringWriter();
				t.printStackTrace(new PrintWriter(sw));
				stacktrace = sw.toString();
			}
			return sdf.format(date) + " " + record.getLevel() + "\t " + record.getMessage() + "\r\n" + stacktrace;
		}
	}

}
