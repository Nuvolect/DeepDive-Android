/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import android.content.Context;

import com.nuvolect.deepdive.webserver.SelfSignedCertificate;
import com.nuvolect.deepdive.webserver.SSLUtil;

import org.junit.Test;

import java.io.File;

import javax.net.ssl.SSLServerSocketFactory;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

//
//TODO create class description
//
public class KeystoreVazenTest {

    @Test
    public void createCertTest(){

        LogUtil.log( KeystoreUtilTest.class, CrypUtilTest.class.getCanonicalName()+" test starting");
        Context ctx = getTargetContext();

        // Create a self signed certificate and put it in a BKS keystore
        String keystoreFilename = "VazanKeystore.bks";

        File file = new File( ctx.getFilesDir(), keystoreFilename);
        file.delete();
        assertThat( file.exists(), is( false));

        Persist.deleteKey(ctx, Persist.SELFSIGNED_KS_KEY);
        assertThat( Persist.keyExists(ctx, Persist.SELFSIGNED_KS_KEY), is( false));

        String absolutePath = file.getAbsolutePath();
        SelfSignedCertificate.makeKeystore( ctx, absolutePath, false);
        assertThat( file.exists(), is( true));
        assertThat( Persist.keyExists(ctx, Persist.SELFSIGNED_KS_KEY), is( true));

        try {
            SSLServerSocketFactory sslServerSocketFactory = SSLUtil.configureSSLPath(ctx, absolutePath);
            String[] suites = sslServerSocketFactory.getSupportedCipherSuites();
            assertThat( suites.length > 0, is( true));

        } catch (Exception e) {
            LogUtil.logException(KeystoreUtilTest.class, e);
        }
        file.delete();
        assertThat( file.exists(), is( false));

        Persist.deleteKey(ctx, Persist.SELFSIGNED_KS_KEY);
        assertThat( Persist.keyExists(ctx, Persist.SELFSIGNED_KS_KEY), is( false));

        LogUtil.log( KeystoreUtilTest.class, CrypUtilTest.class.getCanonicalName()+" test ending, certificate deleted");
    }

}
