/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.nuvolect.deepdive.main.CConst;

import java.util.Map;

public class Persist {

    /**
     * Persist data to Android app private storage.
     *
     * DESIGN PATTERN TO PERSIST ENCRYPTED STRING DATA:
     * 1. Convert string data to a byte[], no encoding required
     * 2. Encrypt the byte[], creating a new byte[]
     * 3. Create a string for storage by encoding the byte[] with Base64
     * 4. Persist the string
     * 5. Clean up any clear text data
     *
     * RETRIEVE AND RESTORE PERSISTED AND ENCRYPTED STRING DATA:
     * 1. Read persisted data into a string
     * 2. Decode the string back into a byte[] using Base64 decode
     * 3. Decrypt the byte[], creating a new byte[]
     * 4. Decode the byte[] into a string with UTF-8.
     * 5. Clean up any clear text data
     */
    private static final String PERSIST_NAME           = "dd_persist";

    // Persist keys, some calling methods pass their own keys
    private static final String PEST_TIME              = "pest_time";
    private static final String PORT_NUMBER            = "port_number";
    private static final String SHOW_TIP_CURRENT       = "show_tip_current";

    /**
     * Remove all persistent data.
     */
    public static void clearAll(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().clear().commit();
    }

    /**
     * Check if a specific key is persisted.
     * @param ctx
     * @param persistKey
     * @return
     */
    public static boolean keyExists(Context ctx, String persistKey) {

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.contains( persistKey);
    }

    public static boolean dumpKeysToLog(Context ctx) {

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        Map<String, ?> map = pref.getAll();
        LogUtil.log(LogUtil.LogType.PERSIST, "key count: "+map.keySet().size());

        for( String key : map.keySet()){

            LogUtil.log(LogUtil.LogType.PERSIST, "key: "+key);
            LogUtil.log(LogUtil.LogType.PERSIST, "value: "+get(ctx, key));
        }

        return map.keySet().size() > 0;
    }

    /**
     * Delete a specific key.
     * @param ctx
     * @param keyToDelete
     * @return
     */
    public static boolean deleteKey(Context ctx, String keyToDelete){

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.edit().remove( keyToDelete).commit();
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

    public static void setCurrentTip(Context ctx, int tipIndex){
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt( SHOW_TIP_CURRENT, tipIndex).commit();
    }
    public static int getCurrentTip(Context ctx){
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getInt(SHOW_TIP_CURRENT, -1);
    }

    public static void putPort(Context ctx, int port) {

        byte[] crypBytes = new byte[0];
        try {
            crypBytes = CrypUtil.encryptInt( port);
            String crypString = Base64.encodeToString(crypBytes, Base64.DEFAULT);

            put( ctx, PORT_NUMBER, crypString);
        } catch (Exception e) {
            LogUtil.logException(LogUtil.LogType.PERSIST, e);
        }
    }

    public static int getPort(Context ctx, int default_port) {

        if( ! keyExists( ctx, PORT_NUMBER))
            return default_port;

        try {
            String crypString = get( ctx, PORT_NUMBER);
            final byte[] crypBytes = CrypUtil.toBytesUTF8( crypString);

            return CrypUtil.decryptInt( crypBytes);
        } catch (Exception e) {
            LogUtil.logException(LogUtil.LogType.PERSIST, e);
        }
        return default_port;
    }

    public static void putDbPassword(Context ctx, byte[] clearBytes) {

        try {
            // Encrypt the byte array, creating a new byte array
            byte[] crypBytes = CrypUtil.encrypt( clearBytes);

            // Encode the array for storage
            String crypEncodedString = CrypUtil.encodeToB64( crypBytes);

            // Store it as a string
            put( ctx, CConst.DB_PASSWORD, crypEncodedString);

        } catch (Exception e) {
            LogUtil.logException(LogUtil.LogType.PERSIST, e);
        }
    }

    public static byte[] getDbPassword(Context ctx) {

        // Get the encoded and encrypted string
        String crypEncodedString = get( ctx, CConst.DB_PASSWORD);

        // Decode the string back into a byte array using Base64 decode
        byte[] crypBytes = CrypUtil.decodeFromB64( crypEncodedString);

        // Decrypt the byte array, creating a new byte array
        byte[] clearBytes = new byte[0];
        try {
            clearBytes = CrypUtil.decrypt( crypBytes);
        } catch (Exception e) {
            LogUtil.logException(LogUtil.LogType.PERSIST, e);
        }

        return clearBytes;
    }

    public static void putSecTok(Context ctx, String clearString) {

        try {
            // Convert it to bytes, no encoding yet
            byte[] clearBytes = CrypUtil.getBytes(clearString);

            // Encrypt the byte array, creating a new byte array
            byte[] encryptedBytes = CrypUtil.encrypt(clearBytes);

            // Prepare for storage by converting the byte array to a Base64 encoded string
            String encryptedEncodedString = CrypUtil.encodeToB64( encryptedBytes );

            // Store it as a string
            put( ctx, CConst.SEC_TOK, encryptedEncodedString );

            // Clean up
            clearBytes = CrypUtil.cleanArray( clearBytes);
        } catch (Exception e) {
            LogUtil.logException(LogUtil.LogType.PERSIST, e);
        }
    }

    public static String getSecTok(Context ctx) {

        // Get the encoded and encrypted string
        String crypString = get( ctx, CConst.SEC_TOK);

        // Decode the string back into a byte array using Base64 decode
        byte[] crypBytes = CrypUtil.decodeFromB64( crypString);

        // Decrypt the byte array, creating a new byte array
        byte[] clearBytes = new byte[0];
        try {
            clearBytes = CrypUtil.decrypt( crypBytes);
        } catch (Exception e) {
            LogUtil.logException(LogUtil.LogType.PERSIST, e);
        }

        // Decode the byte array creating a new String using UTF-8 encoding
        String clearString = CrypUtil.toStringUTF8( clearBytes);

        // Clean up
        clearBytes = CrypUtil.cleanArray( clearBytes);

        return clearString;
    }

    public static void putSelfsignedKsKey(Context ctx, char[] clearChars) {

        // Convert it to bytes, no encoding yet
        byte[] clearBytes = CrypUtil.toBytesUTF8( clearChars);
        String encryptedEncodedString = null;

        try {
            // Encrypt the byte array, creating a new byte array
            byte[] encryptedBytes = CrypUtil.encrypt(clearBytes);

            // Prepare for storage by converting the byte array to a Base64 encoded string
            encryptedEncodedString = CrypUtil.encodeToB64( encryptedBytes );
        } catch (Exception e) {
            LogUtil.logException(LogUtil.LogType.PERSIST, e);
        }

        // Store it as a string
        put( ctx, CConst.SELFSIGNED_KS_KEY, encryptedEncodedString);

        // Clean up
        clearBytes = CrypUtil.cleanArray( clearBytes);
    }

    public static char[] getSelfsignedKsKey(Context ctx) {

        // Get the encoded and encrypted string
        String cryptedEncodedString = get( ctx, CConst.SELFSIGNED_KS_KEY);

        // Decode the string back into a byte array using Base64 decode
        byte[] crypBytes = CrypUtil.decodeFromB64(cryptedEncodedString);

        // Decrypt the byte array, creating a new byte array
        byte[] clearBytes = new byte[0];
        try {
            clearBytes = CrypUtil.decrypt( crypBytes);
        } catch (Exception e) {
            LogUtil.logException(LogUtil.LogType.PERSIST, e);
        }

        // Decode the byte array creating a new String using UTF-8 encoding
        char[] clearChars = CrypUtil.toChar( clearBytes);

        // Clean up
        clearBytes = CrypUtil.cleanArray( clearBytes);

        return clearChars;
    }

    public static void putUsers(Context ctx, String clearString) {

        try {
            // Convert it to bytes, no encoding yet
            byte[] clearBytes = CrypUtil.getBytes(clearString);

            // Encrypt the byte array, creating a new byte array
            byte[] encryptedBytes = CrypUtil.encrypt(clearBytes);

            // Prepare for storage by converting the byte array to a Base64 encoded string
            String encryptedEncodedString = CrypUtil.encodeToB64( encryptedBytes );

            // Store it as a string
            put( ctx, CConst.USERS, encryptedEncodedString );

            // Clean up
            clearBytes = CrypUtil.cleanArray( clearBytes);
        } catch (Exception e) {
            LogUtil.logException(LogUtil.LogType.PERSIST, e);
        }
    }

    public static String getUsers(Context ctx, String emptyArray) {

        if( ! keyExists( ctx, CConst.USERS)){

            putUsers( ctx, emptyArray);
            return emptyArray;
        }
        // Get the encoded and encrypted string
        String crypString = get( ctx, CConst.USERS);

        // Decode the string back into a byte array using Base64 decode
        byte[] crypBytes = CrypUtil.decodeFromB64( crypString);

        // Decrypt the byte array, creating a new byte array
        byte[] clearBytes = new byte[0];
        try {
            clearBytes = CrypUtil.decrypt( crypBytes);
        } catch (Exception e) {
            LogUtil.logException(LogUtil.LogType.PERSIST, e);
        }

        // Decode the byte array creating a new String using UTF-8 encoding
        String clearString = CrypUtil.toStringUTF8( clearBytes);

        // Clean up
        clearBytes = CrypUtil.cleanArray( clearBytes);

        return clearString;
    }
}
