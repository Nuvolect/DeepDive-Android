package com.nuvolect.deepdive.license;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.nuvolect.deepdive.BuildConfig;
import com.nuvolect.deepdive.main.CConst;
import com.nuvolect.deepdive.util.Analytics;
import com.nuvolect.deepdive.util.DialogUtil;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.TimeUtil;

import java.util.Date;


/**
 * Class to manage the license related activities of startup.
 * In the onCreate method of each entry class of the app call LicenseManager with
 * a listener to get the LicenseResult.
 * <pre>
 * Startup process:
 *
 * 1. Test for first time startup, if so
 * 1.a Prompt for concurrence with terms and conditions, LicenseResult.REJECT_TERMS
 *
 * 2 Confirm app version has not expired, note below. LicenseResult.APP_EXPIRED
 *
 * 3 Check for whitelist user, LicenseResult.WHITELIST_USER
 *
 * 4.a Check for pro user, license not expired, LicenseResult.PRO_USER
 * 4.b Check for pro user, license expired, LicenseResult.PRO_USER_EXPIRED
 *      The user will always be a pro user but when license period expires
 *      the user will lose nearly all pro privileges.
 *
 * 5 User not white_list or pro user is an appreciated user, LicenseResult.APPRECIATED_USER
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 * Pro user states
 *
 * New user, not yet a pro user
 *     isProUser: false, isProUserExpired: false  - an appreciated user
 *
 * User becomes a pro user
 *     isProUser: true, isProUserExpired: false   - an active pro user
 *
 * User chooses not to renew pro user license
 * isProUser: false, isProUserExpired: true   - license expired, was a pro user
 *
 * User later re-upgades to pro user
 *     isProUser: true, isProUserExpired: false   - an active pro user
 *
 * Both booleans cannot be true.  You cannot be a pro user and be expired.
 *
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 * About LicenseResult.APP_EXPIRED
 *
 * The purpose is to defeat rogue and outdated versions of the app.
 *
 * Many illicit copies of the app are made and distributed and even sold outside
 * of Google Play and outside of the control of Nuvolect. This app is not open source
 * and is not free, it is for-profit and illicit copies can interfere with Nuvolect's
 * rights and business models.
 *
 * A hard-date will be used to expire a version of the app.
 *
 * The user will be notified within 30 days of expiring:
 *   "This app is getting old and requires update by: mm/dd/yyyy"
 *
 * When the app expires a dialog is shown requesting the user to upgrade.
 *
 * Each version of the app published has an absolute expire date.
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 *</pre>
 */
public class LicenseManager {

    private final boolean DEBUG = LogUtil.DEBUG;

    /**
     * License type is saved in the ordinal position.
     */
    public enum LicenseResult { NIL,
        REJECTED_TERMS,
        APP_EXPIRED,
        WHITELIST_USER,
        PRO_USER,
        PRO_USER_EXPIRED,  // Read access external storage, otherwise same as APPERCIATED_USER
        APPRECIATED_USER,
    }

    private Activity m_act;
    private static LicenseManager sInstance;


    private static boolean mIsWhitelistUser;
    private static boolean mIsProUser;
    private static boolean mIsProUserExpired;

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
            mIsProUser = false;
            mIsProUserExpired = false;
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

            step_2_confirm_version_not_expired();

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

                    step_2_confirm_version_not_expired();
                }

            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    mListener.licenseResult( LicenseResult.REJECTED_TERMS);
                    dialog_alert.cancel();
                    // All done here, calling class will take over with returned result
                }
            });
            dialog_alert = builder.create();
            dialog_alert.show();

            // Activate the HTML
            TextView tv = ((TextView) dialog_alert.findViewById(android.R.id.message));
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
    void step_2_confirm_version_not_expired(){

        Date buildDate = new Date(BuildConfig.BUILD_TIMESTAMP);
        long appBuildTimeDate = buildDate.getTime();
        long timeAppExpires = appBuildTimeDate + CConst.APP_VALID_DURATION;

        if( System.currentTimeMillis() < timeAppExpires){

            step_3_check_for_whitelist_user(); // app is still valid
        }else{

            mLicenseSummary = "App version expired";
            mListener.licenseResult(LicenseResult.APP_EXPIRED);
            // All done here, calling class will take over with returned result
        }
    }

    void step_3_check_for_whitelist_user(){
        if(DEBUG)LogUtil.log( "LicenseManager: step_3_check_for_whitelist_user");

        String whiteListAccount = Whitelist.getWhiteListCredentials(m_act);

        if( ! whiteListAccount.isEmpty()) {

            mIsProUser = true;
            mIsWhitelistUser = true;
            mLicenseSummary = "Whitelist user: " +whiteListAccount;
            mListener.licenseResult( LicenseResult.WHITELIST_USER);
            // All done here, calling class will take over with returned result
        }else{

            step_4_check_for_pro_user_license_not_expired();
        }
    }

    void step_4_check_for_pro_user_license_not_expired(){

        if (DEBUG) LogUtil.log("LicenseManager: step_4_check_for_pro_user");

        if( LicensePersist.isProUser(m_act)) {

            long timeLastProUpgrade = LicensePersist.getProUserUpgradeTime( m_act);
            long timeProExpires = timeLastProUpgrade + CConst.PRO_LICENSE_DURATION;

            if( DEBUG){
                LogUtil.log("timeLastProUpgrade: "+ TimeUtil.friendlyTimeString( timeLastProUpgrade));
                LogUtil.log("timeProExpires    : "+ TimeUtil.friendlyTimeString( timeProExpires));
            }

            if( System.currentTimeMillis() < timeProExpires){

                mIsProUser = true;
                mLicenseSummary = "Pro user";
                mListener.licenseResult(LicenseResult.PRO_USER);
                // All done here, calling class will take over with returned result
            }else{

                mIsProUserExpired = true;
                mLicenseSummary = "Pro user, license expired";
                mListener.licenseResult(LicenseResult.PRO_USER_EXPIRED);
                // All done here, calling class will take over with returned result
            }

        }else{
            step_5_user_is_appreciated_user();
        }
    }

    private void step_5_user_is_appreciated_user() {

        mLicenseSummary = "Appreciated user";
        mListener.licenseResult(LicenseResult.APPRECIATED_USER);
        // All done here, calling class will take over with returned result
    }

    /**
     * Set the user as a pro user.
     * Set the current time as when user upgrade.
     *
     * @param act
     */
    public static void upgradeLicense(final Activity act) {

        Analytics.send( act,
                Analytics.MAIN_MENU,
                Analytics.UPGRADE_MENU,
                Analytics.COUNT, 1);

        DialogUtil.twoButtonMlDialog( act, "Upgrade to Pro",
                "Early adopters can upgrade to Pro for free for six months, no catch.",
                "Not now", "Upgrade", new DialogUtil.DialogUtilCallbacks() {
                    @Override
                    public void confirmed(boolean confirmed) {

                        if( confirmed ){

                            if( ! LicensePersist.isProUser( act)){

                                LicensePersist.setIsProUser( act, true);
                                LicensePersist.setProUserUpgradeTime( act);

                                DialogUtil.dismissDialog( act, "A New Pro User!",
                                        "Congrats! You are now a Pro User!");

                                Analytics.send( act,
                                        Analytics.MAIN_MENU,
                                        Analytics.UPGRADE_SELECT,
                                        Analytics.COUNT, 1);
                            }
                            else {
                                DialogUtil.dismissDialog( act, "A Pro User!",
                                        "This is great, you are already a Pro User!");
                            }
                        }else{
                            Analytics.send( act,
                                    Analytics.MAIN_MENU,
                                    Analytics.UPGRADE_CANCEL,
                                    Analytics.COUNT, 1);
                        }
                    }
                });
    }

    public static boolean isWhitelistUser() {
        return mIsWhitelistUser;
    }

    public static boolean isProUser() {
        return mIsProUser;
    }

    public static boolean isProUserExpired() {
        return mIsProUserExpired;
    }

    public static void setIsWhitelistUser(boolean mIsWhitelistUser) {
        LicenseManager.mIsWhitelistUser = mIsWhitelistUser;
    }

    public static void setIsProUser(boolean mIsProUser) {
        LicenseManager.mIsProUser = mIsProUser;
    }

    public static void setIsProUserExpired(boolean mIsProUserExpired) {
        LicenseManager.mIsProUserExpired = mIsProUserExpired;
    }

}
