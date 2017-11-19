package de.hof.university.app.Util;

import de.hof.university.app.BuildConfig;

/**
 * Derived Assert class that looks for the BuildConfig parameter (DEBUG=0 or 1)
 */

public class Assert {
	public static void assertTrue(boolean condition) {
		if (BuildConfig.DEBUG) {
			Assert.assertTrue(condition);
		}
	}

	public static void assertTrue(String message, boolean condition) {
		if (BuildConfig.DEBUG) {
			Assert.assertTrue(message, condition);
		}
	}
}
