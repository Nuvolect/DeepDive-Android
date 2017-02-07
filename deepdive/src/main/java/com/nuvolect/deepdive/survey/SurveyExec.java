package com.nuvolect.deepdive.survey;//

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.nuvolect.deepdive.R;
import com.nuvolect.deepdive.util.Analytics;
import com.nuvolect.deepdive.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static android.content.pm.PackageManager.GET_PERMISSIONS;

/**
 * Utilities to generate app permission metrics and publish survey results.
 */
public class SurveyExec {

    public static String APP_NAME                   = "appName";
    public static String PACKAGE_NAME               = "packageName";
    public static String FLAGS                      = "flags";
    public static String TARGET_SDK                 = "targetSdk";
    public static String MAKE_MODEL                 = "makeModel";
    public static String VERSION_CODE               = "versionCode";
    public static String VERSION_NAME               = "versionName";
    public static String LAST_UPDATE_TIME           = "lastUpdateTime";
    public static String FIRST_INSTALLATION_TIME    = "firstUpdateTime";

    public static String READ_CONTACTS              = "readContacts";
    public static String WRITE_CONTACTS             = "writeContacts";
    public static String READ_EXTERNAL_STORAGE      = "readExternalStorage";
    public static String WRITE_EXTERNAL_STORAGE     = "writeExternalStorage";
    public static String NETWORK_ACCESS             = "networkAccess";
    public static String RECEIVE_BOOT_COMPLETED     = "receiveBootCompleted";
    public static String INTERNET                   = "internet";
    public static String GET_ACCOUNTS               = "getAccounts";
    public static String CAMERA                     = "camera";
    public static String RECORD_AUDIO               = "recordAudio";
    public static String ACCESS_COARSE_LOCATION     = "accessCoarseLocation";
    public static String ACCESS_FINE_LOCATION       = "accessFineLocation";

    public static String READ_CALENDAR              = "readCalendar";
    public static String WRITE_CALENDAR             = "writeCalendar";
    public static String READ_PHONE_STATE           = "readPhoneState";
    public static String CALL_PHONE                 = "callPhone";
    public static String READ_CALL_LOG              = "readCallLog";
    public static String WRITE_CALL_LOG             = "writeCallLog";
    public static String ADD_VOICEMAIL              = "addVoicemail";
    public static String USE_SIP                    = "useSip";
    public static String PROCESS_OUTGOING_CALLS     = "processOutgoingCalls";
    public static String SEND_SMS                   = "sendSms";
    public static String RECEIVE_SMS                = "receiveSms";
    public static String READ_SMS                   = "readSms";
    public static String RECEIVE_WAP_PUSH           = "receiveWapPush";
    public static String RECEIVE_MMS                = "receiveMms";

    public static String FLAG_SYSTEM                    = "flagSystem";
    public static String FLAG_DEBUGGABLE                = "flagDebuggable";
    public static String FLAG_HAS_CODE                  = "flagHasCode";
    public static String FLAG_PERSISTENT                = "flagPersistent";
    public static String FLAG_FACTORY_TEST              = "flagFactoryTest";
    public static String FLAG_ALLOW_TASK_REPARENTING    = "flagAllowTaskReparenting";
    public static String FLAG_ALLOW_CLEAR_USER_DATA     = "flagAllowClearUserData";
    public static String FLAG_UPDATED_SYSTEM_APP        = "flagUpdatedSystemApp";
    public static String FLAG_TEST_ONLY                 = "flagTestOnly";
    public static String FLAG_SUPPORTS_SMALL_SCREENS    = "flagSupportsSmallScreens";
    public static String FLAG_SUPPORTS_NORMAL_SCREENS   = "flagSupportsNormalScreens";
    public static String FLAG_SUPPORTS_LARGE_SCREENS    = "flagSupportsLargeScreens";
    public static String FLAG_RESIZABLE_FOR_SCREENS     = "flagResizableForScreens";
    public static String FLAG_SUPPORTS_SCREEN_DENSITIES = "flagSupportsScreenDensities";
    public static String FLAG_VM_SAFE_MODE              = "flagVmSafeMode";
    public static String FLAG_ALLOW_BACKUP              = "flagAllowBackup";
    public static String FLAG_KILL_AFTER_RESTORE        = "flagKillAfterRestore";
    public static String FLAG_RESTORE_ANY_VERSION       = "flagRestoreAnyVersion";
    public static String FLAG_EXTERNAL_STORAGE          = "flagExternalStorage";
    public static String FLAG_SUPPORTS_XLARGE_SCREENS   = "flagSupportsXLargeScreens";
    public static String FLAG_LARGE_HEAP                = "flagLargeHeap";
    public static String FLAG_STOPPED                   = "flagStopped";
    public static String FLAG_SUPPORTS_RTL              = "flagSupportsRTL";
    public static String FLAG_INSTALLED                 = "flagInstalled";
    public static String FLAG_IS_DATA_ONLY              = "flagIsDataOnly";
    public static String FLAG_IS_GAME                   = "flagIsGame";
    public static String FLAG_FULL_BACKUP_ONLY          = "flagFullBackupOnly";
    public static String FLAG_USES_CLEARTEXT_TRAFFIC    = "flagUsesCleartextTraffic";
    public static String FLAG_EXTRACT_NATIVE_LIBS       = "flagExtractNativeLibs";
    public static String FLAG_HARDWARE_ACCELERATED      = "flagHardwareExcelerated";
    public static String FLAG_SUSPENDED                 = "flagSuspended";
    public static String FLAG_MULTIARCH                 = "flagMultiarch";
    /**
     * Publish a survey or update the survey for all apps of a single device.
     * Each app survey publish event is timestamped. Timestamps are checked and updated when the
     * survey results are published. Survey results for an app cannot be published more than one
     * time in 7 days. This way a snapshot of apps can be taken at any time without being heavily
     * influenced by users that frequently run the app.
     * @param ctx
     */
    public static void publishAppSurvey(Context ctx) {

        int publishTotal = 0;
        int targetAppsTotal = 0;

        PackageManager pm = ctx.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName, GET_PERMISSIONS);

                String[] requestedPermissions = packageInfo.requestedPermissions;

                if(requestedPermissions != null) {

                    JSONObject appObj = getDefaultAppObj();
                    JSONObject otherObj = null;
                    JSONObject targetAppObj = null;

                    for (String requestedPermission : requestedPermissions) {

                        if (requestedPermission.matches(Manifest.permission.WRITE_CONTACTS))
                            otherObj = appObj.put(WRITE_CONTACTS, 1);
                        else
                        if (requestedPermission.matches(Manifest.permission.READ_CONTACTS))
                            otherObj = appObj.put(READ_CONTACTS, 1);
                        else
                        if (requestedPermission.matches(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                            targetAppObj = appObj.put(WRITE_EXTERNAL_STORAGE, 1);
                        else
                        if (requestedPermission.matches(Manifest.permission.READ_EXTERNAL_STORAGE))
                            targetAppObj = appObj.put(READ_EXTERNAL_STORAGE, 1);
                        else
                        if (requestedPermission.matches(Manifest.permission.RECEIVE_BOOT_COMPLETED))
                            otherObj = appObj.put(RECEIVE_BOOT_COMPLETED, 1);
                        else
                        if (requestedPermission.matches(Manifest.permission.INTERNET))
                            otherObj = appObj.put(INTERNET, 1);
                        else
                        if (requestedPermission.matches(Manifest.permission.GET_ACCOUNTS))
                            otherObj = appObj.put(GET_ACCOUNTS, 1);
                        else
                        if (requestedPermission.matches(Manifest.permission.CAMERA))
                            otherObj = appObj.put(CAMERA, 1);
                        else
                        if (requestedPermission.matches(Manifest.permission.RECORD_AUDIO))
                            otherObj = appObj.put(RECORD_AUDIO, 1);
                        else
                        if (requestedPermission.matches(Manifest.permission.ACCESS_COARSE_LOCATION))
                            otherObj = appObj.put(ACCESS_COARSE_LOCATION, 1);
                        else
                        if (requestedPermission.matches(Manifest.permission.ACCESS_FINE_LOCATION))
                            otherObj = appObj.put(ACCESS_FINE_LOCATION, 1);
                        else
                        if (requestedPermission.matches(Manifest.permission.READ_CALENDAR))
                            otherObj = appObj.put(READ_CALENDAR ,1);
                        else
                        if (requestedPermission.matches(Manifest.permission.WRITE_CALENDAR))
                            otherObj = appObj.put(WRITE_CALENDAR ,1);
                        else
                        if (requestedPermission.matches(Manifest.permission.READ_PHONE_STATE))
                            otherObj = appObj.put(READ_PHONE_STATE ,1);
                        else
                        if (requestedPermission.matches(Manifest.permission.CALL_PHONE))
                            otherObj = appObj.put(CALL_PHONE ,1);
                        else
                        if (requestedPermission.matches(Manifest.permission.READ_CALL_LOG))
                            otherObj = appObj.put(READ_CALL_LOG ,1);
                        else
                        if (requestedPermission.matches(Manifest.permission.WRITE_CALL_LOG))
                            otherObj = appObj.put(WRITE_CALL_LOG ,1);
                        else
                        if (requestedPermission.matches(Manifest.permission.ADD_VOICEMAIL))
                            otherObj = appObj.put(ADD_VOICEMAIL ,1);
                        else
                        if (requestedPermission.matches(Manifest.permission.USE_SIP))
                            otherObj = appObj.put(USE_SIP ,1);
                        else
                        if (requestedPermission.matches(Manifest.permission.PROCESS_OUTGOING_CALLS))
                            otherObj = appObj.put(PROCESS_OUTGOING_CALLS,1);
                        else
                        if (requestedPermission.matches(Manifest.permission.SEND_SMS))
                            otherObj = appObj.put(SEND_SMS ,1);
                        else
                        if (requestedPermission.matches(Manifest.permission.RECEIVE_SMS))
                            otherObj = appObj.put(RECEIVE_SMS ,1);
                        else
                        if (requestedPermission.matches(Manifest.permission.READ_SMS))
                            otherObj = appObj.put(READ_SMS ,1);
                        else
                        if (requestedPermission.matches(Manifest.permission.RECEIVE_WAP_PUSH))
                            otherObj = appObj.put(RECEIVE_WAP_PUSH ,1);
                        else
                        if (requestedPermission.matches(Manifest.permission.RECEIVE_MMS))
                            otherObj = appObj.put(RECEIVE_MMS ,1);
                    }
                    if( otherObj != null || targetAppObj != null){

                        appObj.put(APP_NAME, String.valueOf(applicationInfo.loadLabel(pm)));
                        appObj.put(PACKAGE_NAME, applicationInfo.packageName);
                        appObj.put(FLAGS, applicationInfo.flags);
                        appObj.put(TARGET_SDK, applicationInfo.targetSdkVersion);
                        appObj.put(MAKE_MODEL, DeviceSurvey.getMakeModelName());

                        appObj.put(VERSION_CODE, packageInfo.versionCode);
                        appObj.put(VERSION_NAME, String.valueOf(packageInfo.versionName));
                        appObj.put(LAST_UPDATE_TIME, packageInfo.lastUpdateTime);
                        appObj.put(FIRST_INSTALLATION_TIME, packageInfo.firstInstallTime);

//                        LogUtil.log(""+appObj.toString(4));

//                        if( applicationInfo.processName.contains("nuvolect"))
//                        LogUtil.log(
//                                applicationInfo.loadLabel(pm)+
//                                ", lastUpdateTime: "+ TimeUtil.friendlyTimeString(packageInfo.lastUpdateTime)+
//                        ", firstInstallTime: "+ TimeUtil.friendlyTimeString(packageInfo.firstInstallTime));
//
                        publishTotal += Publish.getInstance(ctx)
                                .publishCond7Days( appObj.toString(), Analytics.SURVEY_JSON_04,
                                        applicationInfo.packageName);

                        if( targetAppObj != null)
                            ++targetAppsTotal;
                    }
                }

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        LogUtil.log(LogUtil.LogType.APP_SURVEY, "Total published: " + publishTotal);

        // Publish the number of apps along with app version and SDK.
        Publish.getInstance(ctx).publishCondTotal7Days(targetAppsTotal);

        // Persist survey timers
        Publish.getInstance(ctx).surveySave();
    }

    public static JSONObject getAppDetail(Context ctx, String package_name) {

        JSONObject appObj = getDefaultAppObj();

        PackageManager pm = ctx.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo( package_name, PackageManager.GET_PERMISSIONS);
            ApplicationInfo applicationInfo = pm.getApplicationInfo( package_name, PackageManager.GET_META_DATA);

            appObj.put(APP_NAME, String.valueOf(applicationInfo.loadLabel(pm)));
            appObj.put(PACKAGE_NAME, applicationInfo.packageName);
            appObj.put(FLAGS, applicationInfo.flags);
            appObj.put(TARGET_SDK, applicationInfo.targetSdkVersion);
            appObj.put(MAKE_MODEL, DeviceSurvey.getMakeModelName());

            appObj.put(VERSION_CODE, packageInfo.versionCode);
            appObj.put(VERSION_NAME, String.valueOf(packageInfo.versionName));
            appObj.put(LAST_UPDATE_TIME, packageInfo.lastUpdateTime);
            appObj.put(FIRST_INSTALLATION_TIME, packageInfo.firstInstallTime);

            String[] requestedPermissions = packageInfo.requestedPermissions;

            for (String requestedPermission : requestedPermissions) {
                
                if (requestedPermission.matches(Manifest.permission.WRITE_CONTACTS))
                    appObj.put(WRITE_CONTACTS, 1);
                else
                if (requestedPermission.matches(Manifest.permission.READ_CONTACTS))
                    appObj.put(READ_CONTACTS, 1);
                else
                if (requestedPermission.matches(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    appObj.put(WRITE_EXTERNAL_STORAGE, 1);
                else
                if (requestedPermission.matches(Manifest.permission.READ_EXTERNAL_STORAGE))
                    appObj.put(READ_EXTERNAL_STORAGE, 1);
                else
                if (requestedPermission.matches(Manifest.permission.RECEIVE_BOOT_COMPLETED))
                    appObj.put(RECEIVE_BOOT_COMPLETED, 1);
                else
                if (requestedPermission.matches(Manifest.permission.INTERNET))
                    appObj.put(INTERNET, 1);
                else
                if (requestedPermission.matches(Manifest.permission.GET_ACCOUNTS))
                    appObj.put(GET_ACCOUNTS, 1);
                else
                if (requestedPermission.matches(Manifest.permission.CAMERA))
                    appObj.put(CAMERA, 1);
                else
                if (requestedPermission.matches(Manifest.permission.RECORD_AUDIO))
                    appObj.put(RECORD_AUDIO, 1);
                else
                if (requestedPermission.matches(Manifest.permission.ACCESS_COARSE_LOCATION))
                    appObj.put(ACCESS_COARSE_LOCATION, 1);
                else
                if (requestedPermission.matches(Manifest.permission.ACCESS_FINE_LOCATION))
                    appObj.put(ACCESS_FINE_LOCATION, 1);
                else
                if (requestedPermission.matches(Manifest.permission.READ_CALENDAR))
                    appObj.put(READ_CALENDAR ,1);
                else
                if (requestedPermission.matches(Manifest.permission.WRITE_CALENDAR))
                    appObj.put(WRITE_CALENDAR ,1);
                else
                if (requestedPermission.matches(Manifest.permission.READ_PHONE_STATE))
                    appObj.put(READ_PHONE_STATE ,1);
                else
                if (requestedPermission.matches(Manifest.permission.CALL_PHONE))
                    appObj.put(CALL_PHONE ,1);
                else
                if (requestedPermission.matches(Manifest.permission.READ_CALL_LOG))
                    appObj.put(READ_CALL_LOG ,1);
                else
                if (requestedPermission.matches(Manifest.permission.WRITE_CALL_LOG))
                    appObj.put(WRITE_CALL_LOG ,1);
                else
                if (requestedPermission.matches(Manifest.permission.ADD_VOICEMAIL))
                    appObj.put(ADD_VOICEMAIL ,1);
                else
                if (requestedPermission.matches(Manifest.permission.USE_SIP))
                    appObj.put(USE_SIP ,1);
                else
                if (requestedPermission.matches(Manifest.permission.PROCESS_OUTGOING_CALLS))
                    appObj.put(PROCESS_OUTGOING_CALLS,1);
                else
                if (requestedPermission.matches(Manifest.permission.SEND_SMS))
                    appObj.put(SEND_SMS ,1);
                else
                if (requestedPermission.matches(Manifest.permission.RECEIVE_SMS))
                    appObj.put(RECEIVE_SMS ,1);
                else
                if (requestedPermission.matches(Manifest.permission.READ_SMS))
                    appObj.put(READ_SMS ,1);
                else
                if (requestedPermission.matches(Manifest.permission.RECEIVE_WAP_PUSH))
                    appObj.put(RECEIVE_WAP_PUSH ,1);
                else
                if (requestedPermission.matches(Manifest.permission.RECEIVE_MMS))
                    appObj.put(RECEIVE_MMS ,1);
            }

            appObj.put(FLAG_SYSTEM                    , oneZero( ApplicationInfo.FLAG_SYSTEM & applicationInfo.flags));
            appObj.put(FLAG_DEBUGGABLE                , oneZero( ApplicationInfo.FLAG_DEBUGGABLE & applicationInfo.flags));
            appObj.put(FLAG_HAS_CODE                  , oneZero( ApplicationInfo.FLAG_HAS_CODE & applicationInfo.flags));
            appObj.put(FLAG_PERSISTENT                , oneZero( ApplicationInfo.FLAG_PERSISTENT & applicationInfo.flags));
            appObj.put(FLAG_FACTORY_TEST              , oneZero( ApplicationInfo.FLAG_FACTORY_TEST & applicationInfo.flags));
            appObj.put(FLAG_ALLOW_TASK_REPARENTING    , oneZero( ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA & applicationInfo.flags));
            appObj.put(FLAG_ALLOW_CLEAR_USER_DATA     , oneZero( ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA & applicationInfo.flags));
            appObj.put(FLAG_UPDATED_SYSTEM_APP        , oneZero( ApplicationInfo.FLAG_UPDATED_SYSTEM_APP & applicationInfo.flags));
            appObj.put(FLAG_TEST_ONLY                 , oneZero( ApplicationInfo.FLAG_TEST_ONLY & applicationInfo.flags));
            appObj.put(FLAG_SUPPORTS_SMALL_SCREENS    , oneZero( ApplicationInfo.FLAG_SUPPORTS_SMALL_SCREENS & applicationInfo.flags));
            appObj.put(FLAG_SUPPORTS_NORMAL_SCREENS   , oneZero( ApplicationInfo.FLAG_SUPPORTS_NORMAL_SCREENS & applicationInfo.flags));
            appObj.put(FLAG_SUPPORTS_LARGE_SCREENS    , oneZero( ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS & applicationInfo.flags));
            appObj.put(FLAG_RESIZABLE_FOR_SCREENS     , oneZero( ApplicationInfo.FLAG_RESIZEABLE_FOR_SCREENS & applicationInfo.flags));
            appObj.put(FLAG_SUPPORTS_SCREEN_DENSITIES , oneZero( ApplicationInfo.FLAG_SUPPORTS_SCREEN_DENSITIES & applicationInfo.flags));
            appObj.put(FLAG_VM_SAFE_MODE              , oneZero( ApplicationInfo.FLAG_VM_SAFE_MODE & applicationInfo.flags));
            appObj.put(FLAG_ALLOW_BACKUP              , oneZero( ApplicationInfo.FLAG_ALLOW_BACKUP & applicationInfo.flags));
            appObj.put(FLAG_KILL_AFTER_RESTORE        , oneZero( ApplicationInfo.FLAG_KILL_AFTER_RESTORE & applicationInfo.flags));
            appObj.put(FLAG_RESTORE_ANY_VERSION       , oneZero( ApplicationInfo.FLAG_RESTORE_ANY_VERSION & applicationInfo.flags));
            appObj.put(FLAG_EXTERNAL_STORAGE          , oneZero( ApplicationInfo.FLAG_EXTERNAL_STORAGE & applicationInfo.flags));
            appObj.put(FLAG_SUPPORTS_XLARGE_SCREENS   , oneZero( ApplicationInfo.FLAG_SUPPORTS_XLARGE_SCREENS & applicationInfo.flags));
            appObj.put(FLAG_LARGE_HEAP                , oneZero( ApplicationInfo.FLAG_LARGE_HEAP & applicationInfo.flags));
            appObj.put(FLAG_STOPPED                   , oneZero( ApplicationInfo.FLAG_STOPPED & applicationInfo.flags));
            appObj.put(FLAG_SUPPORTS_RTL              , oneZero( ApplicationInfo.FLAG_SUPPORTS_RTL & applicationInfo.flags));
            appObj.put(FLAG_INSTALLED                 , oneZero( ApplicationInfo.FLAG_INSTALLED & applicationInfo.flags));
            appObj.put(FLAG_IS_DATA_ONLY              , oneZero( ApplicationInfo.FLAG_IS_DATA_ONLY & applicationInfo.flags));
            appObj.put(FLAG_IS_GAME                   , oneZero( ApplicationInfo.FLAG_IS_GAME & applicationInfo.flags));
            appObj.put(FLAG_FULL_BACKUP_ONLY          , oneZero( ApplicationInfo.FLAG_FULL_BACKUP_ONLY & applicationInfo.flags));
            appObj.put(FLAG_USES_CLEARTEXT_TRAFFIC    , oneZero( ApplicationInfo.FLAG_USES_CLEARTEXT_TRAFFIC & applicationInfo.flags));
            appObj.put(FLAG_EXTRACT_NATIVE_LIBS       , oneZero( ApplicationInfo.FLAG_EXTRACT_NATIVE_LIBS & applicationInfo.flags));
            appObj.put(FLAG_HARDWARE_ACCELERATED      , oneZero( ApplicationInfo.FLAG_HARDWARE_ACCELERATED & applicationInfo.flags));
            appObj.put(FLAG_SUSPENDED                 , oneZero( ApplicationInfo.FLAG_SUSPENDED & applicationInfo.flags));
            appObj.put(FLAG_MULTIARCH                 , oneZero( ApplicationInfo.FLAG_MULTIARCH & applicationInfo.flags));

            File file = new File( applicationInfo.publicSourceDir);
            appObj.put("apkSize", file.length());
            appObj.put("apkName", file.getName());
            appObj.put("apkPath", file.getAbsolutePath());

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return appObj;
    }

    private static int oneZero(int flag){

        if( flag == 0)
            return 0;
        else
            return 1;
    }


    private static JSONObject getDefaultAppObj() {

        JSONObject defaultAppObj = new JSONObject();

        try {

            defaultAppObj.put(READ_CONTACTS, 0);
            defaultAppObj.put(WRITE_CONTACTS,0);
            defaultAppObj.put(READ_EXTERNAL_STORAGE, 0);
            defaultAppObj.put(WRITE_EXTERNAL_STORAGE, 0);
            defaultAppObj.put(NETWORK_ACCESS,0);
            defaultAppObj.put(RECEIVE_BOOT_COMPLETED, 0);
            defaultAppObj.put(INTERNET, 0);
            defaultAppObj.put(GET_ACCOUNTS, 0);
            defaultAppObj.put(CAMERA, 0);
            defaultAppObj.put(RECORD_AUDIO, 0);
            defaultAppObj.put(ACCESS_COARSE_LOCATION, 0);
            defaultAppObj.put(ACCESS_FINE_LOCATION, 0);

            defaultAppObj.put(READ_CALENDAR ,0);
            defaultAppObj.put(WRITE_CALENDAR ,0);
            defaultAppObj.put(READ_PHONE_STATE ,0);
            defaultAppObj.put(CALL_PHONE ,0);
            defaultAppObj.put(READ_CALL_LOG ,0);
            defaultAppObj.put(WRITE_CALL_LOG ,0);
            defaultAppObj.put(ADD_VOICEMAIL ,0);
            defaultAppObj.put(USE_SIP ,0);
            defaultAppObj.put(PROCESS_OUTGOING_CALLS,0);
            defaultAppObj.put(SEND_SMS ,0);
            defaultAppObj.put(RECEIVE_SMS ,0);
            defaultAppObj.put(READ_SMS ,0);
            defaultAppObj.put(RECEIVE_WAP_PUSH ,0);
            defaultAppObj.put(RECEIVE_MMS ,0);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defaultAppObj;
    }

    public static int getContactsAppsTotal(Context ctx) {

        int appsAccessingContacts = 0;

        PackageManager pm = ctx.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName, GET_PERMISSIONS);

                String[] requestedPermissions = packageInfo.requestedPermissions;

                if(requestedPermissions != null) {

                    for (String requestedPermission : requestedPermissions) {

                        if (requestedPermission.contains("CONTACTS")) {

                            ++appsAccessingContacts;
                            break;
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return appsAccessingContacts;
    }


    /**
     * Read the app database and return it as a JSON object.
     * @param ctx
     * @return
     */
    public static JSONObject getAppDb(Context ctx){

        /**
         * Load packages from raw resources into a JSON object
         */
        String fileContents = "";
        StringBuilder sb = new StringBuilder();
        JSONObject jsonObject = new JSONObject();

        try {
            InputStream is = ctx.getResources().openRawResource(R.raw.app_survey_data);

            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) > 0) {

                String s = new String( buffer, 0, len, "UTF-8");
                sb.append( s );
            }
            fileContents = sb.toString();

            if( is != null)
                is.close();
        } catch (FileNotFoundException e) {
            LogUtil.logException(LogUtil.LogType.APP_SURVEY, e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * Build the JSON array
         */
        try {

            jsonObject= new JSONObject( fileContents );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
