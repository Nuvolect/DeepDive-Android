/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.license;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.nuvolect.deepdive.util.LogUtil;


/**
 * Manage the license related activities of startup and execution.
 * In the onCreate method of each entry class of the app call LicenseManager with
 * a listener to get the LicenseResult.
 * <pre>
 * Startup process:
 *
 * 1. Test for first time startup, if so
 * 1.a Prompt for concurrence with terms and conditions, LicenseResult.REJECT_TERMS
 *
 * 2 Check for whitelist user, LicenseResult.WHITELIST_USER
 *</pre>
 */
public class LicenseManager {

    private final boolean DEBUG = LogUtil.DEBUG;

    /**
     * License type is saved in the ordinal position.
     */
    public enum LicenseResult { NIL,
        REJECTED_TERMS,
        WHITELIST_USER,
        PRO_USER,
    }

    /**
     * App expire enumerations
     */
    public enum AppExpireStatus { NIL,
        APP_VALID,
    }

    private Activity m_act;
    private static LicenseManager sInstance;


    private static boolean mIsWhitelistUser;
    private static boolean mIsProUser;

    /** Short description of current license for the Settings page */
    public String mLicenseSummary = "";

    private LicenseCallbacks mListener;
    AlertDialog dialog_alert = null;
    /**
     * Manage the class as a singleton.
     * @param context
     * @return
     */
    public static LicenseManager getInstance(Context context) {
        if (sInstance == null) {
            //Always pass in the Application Context
            sInstance = new LicenseManager(context.getApplicationContext());

            mIsWhitelistUser = false;// is also a pro user
            mIsProUser = true;
        }
        return sInstance;
    }

    private LicenseManager(Context context) {
    }

    /**
     * A callback interface that all activities containing this class must implement.
     */
    public interface LicenseCallbacks {

        public void licenseResult(LicenseResult licenseResult);
    }

    public void checkLicense(Activity act, LicenseCallbacks listener){
        if(DEBUG)LogUtil.log( "LicenseManager: step_0");

        m_act = act;
        mListener = listener;

        step_1a_check_concurrence_with_terms();
    }

    private void step_1a_check_concurrence_with_terms() {
        if(DEBUG)LogUtil.log( "LicenseManager: step_1a_check_concurrence_with_terms");

        if( LicensePersist.getLegalAgree(m_act)){

            step_2_check_for_whitelist_user();

        }else{

            String message = "By using this application you agree to "+AppSpecific.TOC_HREF_URL
                    +" and "+AppSpecific.PP_HREF_URL+". "+
                    "Use of this software is for education and training purposes only.";

            AlertDialog.Builder builder = new AlertDialog.Builder(m_act);
            builder.setTitle("Please confirm Terms and Conditions and Privacy Policy");
            builder.setMessage( Html.fromHtml(message));
            builder.setCancelable(false);
            builder.setIcon(AppSpecific.SMALL_ICON);

            builder.setPositiveButton("I Agree", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int labelIndex) {

                    LicensePersist.setLegalAgree(m_act, true);

                    step_2_check_for_whitelist_user();
                }

            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    mListener.licenseResult( LicenseResult.REJECTED_TERMS);
                    dialog_alert.cancel();
                    // All done here, calling class will take over with returned result
                    return;
                }
            });
            dialog_alert = builder.create();
            dialog_alert.show();

            // Activate the HTML
            TextView tv = ((TextView) dialog_alert.findViewById(android.R.id.message));
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void step_2_check_for_whitelist_user(){
        if(DEBUG)LogUtil.log( "LicenseManager: step_3_check_for_whitelist_user");

        String whiteListAccount = Whitelist.getWhiteListCredentials(m_act);

        if( ! whiteListAccount.isEmpty()) {

            mIsProUser = true;
            mIsWhitelistUser = true;
            mLicenseSummary = "Whitelist user: " +whiteListAccount;
            mListener.licenseResult( LicenseResult.WHITELIST_USER);
            // All done here, calling class will take over with returned result
            return;
        }else{

            mIsProUser = true;
            mIsWhitelistUser = false;
            mLicenseSummary = "Pro user";
            mListener.licenseResult( LicenseResult.PRO_USER);
            // All done here, calling class will take over with returned result
            return;
        }
    }

    public static boolean isWhitelistUser() {
        return mIsWhitelistUser;
    }

    public static boolean isProUser() {
        return mIsProUser;
    }

    public static void setIsWhitelistUser(boolean mIsWhitelistUser) {
        LicenseManager.mIsWhitelistUser = mIsWhitelistUser;
    }

    public static void setIsProUser(boolean mIsProUser) {
        LicenseManager.mIsProUser = mIsProUser;
    }

}
