/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.license;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.nuvolect.deepdive.util.TimeUtil;

@SuppressLint("CommitPrefEdits")
public class LicensePersist {

    private static final String PERSIST_NAME           = "license_persist";

    // Persist keys
    private static final String APP_VERSION            = "app_version";
    private static final String LEGAL_AGREE            = "legal_agree";
    private static final String LEGAL_AGREE_TIME       = "legal_agree_time";
    private static final String LICENSE_RESULT         = "license_result";

    /**
     * Remove all persistent data.
     */
    public static void clearAll(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().clear().commit();
    }

    public static boolean getLegalAgree(Context ctx) {

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        return pref.getBoolean(LEGAL_AGREE, false);
    }

    public static void setLegalAgree(Context ctx, boolean legalAgree){

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        pref.edit().putBoolean(LEGAL_AGREE, legalAgree).commit();
        pref.edit().putLong(LEGAL_AGREE_TIME, System.currentTimeMillis()).commit();
    }

    /**
     * Return the human readable summary of the current license.
     * @param ctx
     * @return
     */
    public static String getLicenseSummary(Context ctx) {

        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        long legalAgreeTime = pref.getLong(LEGAL_AGREE_TIME, 0L);

        LicenseManager.LicenseResult licenseResult = getLicenseResult(ctx);
        String summary = "";

        switch( licenseResult) {

            case NIL:
                summary = "ERROR NIL license type";
                break;
            case REJECTED_TERMS:
                summary = "User rejected terms " + TimeUtil.friendlyTimeString(legalAgreeTime);
                break;
            case WHITELIST_USER:
                summary = "License: Whitelist"
                        + "\nUser accepted terms " + TimeUtil.friendlyTimeString(legalAgreeTime);
                break;
            case PRO_USER:{
                summary = "License: Pro User"
                        + "\nUser accepted terms " + TimeUtil.friendlyTimeString(legalAgreeTime);
                break;
            }
            default:
                break;
        }
        return summary;
    }

    public static void setLicenseResult(Context ctx, LicenseManager.LicenseResult licenseResult) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        pref.edit().putInt(LICENSE_RESULT, licenseResult.ordinal()).commit();
    }
    public static LicenseManager.LicenseResult getLicenseResult(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return LicenseManager.LicenseResult.values()[pref.getInt(LICENSE_RESULT, 0)];
    }


    public static int getAppVersion(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getInt(APP_VERSION, 0);
    }
    public static void setAppVersion(Context ctx, int version){
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(APP_VERSION, version).commit();
    }
}
