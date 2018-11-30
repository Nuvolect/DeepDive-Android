/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class Passphrase {

    public static int ALPHA_UPPER = 1;
    public static int ALPHA_LOWER = 2;
    public static int NUMERIC     = 4;
    public static int SPECIAL     = 8;
    public static int HEX         = 16;
    public static int SYSTEM_MODE = ALPHA_UPPER | ALPHA_LOWER | NUMERIC;


    /**
     * Generate a random password of the specific length using a variety of character types.
     * Does not guarantee each variety of character types is used.
     * @param length
     * @param mode
     * @return
     */
    public static char[] generateRandomPassword(int length, int mode) {

        StringBuffer sourceBuffer = new StringBuffer( 0 );

        if( (mode & ALPHA_UPPER) > 0)
            sourceBuffer.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if( (mode & ALPHA_LOWER) > 0)
            sourceBuffer.append("abcdefghijklmnopqrstuvwxyz");
        if( (mode & NUMERIC) > 0)
            sourceBuffer.append("0123456789");
        if( (mode & SPECIAL) > 0)
            sourceBuffer.append("!$%@#");
        if( (mode & HEX) > 0)
            sourceBuffer.append("0123456789abcdef");

        if( sourceBuffer.length() == 0)
            sourceBuffer.append("0123456789");

        int sourceLength = sourceBuffer.length();

        char[] ranChars = new char[ length];

        for (int i = 0; i < length; i++) {
            double index = Math.random() * sourceLength;
            ranChars[i] = sourceBuffer.charAt((int) index);
        }
        sourceBuffer.delete( 0, sourceLength);

        return ranChars;
    }

    /**
     * Convert a char array to a byte array.
     * @param chars
     * @return
     */
    public static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    /**
     * Convert a byte array to a char array.
     * @param bytes
     * @return
     */
    public static char[] toChars(byte[] bytes){

        char chars[] = new char[0];

        try {
            chars = new String( bytes, "UTF-8").toCharArray();
        } catch (UnsupportedEncodingException e) {
            LogUtil.log(" Exception in toChars");
        }
        return chars;
    }

    /**
     * Clear an array changing the contents to zero then changing the
     * array size to zero.
     *
     * @param dirtyArray
     */
    public static char[] cleanArray(char[] dirtyArray) {

        for(int i = 0; i< dirtyArray.length; i++){

            dirtyArray[i] = 0;//Clear contents.
        }
        dirtyArray = new char[0];//Don't save the size

        return dirtyArray;
    }

    /**
     * Clear an array changing the contents to zero then changing the
     * array size to zero.
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
}
