package com.nuvolect.deepdive.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

public class Persist {

    private static final String PERSIST_NAME           = "pv_persist";

    // Persist keys
    private static final String ALLOW_CHANGE_FLASH 	   = "allow_change_flash";
    private static final String CAMERA_DEBUG 	       = "camera_debug";
    private static final String CAMERA_FACING 	       = "camera_facing";
    private static final String CAMERA_MODE 	       = "camera_mode";
    private static final String DEVELOPER_STATE        = "developer_state";
    private static final String FACING_EXACT_MATCH 	   = "facing_exact_match";
    private static final String FOLDER_LIST 	       = "folder_list";
    private static final String HEARTBEAT_TIME         = "heartbeat_time";
    private static final String MIRROR_PREVIEW 	       = "mirror_preview";
    private static final String PASSPHRASE 			   = "passphrase";
    private static final String PEST_TIME              = "pest_time";
    private static final String PV_STORAGE_KEY         = "pv_storage_key";
    private static final String PHOTO_VIDEO            = "photo_video";
    private static final String SHOW_TIP_CURRENT       = "show_tip_current";
    private static final String UPDATE_MEDIA_STORE 	   = "update_media_store";

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
     * Simple put value with the given key.
     * Return 1 if successful, otherwise 0.
     * @param ctx
     * @param key
     * @param value
     */
    public static int put(Context ctx, String key, String value){
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        return pref.edit().putString(key, value).commit()?1:0;
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

    public static boolean getDeveloper(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(DEVELOPER_STATE, false);
    }

    public static void setDeveloper(Context ctx, boolean isDeveloper) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(DEVELOPER_STATE, isDeveloper).commit();
    }

    public static long getLastHeartbeatTime(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getLong(HEARTBEAT_TIME, 0L);
    }

    public static void setHeartbeatTime(Context ctx, long heartbeatTime) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putLong(HEARTBEAT_TIME, heartbeatTime).commit();
    }

    public static void setEncryptedPassphrase(Context ctx, String passphrase){
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        pref.edit().putString(PASSPHRASE, passphrase).commit();
    }
    public static String getEncryptedPassphrase(Context ctx){
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getString(PASSPHRASE, CConst.DEFAULT_PASSPHRASE);
    }

    public static boolean getUpdateMediaStore(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(UPDATE_MEDIA_STORE, false);
    }

    public static void setUpdateMediaStore(Context ctx, boolean state) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(UPDATE_MEDIA_STORE, state).commit();
    }

    public static boolean getMirrorPreview(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(MIRROR_PREVIEW, false);
    }

    public static void setMirrorPreview(Context ctx, boolean state) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(MIRROR_PREVIEW, state).commit();
    }

    public static boolean getFacingExactMatch(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(FACING_EXACT_MATCH, false);
    }

    public static void setFacingExactMatch(Context ctx, boolean state) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(FACING_EXACT_MATCH, state).commit();
    }

    public static boolean getCameraDebug(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(CAMERA_DEBUG, false);
    }

    public static void setCameraDebug(Context ctx, boolean state) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(CAMERA_DEBUG, state).commit();
    }

    public static boolean getAllowChangeFlash(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(ALLOW_CHANGE_FLASH, false);
    }

    public static void setAllowChangeFlash(Context ctx, boolean state) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(ALLOW_CHANGE_FLASH, state).commit();
    }

//    public static Facing getCameraFacing(Context ctx) {
//        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
//        return Facing.valueOf(pref.getString(CAMERA_FACING, Facing.BACK.name()));
//    }
//
//    public static void setCameraFacing(Context ctx, Facing cameraFacing) {
//        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
//        pref.edit().putString(CAMERA_FACING, cameraFacing.name()).commit();
//    }

    public static boolean getUseSecureStorage(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PV_STORAGE_KEY, true);
    }

    public static void setUseSecureStorage(Context ctx, boolean storageState) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(PV_STORAGE_KEY, storageState).commit();
    }

    /**
     * True is photo, false is video
     * @param ctx
     * @return
     */
    public static boolean isPhoto(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PHOTO_VIDEO, true);
    }

    /**
     * True is photo, false is video
     * @param ctx
     * @param photoVideo
     */
    public static void setIsPhoto(Context ctx, boolean photoVideo) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(PHOTO_VIDEO, photoVideo).commit();
    }

    public static void setCurrentTip(Context ctx, int tipIndex){
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(SHOW_TIP_CURRENT, tipIndex).commit();
    }
    public static int getCurrentTip(Context ctx){
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getInt(SHOW_TIP_CURRENT, -1);
    }

    /**
     * Add a new folder and remove any duplicates.
     * @param ctx
     * @param folder_name
     * @return
     */
    public static JSONArray addFolder(Context ctx, String folder_name) {

        JSONArray array = getFolderList(ctx);
        HashSet<String> hashSet = new HashSet<>();

        try {
            for( int i = 0; i < array.length(); i++){

                hashSet.add( array.getString( i ));
            }
            hashSet.add( folder_name);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray slimArray = new JSONArray();

        for( String folder : hashSet){

            slimArray.put( folder);
        }

        putFolderList( ctx, slimArray);

        return slimArray;
    }

    private static void putFolderList(Context ctx, JSONArray array) {

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putString( FOLDER_LIST, array.toString()).commit();
    }

    public static JSONArray getFolderList(Context ctx) {

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        JSONArray array = null;
        try {
            array = new JSONArray( pref.getString( FOLDER_LIST, "[]"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return array;
    }

    public static boolean deleteFolder(Context ctx, String folder_name) {

        JSONArray array = getFolderList(ctx);

        int hit = -1;

        try {
            for( int i = 0; i < array.length(); i++){

                if( array.getString( i ).contentEquals( folder_name)){

                    hit = i;
                    break;
                }
            }
            if( hit >= 0){

                array.remove( hit );
                putFolderList(ctx, array);
                return true;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
