package com.nuvolect.deepdive.license;

import android.content.Context;

import com.nuvolect.deepdive.util.DeviceInfo;

import java.util.HashSet;
import java.util.Set;

public class Whitelist {

	/**
	 * Check if a device is on the whitelist.  This is done using the a unique device ID
	 * in place of an email to avoid having to use the accounts permission.
	 * @param ctx
	 * @return
	 */
	public static String getWhiteListCredentials(Context ctx) {

		String uniqueDeviceId = DeviceInfo.getUniqueDeviceId(ctx);

		if( developers.contains(uniqueDeviceId))
			return uniqueDeviceId;
		else
			return "";
	}

	/** Build the set of whitelist emails, all must be lower case */
	private static Set<String> developers = new HashSet<String>() {
		private static final long serialVersionUID = 1L;
	{
//		add("bfdcdf9d4013bd90"); // Matt's nexus 9
        add("d5e36d9ee98729a6"); // Matt's samsung 9
		add("f558c6d483a6c1c2"); // Matt's moto x
		add("aaf80fa7c5f683d6"); // Matt's moto x

	}};

}
