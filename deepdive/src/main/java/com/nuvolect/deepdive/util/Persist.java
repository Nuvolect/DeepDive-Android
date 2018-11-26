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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.NoSuchPaddingException;

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

    /**
     * Encrypt clear char[] data with an app wide private key, then persist the encrypted results.
     *
     * @param ctx
     * @param persistKey
     * @param clearChar
     * @throws CertificateException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws NoSuchPaddingException
     * @throws UnrecoverableEntryException
     * @throws IOException
     */
    public static void putEncrypt(Context ctx, String persistKey, char[] clearChar)
            throws CertificateException, InvalidKeyException, NoSuchAlgorithmException,
            KeyStoreException, NoSuchPaddingException, UnrecoverableEntryException, IOException,
            NoSuchProviderException, InvalidAlgorithmParameterException {

        byte[] clearBytes = Passphrase.toBytes( clearChar);
        byte[] cryptBytes = KeystoreUtil.encrypt( ctx, CConst.APP_KEY_ALIAS, clearBytes);
        String cryptString = Base64.encodeToString( cryptBytes, Base64.NO_WRAP);

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        pref.edit().putString( persistKey, cryptString).commit();
    }


    /**
     * Read encrypted data, decrypt it with an app wide private key and return clear results.
     *
     * @param ctx
     * @return
     * @throws CertificateException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws NoSuchPaddingException
     * @throws UnrecoverableEntryException
     * @throws IOException
     */
    public static char[] getDecrypt(Context ctx, String persistKey)
            throws CertificateException, InvalidKeyException, NoSuchAlgorithmException,
            KeyStoreException, NoSuchPaddingException, UnrecoverableEntryException, IOException {

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);

        String encryptString = pref.getString( persistKey, CConst.NO_PASSPHRASE);
        byte[] encryptBytes = Base64.decode( encryptString, Base64.DEFAULT);
        byte[] clearBytes = KeystoreUtil.decrypt( CConst.APP_KEY_ALIAS, encryptBytes);
        char[] clearChars = Passphrase.toChars( clearBytes);

        return clearChars;
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
