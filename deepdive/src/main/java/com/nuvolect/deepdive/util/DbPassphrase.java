/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import android.content.Context;

import com.nuvolect.deepdive.main.CConst;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.NoSuchPaddingException;

import static com.nuvolect.deepdive.util.Passphrase.generateRandomPassword;

/**
 * The passphrase is encrypted/decrypted with a public/private key * from the android keystore.
 * Assumes that to use Android keystore API is 19 or greater.
 */
public class DbPassphrase {

    /**
     * Decrypt the passphrase and return it as a character array.
     *
     * @param ctx
     * @return
     */
    public static char[] getDbPassphrase(Context ctx) throws IOException, CertificateException,
            NoSuchAlgorithmException, InvalidKeyException, UnrecoverableEntryException,
            InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchProviderException,
            KeyStoreException {

        if( ! Persist.keyExists(ctx, CConst.DB_PASSWORD)){

           char[] clearPassphrase = generateRandomPassword( 32, Passphrase.SYSTEM_MODE);
           Persist.putEncrypt(ctx, CConst.DB_PASSWORD, clearPassphrase);
           clearPassphrase = Passphrase.cleanArray( clearPassphrase);
        }

        return Persist.getDecrypt( ctx, CConst.DB_PASSWORD);
    }

    /**
     * Create the public/private keys used with the database passphrase.
     */
    public static void createDbKeystore( Context ctx) {

        KeystoreUtil.createKeyNotExists( ctx, CConst.APP_KEY_ALIAS);
    }
}
