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
 * Class to manage the license related activities of startup.
 * In the onCreate method of each entry class of the app call LicenseManager with
 * a listener to get the LicenseResult.
 * <pre>
 * Startup process:
 *
 1. Test for first time startup, if so
 1.a Prompt for concurrence with terms and conditions, LicenseResult.REJECT_TERMS

 2 Confirm app version has not expired. LicenseResult.APP_EXPIRED

 3 Check for whitelist user, LicenseResult.WHITELIST_USER

 4.a Check for pro user, license not expired, LicenseResult.PRO_USER
 4.b Check for pro user, license expired, LicenseResult.PRO_USER_EXPIRED

 5 User not white_list or pro user is an appreciated user, LicenseResult.APPRECIATED_USER
 *
 *</pre>
 */
public class LicenseManager {

    private final boolean DEBUG = LogUtil.DEBUG;

    public static void upgradeLicense(Activity act) {//SPRINT implement

    }

    /**
     * License type is saved in the ordinal position, do not reorder this list.
     */
    public enum LicenseResult { NIL,
        REJECTED_TERMS,
        APP_EXPIRED,
        WHITELIST_USER,
        PRO_USER,
        PRO_USER_EXPIRED,  // Read access external storage, otherwise same as APPERCIATED_USER
        APPRECIATED_USER,
    }

    private Context m_ctx;
    private Activity m_act;
    private static LicenseManager sInstance;

    private static boolean mIsProUser = false; // Is the license process valid, we have a user?
    public static boolean mIsWhitelistUser = false; // Is the user on the developer whitelist?

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
        }
        return sInstance;
    }

    private LicenseManager(Context context) {
        m_ctx = context;
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
                    +" and "+AppSpecific.PP_HREF_URL;

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
                }
            });
            dialog_alert = builder.create();
            dialog_alert.show();

            // Activate the HTML
            TextView tv = ((TextView) dialog_alert.findViewById(android.R.id.message));
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    void step_2_check_for_whitelist_user(){
        if(DEBUG)LogUtil.log( "LicenseManager: step_2_check_for_whitelist_user");

        String whiteListAccount = Whitelist.getWhiteListCredentials(m_act);

        if( ! whiteListAccount.isEmpty()) {

            mIsProUser = true;
            mIsWhitelistUser = true;
            mLicenseSummary = "Whitelist user: " +whiteListAccount;
            mListener.licenseResult( LicenseResult.WHITELIST_USER);
            // All done here, calling class will take over with returned result
        }else{

            step_3_check_for_early_adopter();
        }
    }

    /*
     */
    void step_3_check_for_early_adopter(){
        if(DEBUG)LogUtil.log( "LicenseManager: step_3_check_for_early_adopter");

        if( LicensePersist.isEarlyAdopter(m_act)){

            mListener.licenseResult( LicenseResult.APPRECIATED_USER);
            mLicenseSummary = "Pro user";
            mIsProUser = true;
        }else{
           // All new users are currently early adopters
            LicensePersist.setIsEarlyAdopter(m_ctx, true);
            mListener.licenseResult( LicenseResult.APPRECIATED_USER);
            mLicenseSummary = "Pro user";
            mIsProUser = true;
        }
//        else step_4_check_for_pro_user();
    }

    void step_4_check_for_pro_user(){

        if (DEBUG) LogUtil.log("LicenseManager: step_4_check_for_pro_user");

        if( LicensePersist.isPremiumUser(m_act)) {

            mListener.licenseResult(LicenseResult.APPRECIATED_USER);
            mLicenseSummary = "Pro user";
            mIsProUser = true;
        }else{

            mListener.licenseResult(LicenseResult.APPRECIATED_USER);
            mLicenseSummary = "Evaluation user";
            mIsProUser = true;
        }
    }
}
