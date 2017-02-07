package com.nuvolect.deepdive.util;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {

	private static SimpleDateFormat simpleFormat = new SimpleDateFormat("MMM d, yyyy  K:mm a", Locale.US);
	/**
	 * Return time as a string in a user friendly format
	 * @param t
	 * @return string
	 */
	static public String friendlyTimeString(long t){

		simpleFormat.setTimeZone(TimeZone.getDefault());
		return simpleFormat.format( t);
	}

	/**
	 * Return the delta time in short form.  Note that unit notation will always be singular
	 * @param earlierTime
	 * @return String
	 */
	static public String deltaTimeHrMinSec( Long earlierTime){

		String time = "";

		long dt = System.currentTimeMillis() - earlierTime;
		long days = dt / (24 * 60 * 60 * 1000);
		long hours = (dt - days * (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
		long minutes = (dt - days * (24 * 60 * 60 * 1000)
				- hours * (60 * 60 * 1000))
				/ (60 * 1000);
		long remainder = dt
				- days * 24 * 60 * 60 * 1000
				- hours * 60 * 60 * 1000
				- minutes * 60 * 1000;
		long seconds = (remainder / 1000) % 60;

		// Display hours, if there are any
		if( hours > 0)
			time = time + hours + " hr ";

		// Display minutes, if there are any
		if( minutes > 0)
			time = time + minutes + " min ";

		// Always display seconds
		time = time + seconds + " sec";

		return time;
	}
}
