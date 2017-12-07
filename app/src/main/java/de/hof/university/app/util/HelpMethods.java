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

public class HelpMethods {
	private static final String TAG = "HelpMethods";

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
