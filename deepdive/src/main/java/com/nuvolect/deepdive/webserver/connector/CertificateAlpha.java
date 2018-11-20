/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver.connector;

import com.nuvolect.deepdive.util.LogUtil;

import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.X500NameBuilder;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.X509v1CertificateBuilder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

//
//TODO create class description
//
public class CertificateAlpha {

    public static void makeCertificate(String path) {

        try {

            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair keyPair = kpg.genKeyPair();

            Date startDate = new Date(System.currentTimeMillis() - 24L * 60 * 60 * 1000);
            Date endDate = new Date(System.currentTimeMillis() + 20L * 365 * 24 * 60 * 60 * 1000);

            X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
            nameBuilder.addRDN(BCStyle.O, "Nuvolect LLC");
            nameBuilder.addRDN(BCStyle.OU, "Development");
            nameBuilder.addRDN(BCStyle.L, "Orlando, FL, USA");

            X500Name x500Name = nameBuilder.build();
            Random random = new Random();

            SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
            X509v1CertificateBuilder v1CertGen = new X509v1CertificateBuilder(
                    x500Name
                    , BigInteger.valueOf(random.nextLong())
                    , startDate
                    , endDate
                    , x500Name
                    , subjectPublicKeyInfo);

            // Prepare Signature:
            ContentSigner sigGen = null;
            /**
             * : org.spongycastle.operator.OperatorCreationException: cannot create signer: no such provider: RSA
             * :     at org.spongycastle.operator.jcajce.JcaContentSignerBuilder.build(JcaContentSignerBuilder.java:102)
             * :     at com.nuvolect.deepdive.webserver.connector.CertificateAlpha.makeCertificate(CertificateAlpha.java:74)
             * :     at com.nuvolect.deepdive.webserver.WebService.onCreate(WebService.java:118)
             * :     at android.app.ActivityThread.handleCreateService(ActivityThread.java:3531)
             * :     at android.app.ActivityThread.-wrap6(ActivityThread.java)
             * :     at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1729)
             * :     at android.os.Handler.dispatchMessage(Handler.java:102)
             * :     at android.os.Looper.loop(Looper.java:154)
             * :     at android.app.ActivityThread.main(ActivityThread.java:6780)
             * :     at java.lang.reflect.Method.invoke(Native Method)
             * :     at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1496)
             * :     at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1386)
             * : Caused by: java.security.NoSuchProviderException: no such provider: RSA
             * :     at sun.security.jca.GetInstance.getService(GetInstance.java:83)
             * :     at sun.security.jca.GetInstance.getInstance(GetInstance.java:206)
             * :     at java.security.Signature.getInstance(Signature.java:451)
             * :     at org.spongycastle.jcajce.util.NamedJcaJceHelper.createSignature(NamedJcaJceHelper.java:99)
             * :     at org.spongycastle.operator.jcajce.OperatorHelper.createSignature(OperatorHelper.java:303)
             * :     at org.spongycastle.operator.jcajce.JcaContentSignerBuilder.build(JcaContentSignerBuilder.java:61)
             * : 	... 11 more
             */
            try {
                Security.addProvider(new BouncyCastleProvider());
                sigGen = new JcaContentSignerBuilder("SHA256WithRSAEncryption")//ERROR throws exception
//                        .setProvider("RSA") // Throws exception, no such provider
                        .build(keyPair.getPrivate());
            } catch (Exception e) {

                LogUtil.logException( CertificateAlpha.class, "JcaContentSingerBuilder", e);
            }
            // Self sign :
            X509CertificateHolder x509CertificateHolder = v1CertGen.build(sigGen);
//            org.spongycastle.asn1.x509.Certificate cert = x509CertificateHolder.toASN1Structure();

            // convert to JRE certificate
            JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
            converter.setProvider(new BouncyCastleProvider());
            X509Certificate x509 = converter.getCertificate(x509CertificateHolder);

            // serialize in DER format
//            byte[] serialized = x509.getEncoded();

            KeyStore keystore = KeyStore.getInstance("BKS");
            String ALIAS = "alias";

            char[] passphrase = "some passphrase".toCharArray();

            keystore.load(null, passphrase);

            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128);
            SecretKey key = kg.generateKey();

            KeyStore.SecretKeyEntry ske = new KeyStore.SecretKeyEntry(key);
            KeyStore.ProtectionParameter kspp = new KeyStore.PasswordProtection(passphrase);
            keystore.setEntry(ALIAS, ske, kspp);
            keystore.setCertificateEntry("alias2", x509);

            // Store away the keystore.
            FileOutputStream fos = new FileOutputStream( path);
            keystore.store(fos, passphrase);
            fos.close();
            LogUtil.log("Keystore creation complete");

        } catch (Exception e) {
            LogUtil.logException( CertificateAlpha.class, "CertificateAlpha.java", e);
        }
    }
}
