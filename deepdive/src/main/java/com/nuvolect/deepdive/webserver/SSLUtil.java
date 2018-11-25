/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver;

import android.content.Context;

import com.nuvolect.deepdive.main.CConst;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniUtil;
import com.nuvolect.deepdive.util.Persist;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;

import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Configure SSL
 */
public class SSLUtil {

    /**
     * Creates an SSLSocketFactory for HTTPS loading certificate from path.
     *
     * @param ctx
     * @param path
     * @return
     * @throws IOException
     */
    public static SSLServerSocketFactory configureSSLPath(Context ctx, String path) throws IOException {

        SSLServerSocketFactory sslServerSocketFactory = null;
        try {
            // Android does not have the default jks but uses bks
            KeyStore keystore = KeyStore.getInstance("BKS");

            char[] passphrase = Persist.getDecrypt( ctx, CConst.SELFSIGNED_KS_KEY);

            OmniFile loadFile = new OmniFile("u0", path);
            InputStream keystoreStream = loadFile.getFileInputStream();
            keystore.load(keystoreStream, passphrase);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keystore);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, passphrase);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            sslServerSocketFactory = sslContext.getServerSocketFactory();

            String[] defaultCiphersuites = sslServerSocketFactory.getDefaultCipherSuites();
            String[] supportedCipherSuites = sslServerSocketFactory.getSupportedCipherSuites();

            if( LogUtil.DEBUG){

                SSLEngine sslEngine = sslContext.createSSLEngine();
                String[] enabledCipherSuites = sslEngine.getEnabledCipherSuites();
                String[] enabledProtocols = sslEngine.getEnabledProtocols();

                String log = path;
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

        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        return sslServerSocketFactory;
    }
    /**
     * Creates an SSLSocketFactory for HTTPS loading certificate from assets.
     *
     * Pass a KeyStore resource with your certificate and passphrase
     */
    public static SSLServerSocketFactory configureSSLAsset(String assetCertPath, char[] passphrase) throws IOException {

        SSLServerSocketFactory sslServerSocketFactory = null;
        try {
            // Android does not have the default jks but uses bks
            KeyStore keystore = KeyStore.getInstance("BKS");
            InputStream keystoreStream = WebService.class.getResourceAsStream(assetCertPath);
            keystore.load(keystoreStream, passphrase);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keystore);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, passphrase);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            sslServerSocketFactory = sslContext.getServerSocketFactory();

        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }

        return sslServerSocketFactory;
    }

    /**
     * Store certificate to a keystore file.
     * @param cert
     * @param passcode
     * @param outFile
     * @return
     */
    public static boolean storeCertInKeystore( byte [] cert, char [] passcode, OmniFile outFile){

        try {
            FileOutputStream fos = new FileOutputStream( outFile.getStdFile());

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream certstream = new ByteArrayInputStream(cert);
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(certstream);

            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load( null, passcode);// Initialize it
            keyStore.setCertificateEntry("mycert", certificate);
            keyStore.store( fos, passcode);
            fos.close();

//            int numEntries = keyStore.size();
//            Long size = outFile.length();

            return true;

        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void probeCert(String certPath, char[] password) throws IOException {

        try {
            // Android does not have the default jks but uses bks
            KeyStore keystore = KeyStore.getInstance("BKS");
            OmniFile certFile = new OmniFile("u0", certPath);
            InputStream keystoreStream = certFile.getFileInputStream();
            keystore.load(keystoreStream, password);

            String log = "Certificate filename: "+ certFile.getName();
            log += "\ncert path: "+ certPath;
            log += "\ncert password: "+ password;

            String alias = "";
            Enumeration<String> aliases = keystore.aliases();
            for (; aliases.hasMoreElements(); ) {
                String s = aliases.nextElement();
                log += "\nAlias: "+s;
                if (alias.isEmpty())
                    alias = s;
            }
            log += probeKeystore( keystore, alias, password);
            log += probeCert( keystore.getCertificate(alias));

            OmniUtil.writeFile( new OmniFile("u0", certFile.getName()+"_probe.txt"), log);

        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private static String probeKeystore(KeyStore keystore, String alias, char[] password) {

        String log = "\n";
        try {
            log += "\nCreation date: " + keystore.getCreationDate(alias).toString();
            log += "\nKeystore type: " + keystore.getType();

            Provider provider = keystore.getProvider();
            log += "\nProvider name: " + provider.getName();
            log += "\nProvider info: " + provider.getInfo();

            Key key = keystore.getKey(alias, password);
            if( key != null){
                log += "\nKey algorithm: " + key.getAlgorithm();
                log += "\nKey format: " + key.getFormat();
                log += "\nKey toString: " + key.toString();
            }else
                log += "\nKey  is null";

        } catch (KeyStoreException e) {
            e.printStackTrace();
            log += e.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            log += e.toString();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
            log += e.toString();
        }

        return log;
    }

    private static String probeCert(Certificate cert) {

        String log = "\n";

        PublicKey pubKey = cert.getPublicKey();

        try {
            log += "\nPublic key algorithm: " + pubKey.getAlgorithm();
            log += "\nPublic key format: " + pubKey.getFormat();
            log += "\nPublic key hashcode: " + String.valueOf(pubKey.hashCode());
            log += "\nPublic key toString: " + pubKey.toString();

            log += "\ncert type: " + cert.getType();
            log += "\ncert hashcode: " + String.valueOf(cert.hashCode());
            log += "\ncert toString: " + cert.toString();
        } catch (Exception e) {
            e.printStackTrace();
            log += e.toString();
        }

        return log;
    }
}
