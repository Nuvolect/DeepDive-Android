/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import android.util.Base64;

import com.nuvolect.deepdive.main.CConst;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import static android.util.Base64.decode;

/**
 * Encryption utilities for various data types using Android's Keystore.
 * Methods use a single key that is assumed already created.
 * These methods do not do any storage.
 * Methods attempt to cleanup any intermediate cleartext.
 */
public class CrypUtil {

    /**
     * Encrypt a four byte int and return it as a byte array.
     *
     * @param clearInt
     * @return
     * @throws Exception
     */
    public static byte[] encryptInt(int clearInt) throws Exception {

        byte[] clearBytes = ByteBuffer.allocate(4).putInt(clearInt).array();
        byte[] cryptBytes = encrypt( clearBytes);

        Passphrase.cleanArray( clearBytes);

        return cryptBytes;
    }

    /**
     * Decrypt a four byte array into an int.
     *
     * @param encryptBytes
     * @return
     * @throws Exception
     */
    public static int decryptInt( byte[] encryptBytes) throws Exception {

        byte[] clearBytes = decrypt(encryptBytes);
        ByteBuffer byteBuffer = ByteBuffer.wrap( clearBytes);
        int clearInt = byteBuffer.getInt();

        cleanArray( clearBytes);

        return clearInt;
    }

    /**
     * Encrypt a character array and return it as a byte array.
     * @param clearChars
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(char[] clearChars) throws  Exception {

        return CrypUtil.encrypt( CrypUtil.toBytesUTF8(clearChars));
    }

    /**
     * Encrypt a byte array and return it as a byte array.
     * @param clearBytes
     * @return
     * @throws Exception
     */
    public static byte[] encrypt( byte[] clearBytes) throws Exception {

        return KeystoreUtil.encrypt( CConst.APP_KEY_ALIAS, clearBytes);
    }

    /**
     * Decrypt a byte array and return it as a byte array.
     * @param encryptBytes
     * @return
     * @throws Exception
     */
    public static byte[] decrypt( byte[] encryptBytes) throws Exception {

        return KeystoreUtil.decrypt( CConst.APP_KEY_ALIAS, encryptBytes);
    }

    /**
     * Clear an array changing the contents to zero then changing the
     * array size to zero.
     *
     * It is recommended that the calling method should use the returned value.
     * Although the calling array values are set to zero, it is still possible
     * to detect the length of the array.
     *
     * Example:
     * dirtyArray  = CrypUtil.cleanArray( dirtyArray );
     *
     * There will be a zeroed array in the heap but it will no longer be associated
     * with the parameter array.
     *
     * @param dirtyArray
     * @return
     */
    public static byte[] cleanArray(byte[] dirtyArray) {

        for(int i = 0; i< dirtyArray.length; i++){

            dirtyArray[i] = 0;//Clear contents.
        }
        dirtyArray = new byte[0];//Don't save the size

        return dirtyArray;
    }

    /**
     * Copy a char array into a byte array using UTF-8 encoding.
     * @param chars
     * @return
     */
    public static byte[] toBytesUTF8(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    /**
     * Convert a String into a byte array using UTF-8 encoding.
     * @param stringUTF8
     * @return
     * @throws UnsupportedEncodingException
     */
    public static byte[] toBytesUTF8(String stringUTF8) throws UnsupportedEncodingException {

        return stringUTF8.getBytes( "UTF-8");
    }

    /**
     * Copy a byte array into a new char array.
     * @param bytes
     * @return
     */
    public static char[] toChar(byte[] bytes) {

        char[] charBuffer = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            charBuffer[i] = (char) (bytes[i] & 0xff);
        }
        return charBuffer;
    }

    public static String toStringUTF8(byte[] bytesUTF8) {

        char[] chars = toChar(bytesUTF8);
        return new String( chars);
    }

    public static String decodeFromB64(byte[] bytes) {

//        return Base64.encodeToString( bytes, Base64.DEFAULT);
        byte[] decodedBytes = Base64.decode( bytes, Base64.DEFAULT);
        return new String( decodedBytes);
    }

    public static byte[] encodeToB64(String clearString) throws UnsupportedEncodingException {

//        return Base64.decode( string, Base64.DEFAULT);
        return Base64.encode( clearString.getBytes("UTF-8"), Base64.DEFAULT);
    }

    public static String encodeToB64( byte[] bytes) throws UnsupportedEncodingException {

        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static byte[] getBytes(String string) throws UnsupportedEncodingException {

        return string.getBytes( "UTF-8");
    }

    public static byte[] decodeFromB64(String encodedString) {

        return Base64.decode( encodedString, Base64.DEFAULT);
    }
}
