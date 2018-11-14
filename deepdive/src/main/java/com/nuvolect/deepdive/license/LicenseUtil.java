/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.license;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LicenseUtil {

	/**
	 * Return a unique string for the device.  This string only changes when you wipe the device
	 * and reinstall Android.
	 * @param context
	 * @return unique device ID string
	 */
	public static String getUniqueInstallId(Context context) {

		String deviceId = Settings.Secure.getString( context.getContentResolver(), Settings.Secure.ANDROID_ID);
		return deviceId;
	}

    /**
     * Generate a 32 character length hex md5 string from plain source text.
     * @param plaintext
     * @return
     */
    public static String md5(String plaintext){

        MessageDigest m;
        try {
            m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(plaintext.getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1,digest);
            String hashtext = bigInt.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while(hashtext.length() < 32 ){
                hashtext = "0"+hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Detect if the app has been upgraded.
     * A new installation returns false, i.e, is not an upgrade,
     * it is a new installation.
     * @param ctx
     * @return  True for an app upgrade, false when not upgraded and for new installation.
     */
    public static boolean appUpgraded(Context ctx) {

        int appVersion = 0;
        try {
            appVersion = ctx.getPackageManager().getPackageInfo(
                    ctx.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        int previousAppVersion = LicensePersist.getAppVersion( ctx);

        if( appVersion > previousAppVersion) {

            LicensePersist.setAppVersion( ctx, appVersion);

            if (previousAppVersion > 0)
                return true;  // App is upgraded and not a new install
        }
        return false;
    }
}
