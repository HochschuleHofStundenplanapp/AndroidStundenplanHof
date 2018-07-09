/*
 * Copyright (c) 2018 Hochschule Hof
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

package de.hof.university.app.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Daniel on 07.12.2017.
 */

public class MyDateTime {
	private static final String TAG = "MyDateTime";

	/**
	 * return the date from the given string
	 * @param timeDateString im Format: HH:mm dd.MM.yyyy example: 14:00 11.12.2017
	 * @return the date from the string
	 */
	public static Date getDateFromString( final String timeDateString) {

		Calendar calendar = GregorianCalendar.getInstance();

		// set milliseconds to 0 because otherwise it could be not 0
		calendar.set( Calendar.MILLISECOND, 0 );

		try {
			// timeDateString: 14:00 11.12.2017

			SimpleDateFormat sdf = new SimpleDateFormat( "HH:mm dd.MM.yyyy" );
			calendar.setTime( sdf.parse( timeDateString ) );
		} catch ( NumberFormatException e ) {
			Log.e( TAG, "getDateFromString", e );
		} catch ( ParseException e ) {
			Log.e( TAG, "ParseException abgefangen: ", e );
		}

		return calendar.getTime();
	}
}
