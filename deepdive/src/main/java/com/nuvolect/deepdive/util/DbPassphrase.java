/*
 * Copyright (c) 2017. Nuvolect LLC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Contact legal@nuvolect.com for a less restrictive commercial license if you would like to use the
 * software without the GPLv3 restrictions.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
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
