package com.nuvolect.deepdive.util;

import android.content.Context;
import android.util.Base64;

import com.nuvolect.deepdive.license.AppSpecific;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * How can I convert String to SecretKey
 *
 * http://stackoverflow.com/questions/4551263/how-can-i-convert-string-to-secretkey/8828196#8828196
 *
 * No integrity checks, for these particular reasons

 1. The need is not apparent from the use case.
 2. "AES/GCM/NoPadding" mode is only available from Java 7 onward
 3. It depends on the user if they want to deploy e.g. HMAC and/or AESCMAC (recommended).
 4. It would require an additional key at the minimum, and two full passes.

 If you got an implementation of GCM mode at both sides - e.g. using Bouncy Castle on Java 6 -
 please go for it, as it is much more secure (as long as the "IV" is really unique).
 It should be really easy to change the implementation.

 Implementation notes regarding encryption

 1. This implementation is not safe when used in an unrestricted client / server role
 because of padding oracle attacks (they require 128 tries per byte or lower, on average,
 independent of algorithm or key size).
 You will need to use a MAC, HMAC or Signature over the encrypted data,
 and verify it before decrypting to deploy it in client/server mode.
 2. Decrypt will return null if decryption fails. This can only indicate a padding exception,
 which should be adequately handled (did I warn about padding oracle attacks?)
 3. Invalid keys will be returned as InvalidArgumentException.
 4. All other security related exceptions are "swept under the table" as it means that the
 Java runtime environment is invalid. For example, supporting "UTF-8" and "AES/CBC/PKCS5Padding"
 is required for every Java SE implementation.

 Some other notes

 1. Please don't try the opposite and insert bytes directly into the input string of
 the encrypt method (using new String(byte[]) for instance). The method may fail silently!
 2. Optimized for readability. Go for Base64 stream and CipherStream implementations
 if you rather prefer speed and better memory footprint.
 3. You need at least Java 6 SE or compatible to run this code.
 4. Encryption/decryption may fail for AES key sizes over 128 bit
 as you may need policy files for unrestricted encryption (available from Oracle)
 5. Beware of governmental regulations when exporting encryption.
 6. This implementation uses hex keys instead of base64 keys as they are small enough,
 and hex is just easier to edit/verify manually.
 7. Used hex and base64 encoding/decoding retrieved from the JDK,
 no external libraries needed whatsoever.
 8. Uber simple to use, but of course not very object oriented,
 no caching of object instances used in encrypt/decrypt. Refactor at will.

 Usage:

 String plain = "Zaphod's just zis guy, ya knöw?";
 String encrypted = encrypt(plain, "000102030405060708090A0B0C0D0E0F");
 System.out.println(encrypted);
 String decrypted = decrypt(encrypted, "000102030405060708090A0B0C0D0E0F");
 if (decrypted != null && decrypted.equals(plain)) {
     System.out.println("Hey! " + decrypted);
 } else {
     System.out.println("Bummer!");
 }
 */
public class SymmetricCrypto {

    public static String decrypt(Context ctx, String encrypted){

        try {
            if( encrypted.isEmpty())
                return "";
            else{

                String clearText = decrypt(AppSpecific.APP_CRYP_SEED_HEX, encrypted);
                if( clearText != null)
                    return clearText;
            }
        } catch (Exception e) {
            LogUtil.logException(ctx, LogUtil.LogType.CRYPT, e);
        }

        return "";
    }

    public static String encrypt(Context ctx, String cleartext){

        try {
            return encrypt(AppSpecific.APP_CRYP_SEED_HEX, cleartext);
        } catch (Exception e) {
            LogUtil.logException(ctx, LogUtil.LogType.CRYPT, e);
        }
        return "";
    }
    /**
     * Encrypt plaintext to encrypted text. symKeyHex needs to be 32 chars long.
     * @param clearText
     * @param symKeyHex
     * @return
     */
    public static String encrypt( final String symKeyHex,  final String clearText ) {

        assert symKeyHex.length() == 32;

        final byte[] symKeyData = Base64.decode(symKeyHex, Base64.DEFAULT);

        final byte[] encodedMessage = clearText.getBytes(Charset.forName("UTF-8"));

        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final int blockSize = cipher.getBlockSize();

            // create the key
            final SecretKeySpec symKey = new SecretKeySpec(symKeyData, "AES");

            // generate random IV using block size (possibly create a method for this)
            final byte[] ivData = new byte[blockSize];
            final SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
            rnd.nextBytes(ivData);
            final IvParameterSpec iv = new IvParameterSpec(ivData);

            cipher.init(Cipher.ENCRYPT_MODE, symKey, iv);

            final byte[] encryptedMessage = cipher.doFinal(encodedMessage);

            // concatenate IV and encrypted message
            final byte[] ivAndEncryptedMessage = new byte[ivData.length + encryptedMessage.length];
            System.arraycopy(ivData, 0, ivAndEncryptedMessage, 0, blockSize);
            System.arraycopy(encryptedMessage, 0, ivAndEncryptedMessage,
                    blockSize, encryptedMessage.length);

            final String ivAndEncryptedMessageBase64 =
                    Base64.encodeToString(ivAndEncryptedMessage, Base64.DEFAULT);

            return ivAndEncryptedMessageBase64;

        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(
                    "key argument does not contain a valid AES key");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Unexpected exception during encryption", e);
        }
    }

    /**
     * Decrypt an encrypted string into plain text. symKeyHex needs to be 32 chars long.
     * @param symKeyHex
     * @param ivAndEncryptedMessageBase64
     * @return
     */
    public static String decrypt( final String symKeyHex, final String ivAndEncryptedMessageBase64 ) {

        assert symKeyHex.length() == 32;

        final byte[] symKeyData = Base64.decode((symKeyHex), Base64.DEFAULT);

        final byte[] ivAndEncryptedMessage = Base64.decode(ivAndEncryptedMessageBase64, Base64.DEFAULT);
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final int blockSize = cipher.getBlockSize();

            // create the key
            final SecretKeySpec symKey = new SecretKeySpec(symKeyData, "AES");

            // retrieve random IV from start of the received message
            final byte[] ivData = new byte[blockSize];
            System.arraycopy(ivAndEncryptedMessage, 0, ivData, 0, blockSize);
            final IvParameterSpec iv = new IvParameterSpec(ivData);

            // retrieve the encrypted message itself
            final byte[] encryptedMessage = new byte[ivAndEncryptedMessage.length
                    - blockSize];
            System.arraycopy(ivAndEncryptedMessage, blockSize,
                    encryptedMessage, 0, encryptedMessage.length);

            cipher.init(Cipher.DECRYPT_MODE, symKey, iv);

            final byte[] encodedMessage = cipher.doFinal(encryptedMessage);

            // concatenate IV and encrypted message
            final String message = new String(encodedMessage, Charset.forName("UTF-8"));

            return message;

        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(
                    "key argument does not contain a valid AES key");
        } catch (BadPaddingException e) {
            // you'd better know about padding oracle attacks
            return null;
        } catch (GeneralSecurityException e) {
            LogUtil.log(LogUtil.LogType.BETTER_CRYPTO, "GeneralSecurityException");
            LogUtil.logException(LogUtil.LogType.BETTER_CRYPTO, e);
            return null;
//            throw new IllegalStateException(
//                    "Unexpected exception during decryption", e);
        }
    }
}
