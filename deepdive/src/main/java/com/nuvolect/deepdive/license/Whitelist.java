package com.nuvolect.deepdive.license;

import android.content.Context;

import com.nuvolect.deepdive.util.DeviceInfo;

import java.util.HashSet;
import java.util.Set;

public class Whitelist {

	/**
	 * Check if a device is on the whitelist.  This is done using the a unique install ID
	 * in place of an email to avoid having to use the accounts permission.
	 * @param ctx
	 * @return
	 */
	public static String getWhiteListCredentials(Context ctx) {

		String uniqueDeviceId = DeviceInfo.getUniqueInstallId(ctx);

		if( developers.contains(uniqueDeviceId))
			return uniqueDeviceId;
		else
			return "";
	}
//	/**
//	 * Check if user is on whitelist.
//	 * @param ctx
//	 * @return  Returns the email address of a developer otherwise an empty string.
//	 */
//	public static String onWhitelist(Context ctx) {
//
//        Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE);
//
//        Account[] myAccounts = AccountManager.get(ctx).getAccounts();
//
//        for (Account myAccount : myAccounts) {
//
//            String account_email = myAccount.name.toLowerCase(Locale.US).trim();
//
//            if (EMAIL_PATTERN.matcher(account_email).matches()){
//
//                if( developers.contains( account_email))
//                    return account_email;
//            }
//        }
//
//		return "";
//	}

	/** Build the set of whitelist emails, all must be lower case */
	private static Set<String> developers = new HashSet<String>() {
		private static final long serialVersionUID = 1L;
	{
//		add("bfdcdf9d4013bd90"); // Matt's nexus 9
        add("d5e36d9ee98729a6"); // Matt's samsung 9

	}};

}
