/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.probe;

import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniUtil;

import java.io.File;
import java.security.Provider;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

//
//TODO create class description
//
public class InspectCert {

    public static void inspectSocketFactory(SSLContext sslContext, SSLServerSocketFactory sslServerSocketFactory, String absolutePath, TrustManagerFactory trustManagerFactory, File loadFile) {

        String[] defaultCiphersuites = sslServerSocketFactory.getDefaultCipherSuites();
String[] supportedCipherSuites = sslServerSocketFactory.getSupportedCipherSuites();

                SSLEngine sslEngine = sslContext.createSSLEngine();
                String[] enabledCipherSuites = sslEngine.getEnabledCipherSuites();
                String[] enabledProtocols = sslEngine.getEnabledProtocols();

                String log = absolutePath;
                String algorithm = trustManagerFactory.getAlgorithm();
                Provider provider = trustManagerFactory.getProvider();

                log += "\n\nalgorithm: "+algorithm;
                log += "\n\nprovider: "+provider;
                log += "\n\ndefaultCipherSuites: \n"+Arrays.toString(defaultCiphersuites);
                log += "\n\nsupportedCipherSuites: \n"+Arrays.toString(supportedCipherSuites);
                log += "\n\nenabledCipherSuites: \n"+Arrays.toString(enabledCipherSuites);
                log += "\n\nenabledProtocols: \n"+Arrays.toString(enabledProtocols);

                OmniUtil.writeFile(new OmniFile("u0", "SSL_Factory_"+loadFile.getName()+"_log.txt"), log);

                LogUtil.log("SSL configure successful");
    }
}
