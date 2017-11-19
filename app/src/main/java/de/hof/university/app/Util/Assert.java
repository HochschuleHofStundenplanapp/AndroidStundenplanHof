package de.hof.university.app.Util;

import de.hof.university.app.BuildConfig;

/**
 * Derived Assert class that looks for the BuildConfig parameter (DEBUG=0 or 1)
 */

public class Assert {
	/**
	 *
	 * @param condition the condition
	 */
	public static void assertTrue(boolean condition) {
		if (BuildConfig.DEBUG) {
			junit.framework.Assert.assertTrue(condition);
		}
	}

	public static void assertTrue(String message, boolean condition) {
		if (BuildConfig.DEBUG) {
			junit.framework.Assert.assertTrue(message, condition);
		}
	}
}
