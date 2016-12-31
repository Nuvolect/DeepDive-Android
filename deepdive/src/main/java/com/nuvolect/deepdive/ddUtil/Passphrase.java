package com.nuvolect.deepdive.ddUtil;

import android.content.Context;

import com.nuvolect.deepdive.license.LicenseUtil;
import com.nuvolect.deepdive.survey.DeviceSurvey;

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


    public static String generateRandomString(int length, int mode) {

        StringBuffer buffer = new StringBuffer();
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

        for (int i = 0; i < length; i++) {
            double index = Math.random() * charactersLength;
            buffer.append(characters.charAt((int) index));
        }
        return buffer.toString();
    }

    /** Decrypt the passphrase and return it as a string */
    public static String getDbPassphrase(Context ctx) {

        String clearPassphrase = "";
        String cryptPassphrase = Persist.getEncryptedPassphrase(ctx);

        if( cryptPassphrase.equals(CConst.DEFAULT_PASSPHRASE)){

            // First time, create a random passcode, encrypt and save it
            clearPassphrase = generateRandomString( 32, HEX);
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
            clearPassphrase = BetterCrypto.decrypt( md5Key, cryptPassphrase);

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
            String cryptPassphrase = BetterCrypto.encrypt( md5Key, passphrase);
            Persist.setEncryptedPassphrase(ctx, cryptPassphrase);

        } catch (Exception e) {
            LogUtil.logException(ctx, LogUtil.LogType.CRYPT, e);
            success = false;
        }

        return success;
    }
}