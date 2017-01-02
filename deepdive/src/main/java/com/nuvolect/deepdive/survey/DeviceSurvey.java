package com.nuvolect.deepdive.survey;//

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import com.nuvolect.deepdive.ddUtil.PermissionUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLContext;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Query details about a device.
 */
public class DeviceSurvey {

    /**
     * Return a unique string for the device.  This string only changes when you wipe the device
     * and reinstall Android.
     * @param context
     * @return unique device ID string
     */
    public static String getUniqueInstallId(Context context) {

        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return deviceId;
    }

    /**
     * Retrieves phone make and model
     * @return
     */
    public static String getMakeModelName() {
        String manufacturer = android.os.Build.MANUFACTURER;
        String model = android.os.Build.MODEL;

        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    //Used for the phone model
    private static String capitalize(String s) {

        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    /**
     * Return a JSON object with device information
     * manufacturer
     * model
     * uniqueInstalledId
     * ssl
     * accounts
     * wifiList
     * logCat
     *
     * @param ctx
     * @return
     */
    public static JSONObject getInfo(Context ctx) {

        JSONObject object = new JSONObject();

        try {
            int API = android.os.Build.VERSION.SDK_INT;
            object.put("manufacturer", android.os.Build.MANUFACTURER);
            object.put("model", android.os.Build.MODEL);
            object.put("security_patch", API>=23?android.os.Build.VERSION.SECURITY_PATCH:"N/A<23");
            object.put("release", android.os.Build.VERSION.RELEASE);
            object.put("uniqueInstallId", getUniqueInstallId(ctx));
            object.put("ssl", getSslDetails());
            object.put("accounts", getAccounts(ctx));
            object.put("wifiList", getWifiConfigured(ctx));
            object.put("externalStorageAvailable", externalMemoryAvailable());
            object.put("internalMemorySize", getInternalMemoryDescription(ctx));
            object.put("externalStorageSize", getExternalStorageDescription());
            object.put("deviceInfo", com.nuvolect.deepdive.ddUtil.DeviceInfo.getDeviceInfo(ctx));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return object;
    }

    /**
     *
     * TODO return time/date/count history of WiFi use
     * @param ctx
     * @return
     */
    private static JSONArray getWifiConfigured(Context ctx) {

        JSONArray wifiList = new JSONArray();

        try {
            WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            List<WifiConfiguration> wifiConfiguredList = wifiManager.getConfiguredNetworks();

            for (WifiConfiguration wifi : wifiConfiguredList) {

                wifiList.put(wifi.SSID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            wifiList.put("WiFi disabled");
        }
        return wifiList;
    }

    private static String getSslDetails() throws NoSuchAlgorithmException {

        SSLContext sslContext = SSLContext.getInstance("TLS");
        String details = sslContext.getProtocol();
        details += ", " + sslContext.getProvider().toString();

        return details;
    }

    private static JSONArray getAccounts(Context ctx) {

        JSONArray accounts = new JSONArray();

        if (PermissionUtil.canGetAccounts(ctx)) {

            if (ActivityCompat.checkSelfPermission( ctx, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return new JSONArray();
            }
            Account[] myAccounts = AccountManager.get(ctx).getAccounts();

            for (Account myAccount : myAccounts) {

                String account = myAccount.name.toLowerCase(Locale.US).trim();
                accounts.put( account);
            }
        }
        return accounts;
    }

    public static JSONArray getLogCat(){

        JSONArray log = new JSONArray();
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                log.put(line);
            }
        }
        catch (IOException e) {}

        return log;
    }

    public static JSONArray getShell(String shell_cmd) {

        JSONArray log = new JSONArray();
        try {
            Process process = Runtime.getRuntime().exec( shell_cmd );
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                log.put(line);
            }
        }
        catch (IOException e) {
            log.put("exception");
        }

        return log;
    }

    public static boolean externalMemoryAvailable() {

        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    public static String getInternalMemorySize() {

        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return formatSize(availableBlocks * blockSize);
    }

    public static String getTotalExternalStorageSize() {

        String ERROR = "Information request error";

        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return formatSize(totalBlocks * blockSize);
        } else {
            return ERROR;
        }
    }

    public static String getInternalMemoryDescription(Context ctx){

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);

        //Percentage can be calculated for API 16+
        double percent = (double)mi.availMem / mi.totalMem * 100;

        String freeStr = formatSize( mi.availMem);
        String totalStr = formatSize( mi.totalMem);

        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(1);
        numberFormat.setMaximumFractionDigits(1);
        String percentStr = numberFormat.format( percent)+"%";

        return freeStr + " of "+ totalStr + " "+ percentStr;
    }

    public static String getExternalStorageDescription(){

        File externalStorageDir = Environment.getExternalStorageDirectory();
        long free = externalStorageDir.getFreeSpace() / 1024 / 1024;

        StatFs stat = new StatFs(externalStorageDir.getPath());

        double percent = (double)stat.getFreeBytes() / stat.getTotalBytes() * 100;

        String freeStr = formatSize( stat.getFreeBytes());
        String totalStr = formatSize( stat.getTotalBytes());

        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(1);
        numberFormat.setMaximumFractionDigits(1);
        String percentStr = numberFormat.format( percent)+"%";

        return freeStr + " of "+ totalStr + " "+ percentStr;

    }

    public static float megabytesAvailable(File f) {
        StatFs stat = new StatFs(f.getPath());
        long bytesAvailable = (long)stat.getBlockSizeLong() * (long)stat.getAvailableBlocksLong();
        return bytesAvailable / (1024.f * 1024.f);
    }

    public static String formatSize(long size) {

        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }
}
