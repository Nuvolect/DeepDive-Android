/*
 * Copyright (c) 2017. Nuvolect LLC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.nuvolect.deepdive.util;
//
//TODO create class description
//

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;


public class KeystoreUtil {

    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final String KEYSTORE_PROVIDER_ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final int BASE64 = Base64.URL_SAFE;
    private static SecureRandom random = new SecureRandom();

    private static String error = "";
    private static JSONObject result;

    private enum TEST_ID {
        NIL,
        create_key,
        decrypt,
        delete_key,
        encrypt,
        get_keys,
        lockscreen_test,
    }
    public static JSONObject dispatch(Context ctx, Map<String, String> params) {

        TEST_ID test_id = null;
        try {
            test_id = TEST_ID.valueOf(params.get("test_id"));
        } catch (IllegalArgumentException e) {
            error = "Error, invalid command: "+params.get("test_id");
        }
        assert test_id != null;
        result = new JSONObject();

        try {
            switch ( test_id ){

                case create_key:{

                    result = KeystoreUtil.createKey( ctx, params.get("key_alias"));
                    break;
                }
                case delete_key:{

                    result = KeystoreUtil.deleteKey( ctx, params.get("key_alias"));
                    break;
                }
                case encrypt:{

                    String key_alias = params.get("key_alias");
                    String clear_text = params.get("cleartext");
                    result = KeystoreUtil.encrypt( key_alias, clear_text);
                    break;
                }
                case decrypt:{

                    String key_alias = params.get("key_alias");
                    String cipher_text_b64 = params.get("ciphertext");
                    result = KeystoreUtil.decrypt( key_alias, cipher_text_b64);
                    break;
                }
                case get_keys:{

                    JSONArray keys = KeystoreUtil.getKeys();
                    result.put("keys",keys);
                    break;
                }
                case lockscreen_test:{

                    result = KeystoreUtil.testAndroidLockscreenEnabled( ctx);
                    break;
                }

                default:
                    error = "Invalid test: "+test_id;
            }
        } catch (Exception e) {
            LogUtil.logException( KeystoreUtil.class, e);
            error = "Exception";
        }
        return result;
    }
    /**
     * Test if the Android system lockscreen is enabled by creating a keystore item.
     * Creating the item will fail if the lockscreen is not enabled.
     * @param ctx
     * @return
     */
    public static JSONObject testAndroidLockscreenEnabled(Context ctx){

        String LOCKSCREEN_TEST = "lockscreen_test";
        String lockscreenEnabled  = "disabled";

        JSONObject result = createKey(ctx, LOCKSCREEN_TEST);
        try {

            if( result.getString("error").contains("Secure lock screen must be enabled")){

                lockscreenEnabled = "disabled";
            }else
            if( result.getString("success").contentEquals("true"))
                lockscreenEnabled = "enabled";

            result.put(LOCKSCREEN_TEST,lockscreenEnabled);

        } catch (JSONException e) {
            LogUtil.logException( KeystoreUtil.class, e);
        }
        return result;
    }
    /**
     * Creates a public and private key and stores it using the Android Key Store, so that only
     * this application will be able to access the keys.
     */
    public static JSONObject createKey(Context ctx, String key_alias) {

        String public_key = "", privateKeyEntryString = "";
        String error = "";
        JSONObject result = new JSONObject();

            try {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 1);

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(ctx)
                        .setAlias(key_alias)
                        .setSubject(new X500Principal("CN=SecureSuite, O=Nuvolect"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();

                KeyPairGenerator generator = KeyPairGenerator.getInstance( "RSA","AndroidKeyStore");
                generator.initialize(spec);

                KeyPair keyPair = generator.generateKeyPair();
                public_key = keyPair.getPublic().toString();

                // Return details of the private key
                KeyStore ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
                ks.load(null);
                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)ks.getEntry( key_alias, null);
                privateKeyEntryString = privateKeyEntry.toString();

            } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                    UnrecoverableEntryException | KeyStoreException | CertificateException |
                    IOException | NoSuchProviderException  e) {

                error = e.getMessage();
                LogUtil.logException( KeystoreUtil.class, e);
            }

        try {
            result.put("public_key", public_key);
            result.put("private_key_entry", privateKeyEntryString);
            result.put("error", error);
            result.put("success", error.length()==0?"true":"false");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONObject encrypt(String key_alias, String plaintext){

        String cipherTextB64 = "";
        JSONObject result = new JSONObject();
        String error = "";
        try {

            KeyStore ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            ks.load(null);
            if( ks.containsAlias( key_alias)){

                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)ks.getEntry( key_alias, null);
                RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

//                Cipher rsaCipher = Cipher.getInstance( CIPHER_ALGORITHM, "AndroidOpenSSL"); //AndroidOpenSSL is deprecated
                Cipher rsaCipher = Cipher.getInstance( CIPHER_ALGORITHM );
                rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                CipherOutputStream cipherOutputStream = new CipherOutputStream( outputStream, rsaCipher);
                cipherOutputStream.write(plaintext.getBytes("UTF-8"));
                cipherOutputStream.close();

                byte [] cipherBytes = outputStream.toByteArray();
                cipherTextB64 = Base64.encodeToString(cipherBytes, BASE64);

            }else{
                error = "Key alias not found: "+key_alias;
            }

        } catch ( Exception e) {

            error = e.getCause().toString();
            LogUtil.logException( KeystoreUtil.class, e);
        }

        try {
            result.put("ciphertext", cipherTextB64);
            result.put("success", error.length()==0?"true":"false");
            result.put("error", error);

        } catch (JSONException e) {
            LogUtil.logException( KeystoreUtil.class, e);
        }

        return result;
    }

    public static JSONObject decrypt(String key_alias, String cipherTextB64){

        String cleartext = "", privateKeyEntryString="";
        JSONObject result = new JSONObject();
        String error = "";
        try {

            KeyStore ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            ks.load(null);
            if( ks.containsAlias( key_alias)){

                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)ks.getEntry( key_alias, null);
                privateKeyEntryString = privateKeyEntry.toString();

                Cipher output = Cipher.getInstance( CIPHER_ALGORITHM);
                output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

                CipherInputStream cipherInputStream = new CipherInputStream(
                        new ByteArrayInputStream(Base64.decode( cipherTextB64, BASE64)), output);
                ArrayList<Byte> values = new ArrayList<>();
                int nextByte;
                while ((nextByte = cipherInputStream.read()) != -1) {
                    values.add((byte)nextByte);
                }

                byte[] bytes = new byte[values.size()];
                for(int i = 0; i < bytes.length; i++) {
                    bytes[i] = values.get(i).byteValue();
                }
                cleartext = new String(bytes, 0, bytes.length, "UTF-8");

                if( ! cipherTextB64.isEmpty() && cleartext.isEmpty())
                    error = "ERROR: Decrypt produced empty string";

            }else{
                error = "Key alias not found: "+key_alias;
            }

        } catch ( Exception e) {

            error = e.getMessage();
            LogUtil.logException( KeystoreUtil.class, e);
        }

        try {
            result.put("cleartext", cleartext);
            result.put("private_key_entry", privateKeyEntryString);
            result.put("success", error.length()==0?"true":"false");
            result.put("error", error);

        } catch (JSONException e) {
            LogUtil.logException( KeystoreUtil.class, e);
        }

        return result;
    }

    public static JSONObject deleteKey(Context ctx, String alias){

        JSONObject result = new JSONObject();
        String error = "";
        try {

            KeyStore ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            ks.load(null);
            if( ks.containsAlias( alias)){

                ks.deleteEntry( alias);
            }else{

                error = "Key alias "+alias+" not found";
            }


        } catch (KeyStoreException | CertificateException
                | NoSuchAlgorithmException | IOException e) {

            error = e.getCause().toString();
            LogUtil.logException( KeystoreUtil.class, e);
        }

        try {
            result.put("error", error);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get the names of all keys created by our app
     * @return
     */
    public static JSONArray getKeys() {

        KeyStore ks = null;
        JSONArray keys = new JSONArray();

        try {
            ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            ks.load(null);
            Enumeration<String> ksEnumeration = ks.aliases();

            while( ksEnumeration.hasMoreElements()){

                JSONObject obj = new JSONObject();
                String key_alias = ksEnumeration.nextElement();
                obj.put("alias", key_alias);

                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)ks.getEntry( key_alias, null);

                String s = privateKeyEntry.getCertificate().toString();

                obj.put("certificate", s);

                keys.put( obj );
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                IOException | JSONException | UnrecoverableEntryException e) {

            LogUtil.logException(KeystoreUtil.class, e);
        }

        return keys;
    }

    /**
     * Create a public/private key if it does not already exist.
     * @param ctx
     * @param keyAlias
     * @return boolean: true if the key is created.
     */
    public static boolean createKeyNotExists(Context ctx, String keyAlias) {

        KeyStore ks = null;
        boolean keyCreated = false;
        try {

            ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            ks.load(null);
            if( ks.containsAlias( keyAlias))
                return false;

            /**
             * Key does not exist.
             * Create the key.
             */
            JSONObject jsonObject = createKey( ctx, keyAlias);
            keyCreated = jsonObject.getString( "success").contentEquals( "true");

        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                IOException | JSONException e) {

            LogUtil.logException(KeystoreUtil.class, e);
        }

        return keyCreated;
    }
}
