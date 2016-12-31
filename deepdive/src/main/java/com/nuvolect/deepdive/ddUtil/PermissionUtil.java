package com.nuvolect.deepdive.ddUtil;//

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

//TODO create class description
//
public class PermissionUtil {

    public final static int ACCESS_FINE_LOCATION   = 1;
    public final static int ACCESS_COARSE_LOCATION = 2;
    public final static int READ_CONTACTS          = 3;
    public final static int READ_EXTERNAL_STORAGE  = 4;
    public final static int WRITE_EXTERNAL_STORAGE = 5;
    public final static int READ_PHONE_STATE       = 6;
    public final static int READ_PHONE_STATE_READ_CONTACTS = 7;

    public static boolean canAccessAnyLocation(Context ctx){

        return canAccessFineLocation(ctx) || canAccessCoarseLocation(ctx);
    }

    public static boolean canAccessFineLocation(Context ctx){

        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
    public static boolean canAccessCoarseLocation(Context ctx){

        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
    public static boolean canAccessReadContacts(Context ctx){

        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }
    public static boolean canGetAccounts(Context ctx){

        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.GET_ACCOUNTS)
                == PackageManager.PERMISSION_GRANTED;
    }
    public static void requestReadContacts(Activity act, int responseId){

        if (Build.VERSION.SDK_INT >= 23) {

            act.requestPermissions(
                    new String[]{Manifest.permission.READ_CONTACTS}, responseId);
        }else{

            ActivityCompat.requestPermissions(act,
                    new String[]{Manifest.permission.READ_CONTACTS}, responseId);
        }
    }
    public static void requestReadWriteContacts(Activity act, int responseId){

        if (Build.VERSION.SDK_INT >= 23) {

            act.requestPermissions(
                    new String[]{
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS},
                    responseId);
        }else{

            ActivityCompat.requestPermissions(act,
                    new String[]{
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS},
                    responseId);
        }
    }
    public static boolean canReadExternalStorage(Context ctx) {

        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }
    public static void requestReadExternalStorage(Activity act, int responseId){

        if (Build.VERSION.SDK_INT >= 23) {

            act.requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, responseId);
        }else{

            ActivityCompat.requestPermissions(act,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, responseId);
        }
    }
    public static boolean canWriteExternalStorage(Context ctx) {

        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }
    public static void requestWriteExternalStorage(Activity act, int responseId){

        if (Build.VERSION.SDK_INT >= 23) {
            act.requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, responseId);
        }else{
            ActivityCompat.requestPermissions(act,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, responseId);
        }
    }

    public static boolean canReadWriteExternalStorage(Activity act) {

        return canReadExternalStorage(act) && canWriteExternalStorage(act);
    }
    public static void requestReadWriteExternalStorage(Activity act, int responseId){

        if (Build.VERSION.SDK_INT >= 23) {
            act.requestPermissions(
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                    }, responseId);
        }else{
            ActivityCompat.requestPermissions(act,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    }, responseId);
        }
    }


    public static boolean canAccessPhoneState(Context ctx) {

        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED;
    }


    private static final String SCHEME = "package";

    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";

    private static final String APP_PKG_NAME_22 = "pkg";

    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";

    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

    public static void showInstalledAppDetails(Context context, String packageName) {
        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) { // above 2.3
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else { // below 2.3
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22 : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        context.startActivity(intent);
    }

    public static boolean canAccessCamera(Context ctx) {

        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean canAccessMicrophone(Context ctx) {

        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }
}
