/*
 * Copyright (c) 2016 Lars Gaidzik & Lukas Mahr
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.hof.university.app.Util;

import de.hof.university.app.BuildConfig;


/**
 * Derived logging class that looks for the BuildConfig parameter (DEBUG=0 or 1)
 */
public final class Log {

    private Log() {
        super();
    }

    /**
     * Look into android.util.Log.d
     *
     * @param Tag
     * @param msg
     */
    public static void d(final String Tag, final String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(Tag, msg);
        }
    }

    /**
     * Look into android.util.Log.i
     *
     * @param Tag
     * @param msg
     */
    public static void i(final String Tag, final String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(Tag, msg);
        }
    }

    /**
     * Look into android.util.Log.e
     *
     * @param Tag
     * @param msg
     */
    public static void e(final String Tag, final String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(Tag, msg);
        }
    }

    /**
     * Look into android.util.Log.e
     *
     * @param Tag
     * @param msg
     * @param e
     */
    public static void e(final String Tag, final String msg, final Exception e) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(Tag, msg, e);
        }
    }

    /**
     * Look into android.util.Log.v
     *
     * @param Tag
     * @param msg
     */
    public static void v(final String Tag, final String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(Tag, msg);
        }
    }

}
