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
import com.nuvolect.deepdive.survey.DeviceSurvey;

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

    public static final String PASSWORD_GEN_HISTORY = "password_gen_history";
    public static final String PASSWORD_TARGET      = "password_target";
    public static final String PASSWORD_LENGTH      = "password_length";
    public static final String PASSWORD_GEN_MODE    = "password_gen_mode";


    public static char[] generateRandomPassword(int length, int mode) {

        //FIXME revisit requirements to include at least 1 of each char type

        String characters = "";

        if( (mode & ALPHA_UPPER) > 0)
            characters += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        if( (mode & ALPHA_LOWER) > 0)
            characters += "abcdefghijklmnopqrstuvwxyz";
        if( (mode & NUMERIC) > 0)
            characters += "0123456789";
        if( (mode & SPECIAL) > 0)
            characters += "!$%@#";
        if( (mode & HEX) > 0)
            characters += "0123456789abcdef";

        if( characters.isEmpty())
            characters = "0123456789";

        int charactersLength = characters.length();

        char[] ranChars = new char[ length];

        for (int i = 0; i < length; i++) {
            double index = Math.random() * charactersLength;
            ranChars[i] = characters.charAt((int) index);
        }
        return ranChars;
    }

    /** Decrypt the passphrase and return it as a string */
    public static String getDbPassphrase(Context ctx) {

        String clearPassphrase = "";
        String cryptPassphrase = Persist.getEncryptedPassphrase(ctx);

        if( cryptPassphrase.equals(CConst.DEFAULT_PASSPHRASE)){

            // First time, create a random passcode, encrypt and save it
            //FIXME use char[], zero it when complete
            clearPassphrase = generateRandomPassword( 32, HEX).toString();
            boolean success = putDbPassphrase(ctx, clearPassphrase);

            assert success;

            return clearPassphrase;
        }
        try {

            /**
             * Create a 32 hex char key
             */
            String uniqueInstallId = DeviceSurvey.getUniqueInstallId(ctx);
            String md5Key = LicenseUtil.md5(CConst.RANDOM_EDGE + uniqueInstallId);
            clearPassphrase = SymmetricCrypto.decrypt( md5Key, cryptPassphrase);

        } catch (Exception e) {
            LogUtil.logException(ctx, LogUtil.LogType.CRYPT, e);
        }
        return clearPassphrase;
    }

    /** Encrypt the passphrase and save it to persist */
    public static boolean putDbPassphrase(Context ctx, String passphrase){

        boolean success = true;
        try {

            String uniqueInstallId = DeviceSurvey.getUniqueInstallId(ctx);
            String md5Key = LicenseUtil.md5( CConst.RANDOM_EDGE + uniqueInstallId);
            String cryptPassphrase = SymmetricCrypto.encrypt( md5Key, passphrase);
            Persist.setEncryptedPassphrase(ctx, cryptPassphrase);

        } catch (Exception e) {
            LogUtil.logException(ctx, LogUtil.LogType.CRYPT, e);
            success = false;
        }

        return success;
    }

    public static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    public static char[] toChars(byte[] bytes){

        char chars[] = new char[0];

        try {
            chars = new String( bytes, "UTF-8").toCharArray();
        } catch (UnsupportedEncodingException e) {
            LogUtil.log(" Exception in toChars");
        }
        return chars;
    }
}
