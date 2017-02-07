package com.nuvolect.deepdive.util;

import android.content.Context;

/**
 * Persist encrypted key value data.
 */
public class Cryp {

    private static final String GROUP_JSON = "group_json";
    public static final String APP_VERSION = "app_version";

    /**
     * Return the value of a related key, or return an empty string
     * if the key is not found.
     * @param key
     * @return
     */
    public static String get(Context ctx, String key){

        if( key == null || key.isEmpty())
            return "";

        String cryp = Persist.get(ctx, key);
        if( cryp.isEmpty())
            return "";

        String clear = BetterCrypto.decrypt(ctx, cryp);

        return clear;
    }

    /**
     * Return the value of a related key, or return the default value
     * if the key is not found.
     * @param key
     * @param defValue
     * @return
     */
    public static String get(Context ctx, String key, String defValue) {

        String value = get(ctx, key);

        if( value == null || value.isEmpty()){

            put( ctx, key, defValue);
            return defValue;
        }
        else{

            return value;
        }
    }

    /**
     * Persist the value referenced by a key.  Return the number of
     * records updated: 0, first time update, 1, value updated, 2+ error.
     * @param key
     * @param value
     * @return
     */
    public static int put(Context ctx, String key, String value){

        if( value == null)
            LogUtil.log("put key is NULL - ERROR ----------------------------------------");

        String cryp = BetterCrypto.encrypt(ctx, value);
        return Persist.put(ctx, key, cryp)?1:0;
    }

    /**
     * Return the integer value referenced by a key.
     * Return 0 if the key does not exist.
     * @param key
     * @return
     */
    public static int getInt(Context ctx, String key) {

        String v = Persist.get(ctx, key);
        if( v.isEmpty())
            return 0;
        else{
            String clear = BetterCrypto.decrypt(ctx, v);
            return Integer.valueOf( clear );
        }
    }

    /**
     * Return the integer value referenced by a key.
     * If the key does not exist, save the default as the key and return it.
     * @param key
     * @param defInt
     * @return
     */
    public static int getInt(Context ctx, String key, int defInt) {

        String v = Persist.get( ctx, key);
        if( v.isEmpty()) {
            putInt(ctx, key, defInt);
            return defInt;
        }
        else{
            String clear = BetterCrypto.decrypt(ctx, v);
            return Integer.valueOf( clear );
        }
    }

    /**
     * Persist the value referenced by a key.
     * Return the number of records updated: 1 success, 0 fail
     * @param key
     * @param val
     */
    public static int putInt(Context ctx, String key, int val) {

        String cryp = BetterCrypto.encrypt(ctx, String.valueOf( val ));

        return Persist.put( ctx, key, cryp)?1:0;
    }
}
