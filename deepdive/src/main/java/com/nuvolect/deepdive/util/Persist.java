package com.nuvolect.deepdive.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.nuvolect.deepdive.main.CConst;

import org.json.JSONException;
import org.json.JSONObject;

public class Persist {

    private static final String PERSIST_NAME           = "dd_persist";

    // Persist keys
    private static final String PASSPHRASE 			   = "passphrase";
    private static final String PEST_TIME              = "pest_time";
    private static final String SHOW_TIP_CURRENT       = "show_tip_current";

    /**
     * Remove all persistent data.
     */
    public static void clearAll(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().clear().commit();
    }

    /**
     * Simple get.  Return empty string if not found
     * @param ctx
     * @param key
     * @return
     */
    public static String get(Context ctx, String key) {

        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getString(key, "");
    }

    /**
     * Simple get.  Return default string if not found
     * @param ctx
     * @param key
     * @return
     */
    public static String get(Context ctx, String key, String defaultString) {

        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getString(key, defaultString);
    }

    /**
     * Simple put value with the given key.
     * Return true if successful, otherwise false.
     * @param ctx
     * @param key
     * @param value
     */
    public static boolean put(Context ctx, String key, String value){
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        return pref.edit().putString(key, value).commit();
    }

    /**
     * Return true when current time is within a sincePeriod
     * @param ctx
     * @param key key to find last time
     * @param sincePeriod  How long the period is in ms
     * @return
     */
    public static boolean getPestCheck(Context ctx, String key, long sincePeriod) {

        long currentTime = System.currentTimeMillis();

        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        try {
            JSONObject object = new JSONObject( pref.getString( PEST_TIME, "{}"));
            if( object.has( key )){

                long lastPestTime = object.getLong( key);
                /**
                 * May be pestering, check if we are within the pester period
                 */
                if( lastPestTime + sincePeriod > currentTime){

                    return true; // Within the pester period
                }else{
                    /**
                     * Not within the pester period, save the time and return false
                     */
                    object.put(key, currentTime);
                    pref.edit().putString(PEST_TIME, object.toString()).commit();
                    return false;
                }
            }else{
                /**
                 * Not pestering, this is the first call so save the time and return false
                 */
                object.put(key, currentTime);
                pref.edit().putString(PEST_TIME, object.toString()).commit();
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static void setEncryptedPassphrase(Context ctx, String passphrase){
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        pref.edit().putString(PASSPHRASE, passphrase).commit();
    }
    public static String getEncryptedPassphrase(Context ctx){
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getString(PASSPHRASE, CConst.NO_PASSPHRASE);
    }

    public static void setCurrentTip(Context ctx, int tipIndex){
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(SHOW_TIP_CURRENT, tipIndex).commit();
    }
    public static int getCurrentTip(Context ctx){
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getInt(SHOW_TIP_CURRENT, -1);
    }
}
