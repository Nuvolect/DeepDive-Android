/*
 * Copyright (c) 2011 - 2015, Nuvolect LLC. All Rights Reserved.
 * All intellectual property rights, including without limitation to
 * copyright and trademark of this work and its derivative works are
 * the property of, or are licensed to, Nuvolect LLC.
 * Any unauthorized use is strictly prohibited.
 */
package com.nuvolect.deepdive.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DeviceInfo {

	/**
	 * Return a unique string for the device.  This string only changes when you wipe the device
	 * and reinstall Android.
	 * @param context
	 * @return unique device ID string
	 */
	public static String getUniqueDeviceId(Context context) {

		String deviceId = Settings.Secure.getString( context.getContentResolver(), Settings.Secure.ANDROID_ID);
		return deviceId;
	}

	/**
	 * Return a somewhat human readable set of device information, OS, etc.
	 * @param context
	 * @return
	 */
	public static JSONObject getDeviceInfo(Context context){

		JSONObject j = new JSONObject();
		try {
			j.put("os_version", System.getProperty("os.version"));
			j.put("os_name", System.getProperty("os.name"));
			j.put("incremental",  android.os.Build.VERSION.INCREMENTAL);
			j.put("sdk", android.os.Build.VERSION.SDK_INT);
			j.put("sdk_details", getSdkDetails());
			j.put("device", android.os.Build.DEVICE);
			j.put("model", android.os.Build.MODEL);
			j.put("product",android.os.Build.PRODUCT);
			j.put("unique_device_id", getUniqueDeviceId(context));
		} catch (JSONException e) {
		}
		return j;
	}

	public static String getSdkDetails() {

		int sdk = android.os.Build.VERSION.SDK_INT;

		switch (sdk){

			case 3: return "Android 1.5 API 3 Cupcake";
			case 4: return "Android 1.6 API 4 Donut";
			case 5:
			case 6:
			case 7: return "Android 2.0 - 2.1 API 5-7 Eclair";
			case 8: return "Android 2.2 - 2.2.3 API 8 Froyo";
			case 9:
			case 10: return "Android 2.3 - 2.3.7 API 9-10 Gingerbread";
			case 11:
			case 12:
			case 13: return "Android 3.0 - 3.2.6 API 11-13 Honeycomb";
			case 14:
			case 15: return "Android 4.0 - 4.0.4 API 14-15 Ice Cream Sandwich";
			case 16:
			case 17:
			case 18: return "Android 4.1 - 4.3.1 API 16-18 Jelly Bean";
			case 19:
			case 20: return "Android 4.4 - 4.4.4 API 19-20 KitKat";
			case 21:
			case 22: return "Android 5.0 - 5.1.1 API 21-22 Lollipop";
			case 23: return "Android 6.0 - 6.0.1 API 23 Marshmallow";
			case 24:
			case 25: return "Android 7.0 - 7.1 API 24-25 Nougat";
			case 26: return "Android 8.0.0 26 Oreo";
			case 27: return "Android 8.1.0 27 Oreo";
			case 28: return "Android 9 28 Pie";
		}

		return String.valueOf( sdk );
	}

	/**
	 * Retrieves phone model
	 * @return
	 */
	public static String getMakeModelName() {
		String manufacturer = android.os.Build.MANUFACTURER;
		String model = android.os.Build.MODEL;

		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}
	//Used for the phone model
	private static String capitalize(String s) {

		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	/**
	 * Test if a specific intent can be run on this device.  They user may not have
	 * GoogleMaps installed, or an app for email.
	 * @param context
	 * @param intent
	 * @return
	 */
	public static boolean isIntentAvailable(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list =
				packageManager.queryIntentActivities(intent,
						PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
}
