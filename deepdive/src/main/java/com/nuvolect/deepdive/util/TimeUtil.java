/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {

	private static SimpleDateFormat formatMDYM = new SimpleDateFormat("MMM d, yyyy  K:mm a", Locale.US);
	private static SimpleDateFormat formatDMY = new SimpleDateFormat("d MMM, yyyy", Locale.US);
	private static SimpleDateFormat formatHrMinSec = new SimpleDateFormat("K:mm:ss a", Locale.US);
	/**
	 * Return time as a string in a user friendly format
	 * @param t
	 * @return string
	 */
	static public String friendlyTimeString(long t){

		formatMDYM.setTimeZone(TimeZone.getDefault());
		return formatMDYM.format( t);
	}
	/**
	 * Return time as a string in a user friendly format
	 * @param t
	 * @return string
	 */
	public static String friendlyTimeMDYM(long t) {

		formatMDYM.setTimeZone(TimeZone.getDefault());
		return formatMDYM.format( t);
	}

	/**
	 * Return time as a string in a user friendly format
	 * @param t
	 * @return string
	 */
	static public String friendlyTimeHrMinSec(long t){

		formatHrMinSec.setTimeZone(TimeZone.getDefault());
		return formatHrMinSec.format( t );
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

	/**
	 * Return the delta time in short form.  Note that unit notation will always be singular
	 * @param earlierTime
	 * @return String
	 */
	static public String deltaTimeHrMinSecMs( Long earlierTime){

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
        long remainderMs = remainder - (seconds * 1000);

		// Display hours, if there are any
		if( hours > 0)
			time = time + hours + " hr ";

		// Display minutes, if there are any
		if( minutes > 0)
			time = time + minutes + " min ";

		// Always display seconds
		time = time + seconds + " sec "+remainderMs + " ms";

		return time;
	}

	public static String getFriendlyDate(long time) {

		formatMDYM.setTimeZone(TimeZone.getDefault());
		return formatDMY.format( time);
	}
}
