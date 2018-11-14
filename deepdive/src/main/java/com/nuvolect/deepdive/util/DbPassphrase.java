/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import android.content.Context;

import com.nuvolect.deepdive.license.LicenseUtil;
import com.nuvolect.deepdive.main.CConst;

import org.json.JSONException;
import org.json.JSONObject;

import static com.nuvolect.deepdive.util.Passphrase.HEX;
import static com.nuvolect.deepdive.util.Passphrase.generateRandomString;

/**
 * The passphrase is encrypted/decrypted with a public/private key
 * from the android keystore. AES symmetric encryption is used as a fallback
 * if the device Keystore is not capable.
 */
public class DbPassphrase {

    private static String KEY_ALIAS = "db_key_alias";
    private static String CIPHERTEXT = "ciphertext";
    private static String CLEARTEXT = "cleartext";

    /**
     * Decrypt the passphrase and return it as a string.
     *
     * @param ctx
     * @return
     */
    public static String getDbPassphrase(Context ctx) {

        String clearPassphrase = "";
        boolean success = false;
        String cryptPassphrase = Persist.getEncryptedPassphrase(ctx);

        if( cryptPassphrase.equals(CConst.NO_PASSPHRASE)){

            // First time, create a random passcode, encrypt and save it
            clearPassphrase = generateRandomString( 32, HEX);
            success = setDbPassphrase(ctx, clearPassphrase);

            assert success;

            return clearPassphrase;
        }
        try {
            /**
             * First try to decrypt with the Keystore private key.
             * If that fails attempt AES symmetric decryption.
             */
            try {
                JSONObject jsonObject = KeystoreUtil.decrypt(KEY_ALIAS, cryptPassphrase);
                if( jsonObject.getString("success").contentEquals("true")){

                    clearPassphrase = jsonObject.getString( CLEARTEXT );
                    success = true;
                }
            } catch (JSONException e) {
                success = false;
            }

            if( ! success ){

                /**
                 * Keystore failed. Fallback and use symmetric encryption.
                 * Use a static 32 hex char static key.
                 */
                String md5Key = LicenseUtil.md5( CConst.RANDOM_EDGE);
                clearPassphrase = SymmetricCrypto.decrypt( md5Key, cryptPassphrase);
            }

        } catch (Exception e) {
            LogUtil.logException(ctx, LogUtil.LogType.CRYPT, e);
        }
        return clearPassphrase;
    }

    /**
     * Encrypt the passphrase and save it to persisted storage.
     *
     * @param ctx
     * @param clearPassphrase
     * @return
     */
    public static boolean setDbPassphrase(Context ctx, String clearPassphrase){

        boolean success = true;
        String cryptPassphrase="";
        try {

            JSONObject jsonObject = KeystoreUtil.encrypt(KEY_ALIAS, clearPassphrase );
            if( jsonObject.getString("success").contentEquals("true")){

                cryptPassphrase = jsonObject.getString(CIPHERTEXT);
            }

            /**
             * If Keystore fails, use symmetric encryption with an embedded
             * passphrase.
             */
            if( cryptPassphrase.isEmpty()){

                String md5Key = LicenseUtil.md5( CConst.RANDOM_EDGE);
                cryptPassphrase = SymmetricCrypto.encrypt( md5Key, clearPassphrase);
            }

            Persist.setEncryptedPassphrase(ctx, cryptPassphrase);

        } catch (Exception e) {
            LogUtil.logException(ctx, LogUtil.LogType.CRYPT, e);
            success = false;
        }

        return success;
    }

    /**
     * Create the public/private keys used with the database passphrase.
     */
    public static void createDbKeystore( Context ctx) {

        KeystoreUtil.createKeyNotExists( ctx, KEY_ALIAS);
    }
}
