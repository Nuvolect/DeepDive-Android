package com.nuvolect.deepdive.util;//

import android.content.Context;

/**
 * Utilities to persist encrypted data.
 */
public class CrypUtil {

    public static final String APP_VERSION = "app_version";

    /**
     * Return the value of a related key, or return an empty string
     * if the key is not found.
     * @param key
     * @return
     */
    public static String get(Context ctx, String key){

        String crypValue = Persist.get(ctx, key);

        if( crypValue.isEmpty())
            return "";

        return BetterCrypto.decrypt(ctx, crypValue);
    }

    /**
     * Return the value of a related key, or return the default value
     * if the key is not found.
     * @param key
     * @param defValue
     * @return
     */
    public static String get(Context ctx, String key, String defValue) {

        String crypValue = Persist.get(ctx, key);

        if( crypValue.isEmpty())
            return defValue;

        return BetterCrypto.decrypt(ctx, crypValue);
    }

    /**
     * Persist the value referenced by a key.  Return the number of
     * records updated: 0, first time update, 1, value updated, 2+ error.
     * @param key
     * @param value
     * @return
     */
    public static int put(Context ctx, String key, String value){

        String crypValue = BetterCrypto.encrypt(ctx, value);

        int firstTime = 0;
        try {
            firstTime = Persist.get(ctx, key).isEmpty() ? 0 : 1;
            Persist.put(ctx, key, crypValue);
        } catch (Exception e) {
            LogUtil.logException(ctx, LogUtil.LogType.BETTER_CRYPTO, e);
            return 2;
        }
        return firstTime;
    }

    /**
     * Return the integer value referenced by a key.
     * Return 0 if the key does not exist.
     * @param key
     * @return
     */
    public static int getInt(Context ctx, String key) {

        String v = get(ctx, key);
        if( v.isEmpty())
            return 0;
        else
            return Integer.valueOf( v );
    }

    /**
     * Return the integer value referenced by a key.
     * If the key does not exist, save the default as the key and
     * return it.
     * @param key
     * @param defInt
     * @return
     */
    public static int getInt(Context ctx, String key, int defInt) {

        String v = get( ctx, key);
        if( v.isEmpty()) {
            putInt( ctx, key, defInt);
            return defInt;
        }
        else
            return Integer.valueOf( v );
    }
    /**
     * Persist the value referenced by a key.  Return the number of
     * records updated: 0, first time update, 1, value updated, 2+ error.
     * @param key
     * @param val
     */
    public static int putInt(Context ctx, String key, int val) {

        return put( ctx, key, String.valueOf(val));
    }
}
