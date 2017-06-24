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
import com.nuvolect.deepdive.util.DeviceInfo;
import com.nuvolect.deepdive.util.DialogUtil;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.TimeUtil;

import org.headsupdev.license.License;
import org.headsupdev.license.LicenseDecoder;
import org.headsupdev.license.LicenseUtils;

import java.text.SimpleDateFormat;
import java.util.Date;


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
 * 2 Confirm app version has not expired, note below. LicenseResult.APP_EXPIRED
 *
 * 3 Check for whitelist user, LicenseResult.WHITELIST_USER
 *
 * 4.a Check for no key, invalid key, LicenseResult.EMPTY_KEY, LicenseResult.INVALID_KEY
 * 4.b Check for invalid device, LicenseResult.INVALID_DEVICE
 * 4.c Check for pro user, license not expired, LicenseResult.PRO_USER
 * 4.d Check for pro user, license expired, LicenseResult.PRO_USER_EXPIRED
 *      The user will always be a pro user but when license period expires
 *      the user will lose nearly all pro privileges.
 *
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
        EMPTY_KEY,
        INVALID_KEY,
        INVALID_DEVICE,
        PRO_USER,
        PRO_USER_EXPIRED,  // Read access external storage, otherwise same as APPERCIATED_USER
    }

    /**
     * App expire enumerations
     */
    public enum AppExpireStatus { NIL,
        APP_VALID,
        APP_EXPIRE_WITHIN_30_DAYS,
        APP_EXPIRE_WITHIN_7_DAYS,
        APP_EXPIRED,
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
    private void step_2_confirm_version_not_expired(){

        AppExpireStatus appExpireStatus = getAppExpireStatus();

        if( appExpireStatus != AppExpireStatus.APP_EXPIRED){

            step_3_check_for_whitelist_user(); // app is still valid
        }else{

            mLicenseSummary = "App version expired";
            mListener.licenseResult(LicenseResult.APP_EXPIRED);
            // All done here, calling class will take over with returned result
            return;
        }
    }

    private void step_3_check_for_whitelist_user(){
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

            step_4a_check_for_invalid_key();
        }
    }

    private static License out;
    private static String m_licenseDeviceId;
    private static String m_licenseDate;
    private static String m_licensePeriodDays;

    private void step_4a_check_for_invalid_key(){

        if (DEBUG) LogUtil.log("LicenseManager: step_4b_check_for_invalid_key");

        String encodedLicense = LicensePersist.getLicenseCryp( m_act).trim();

        if( encodedLicense.isEmpty()){

            mLicenseSummary = "App license key is invalid";
            mListener.licenseResult(LicenseResult.EMPTY_KEY);
            // All done here, calling class will take over with returned result
            return;
        }

        AppConfig config = new AppConfig();
        out = new License();

        LicenseDecoder decoder = new LicenseDecoder();
        try {
            decoder.setPublicKey( LicenseUtils.deserialiseKey( config.getPublicKeyFile(m_act) ) );
            decoder.setSharedKey( LicenseUtils.deserialiseKey( config.getSharedKeyFile(m_act) ) );
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean licenseFailed = false;

        License out = new License();
        try {
            decoder.decodeLicense( encodedLicense, out );
        } catch ( Exception e ) {
            licenseFailed = true;
            LogUtil.log( "License decoding failed..." );
            // handle however...
        }

        if( licenseFailed ) {

            mLicenseSummary = "App license key is invalid";
            mListener.licenseResult(LicenseResult.INVALID_KEY);
            // All done here, calling class will take over with returned result
            return;
        }

        m_licenseDeviceId = out.getLicenseDeviceId();
        m_licenseDate = out.getLicenseDate();
        m_licensePeriodDays = out.getLicensePeriodDays();

        LogUtil.log( "License decoding success, licenseName      : "+out.getLicenseName());
        LogUtil.log( "License decoding success, licenseDeviceId  : "+out.getLicenseDeviceId());
        LogUtil.log( "License decoding success, licenseDate      : "+out.getLicenseDate());
        LogUtil.log( "License decoding success, licensePeriodDays: "+out.getLicensePeriodDays());

        step_4b_check_for_invalid_device();
    }

    private void step_4b_check_for_invalid_device(){

        if (DEBUG) LogUtil.log("LicenseManager: step_4b_check_for_invalid_device");

        String deviceId = DeviceInfo.getUniqueDeviceId( m_act);
        String licenseId = m_licenseDeviceId;

        if( licenseId == null || licenseId.isEmpty() || ! deviceId.contentEquals( licenseId)) {

            mLicenseSummary = "App license invalid for this device ("+ deviceId +")";
            mListener.licenseResult(LicenseResult.INVALID_DEVICE);
            // All done here, calling class will take over with returned result
           return;
        }

        step_4cd_check_license_not_expired();
    }

    private void step_4cd_check_license_not_expired(){

        if (DEBUG) LogUtil.log("LicenseManager: step_4cd_check_license_not_expired");

        // Extra day to get you through midnight of the day it expires.
        long licensePeriodMs = (Long.parseLong( m_licensePeriodDays) +1) * 24 * 60 * 60 * 1000;
        long licenseEndMs = 0;

        try {
            String dateString = m_licenseDate;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            Date date = sdf.parse(dateString);

            // End time is the time the license started plus the length of the license.
            licenseEndMs = date.getTime() + licensePeriodMs;

        } catch (Exception e) {
            LogUtil.logException(LicenseManager.class, e);
        }

        if( DEBUG){
            LogUtil.log("licenseEnd: "+ TimeUtil.friendlyTimeString( licenseEndMs));
        }

        if( System.currentTimeMillis() < licenseEndMs){

            mIsProUser = true;
            mLicenseSummary = "Pro user";
            mListener.licenseResult(LicenseResult.PRO_USER);
            // All done here, calling class will take over with returned result
            return;
        }else{

            mIsProUserExpired = true;
            mLicenseSummary = "Pro user, license expired";
            mListener.licenseResult(LicenseResult.PRO_USER_EXPIRED);
            // All done here, calling class will take over with returned result
            return;
        }
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

        String deviceId = DeviceInfo.getUniqueDeviceId( act);

        //FIXME develop/test upgrade when license expires

        DialogUtil.twoButtonMlDialog( act, "Upgrade or renew license",
                "Go to nuvolect.com/deepdive and use device ID: "+ deviceId,
                "Not now", "Upgrade", new DialogUtil.DialogUtilCallbacks() {
                    @Override
                    public void confirmed(boolean confirmed) {

                        if( confirmed ){

                            if( ! LicensePersist.isProUser( act)){

                                LicensePersist.setIsProUser( act, true);

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

    /**
     * Return the status of when the app will expire.
     * @return
     */
    public static AppExpireStatus getAppExpireStatus() {

        Date buildDate = new Date(BuildConfig.BUILD_TIMESTAMP);
        long appBuildTimeDate = buildDate.getTime();

        long timeExpiresHalfYear = appBuildTimeDate + CConst.DURATION_HALF_YEAR_MS;
        long timeExpires30Days = timeExpiresHalfYear - CConst.DURATION_30_DAYS_MS;
        long timeExpires7Days = timeExpiresHalfYear - CConst.DURATION_7_DAYS_MS;

        if( System.currentTimeMillis() > timeExpiresHalfYear) {

            return AppExpireStatus.APP_EXPIRED;
        }
        if( System.currentTimeMillis() > timeExpires7Days) {

            return AppExpireStatus.APP_EXPIRE_WITHIN_7_DAYS;
        }
        if( System.currentTimeMillis() > timeExpires30Days) {

            return AppExpireStatus.APP_EXPIRE_WITHIN_30_DAYS;
        }

        return AppExpireStatus.APP_VALID;
    }

    public static String getAppExpireDate() {

        Date buildDate = new Date(BuildConfig.BUILD_TIMESTAMP);
        long appBuildTimeDate = buildDate.getTime();
        long timeExpiresHalfYear = appBuildTimeDate + CConst.DURATION_HALF_YEAR_MS;

        return TimeUtil.getFriendlyDate( timeExpiresHalfYear);
    }


    public static boolean isWhitelistUser() {
        return mIsWhitelistUser;
    }

    public static boolean isProUser() {
        return mIsProUser;
    }

    public static boolean isFreeUser() {
//        return ! mIsProUser;//pro-user
        return true;
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
