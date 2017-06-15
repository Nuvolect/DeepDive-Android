package com.nuvolect.deepdive.license;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nuvolect.deepdive.main.CConst;
import com.nuvolect.deepdive.util.CrypUtil;
import com.nuvolect.deepdive.util.DeviceInfo;
import com.nuvolect.deepdive.util.JsonUtil;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.TimeUtil;

import org.headsupdev.license.License;
import org.headsupdev.license.LicenseDecoder;
import org.headsupdev.license.LicenseUtils;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint("CommitPrefEdits")
public class LicensePersist {

    private static final String PERSIST_NAME           = "license_persist";

    // Persist keys
    private static final String EARLY_ADOPTER          = "early_adopter";
    private static final String LAST_NAG_TIME          = "last_nag_time";
    private static final String LAST_PITCH             = "last_pitch";
    private static final String LEGAL_AGREE            = "legal_agree";
    private static final String LEGAL_AGREE_TIME       = "legal_agree_time";
    private static final String LICENSE_RESULT         = "license_result";
    private static final String PRO_USER               = "pro_user";
    private static final String ENCODED_LICENSE        = "encoded_license";
    private static final String PRO_USER_UPGRADE_TIME  = "pro_user_upgrade_time";
    private static final String LICENSE_CRYP           = CConst.LICENSE_CRYP;// match settings.xml

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
                LicenseDecoder decoder = new LicenseDecoder();
                try {
                    AppConfig config = new AppConfig();
                    decoder.setPublicKey(LicenseUtils.deserialiseKey(config.getPublicKeyFile(ctx)));
                    decoder.setSharedKey(LicenseUtils.deserialiseKey(config.getSharedKeyFile(ctx)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String licDetails = "";

                License out = new License();
                try {
                    String licenseCryp = LicensePersist.getLicenseCryp( ctx);
                    decoder.decodeLicense( licenseCryp, out);
                } catch (Exception e) {
                    licDetails = "License invalid";
                    LogUtil.log( licDetails);
                }
                if( licDetails.isEmpty()){// no decoder error, get license specifics

                    licDetails = out.getSummary();
                }

                summary = "License: Pro User"
                        + "\nUser accepted terms " + TimeUtil.friendlyTimeString(legalAgreeTime)
                        + "\n" + licDetails;
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

    /**
     * Keep track of when you are nagging the user on various issues.
     * Return false when current time is within a sincePeriod, it is not time to nag.
     * This method can can be called multiple times and will continue to return false until outside
     * of the pest period when it returns true a single time.
     *
     * @param ctx
     * @param key key to find last nag time
     * @param noNagPeriodMs  How long the period is in ms
     * @return
     */
    public static boolean timeToNagUser(Context ctx, String key, long noNagPeriodMs) {

        long currentTime = System.currentTimeMillis();

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        try {

            JSONObject object = new JSONObject( pref.getString(LAST_NAG_TIME, "{}"));
            if( object.has( key )){

                long timeSinceLastNag = currentTime - JsonUtil.getLong(key, object);

                /**
                 * Check if we are outside the no-nag period
                 */
                if( timeSinceLastNag > noNagPeriodMs){

                    /**
                     * Time to nag the user again.
                     */
                    object.put(key, currentTime);
                    pref.edit().putString(LAST_NAG_TIME, object.toString()).commit();

                    return true;
                }else{
                    /**
                     * You are a pest and within the pest period, return true
                     */
                    return false;
                }
            }else{
                /**
                 * First time, definitely time to nag the user
                 */
                object.put(key, currentTime);
                pref.edit().putString(LAST_NAG_TIME, object.toString()).commit();

                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Returns the current app version or zero when the app version has never been set.
     * @param ctx
     * @return
     */
    public static int getAppVersion(Context ctx) {

        return CrypUtil.getInt(ctx, CrypUtil.APP_VERSION);
    }

    public static void setAppVersion(Context ctx, int appVersion) {

        CrypUtil.putInt(ctx, CrypUtil.APP_VERSION, appVersion);
    }

    public static void setIsProUser(Context ctx, boolean b) {

        CrypUtil.put( ctx, PRO_USER, b?"true":"false");
    }

    public static boolean isInstallIdMatch(Context ctx){

        String installId = DeviceInfo.getUniqueInstallId( ctx);
        boolean installIdMatch = false;

        try {
            JSONObject json = new JSONObject( CrypUtil.get( ctx, ENCODED_LICENSE, "{}"));

            if( json.getString("install_id").contentEquals( installId)){
                installIdMatch = true;
            }

        } catch (JSONException e) {
            installIdMatch = false;
        }

        return installIdMatch;
    }

    public static boolean isLicensePeriodValid(Context ctx){

        boolean licensePeriodIsValid = false;

        try {
            JSONObject json = new JSONObject( CrypUtil.get( ctx, ENCODED_LICENSE, "{}"));
            long licenseDate = json.getLong("LICENSE_DATE");

            long timeProExpires = licenseDate + CConst.DURATION_1_YEAR_MS;

            if( System.currentTimeMillis() < timeProExpires){
                licensePeriodIsValid = true;
            }
        } catch (JSONException e) {
            LogUtil.logException( LicensePersist.class, e);
        }

        return licensePeriodIsValid;
    }

    public static boolean isProUser(Context ctx) {

        return CrypUtil.get( ctx, PRO_USER, "false").contentEquals("true");
    }

    public static long getProUserUpgradeTime(Context ctx) {

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        return pref.getLong(PRO_USER_UPGRADE_TIME, 0);
    }

    public static void setProUserUpgradeTime(Context ctx ){

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        pref.edit().putLong( PRO_USER_UPGRADE_TIME, System.currentTimeMillis()).commit();
    }

    /**
     * Return encrypted license key as saved in app Settings.
     * @param ctx
     * @return
     */
    public static String getLicenseCryp(Context ctx ){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences( ctx);
        return sharedPref.getString(CConst.LICENSE_CRYP, "");
    }

    public static void putLicenseCryp(Context ctx, String licenseCryp){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences( ctx);
        sharedPref.edit().putString(CConst.LICENSE_CRYP, licenseCryp).apply();
    }

}
