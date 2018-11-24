/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver;

import android.content.Context;

import com.nuvolect.deepdive.main.CConst;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.Passphrase;
import com.nuvolect.deepdive.util.Persist;

import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.X500NameBuilder;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.ExtendedKeyUsage;
import org.spongycastle.asn1.x509.Extension;
import org.spongycastle.asn1.x509.KeyPurposeId;
import org.spongycastle.asn1.x509.KeyUsage;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.crypto.NoSuchPaddingException;

/**
 * Generate a self-signed certificate and store it in a keystore.
 * The keystore password is secured in Android's keystore.
 *
 * Here's a complete self-signed ECDSA certificate generator that creates certificates
 * usable in TLS connections on both client and server side.
 * It was tested with BouncyCastle 1.57. Similar code can be used to create RSA certificates.
 *
 * https://stackoverflow.com/questions/29852290/self-signed-x509-certificate-with-bouncy-castle-in-java
 * Robert Vazan, https://stackoverflow.com/users/1981276/robert-va%C5%BEan
 */
public class KeystoreVazen {

    //SPRINT refactor storePassword to use a random password in Android Keystore
    //SPRINT allow keystore storage in private app storage or in OMNIFile path

    public static boolean makeKeystore(Context ctx, String keystoreFilepath, boolean recreate){

        boolean success = true;
        OmniFile keystoreFile = new OmniFile("u0", keystoreFilepath);

        // If the keystore already exists and recreate not requested, nothing to do.
        boolean skipOut = ! keystoreFile.exists();
        if( skipOut && ! recreate)
            return success;

        try {

            // Generate a random password, encrypt it with Android keystore then persist encrypted value
            char[] storePassword = Passphrase.generateRandomPassword( 32, Passphrase.SYSTEM_MODE);
            Persist.putEncrypt( ctx, CConst.SELFSIGNED_KS_KEY, storePassword);

            SecureRandom random = new SecureRandom();
            Provider bcProvider = new BouncyCastleProvider();
            Security.addProvider(bcProvider);

            // create keypair
            KeyPairGenerator keypairGen = KeyPairGenerator.getInstance("RSA");// RSA fails
            keypairGen.initialize(4096);
            KeyPair keypair = keypairGen.generateKeyPair();

            // yesterday
            Date validityBeginDate = new Date(System.currentTimeMillis() - 24L * 60 * 60 * 1000);
            // in 25 years
            Date validityEndDate = new Date(System.currentTimeMillis() + 25L * 365 * 24 * 60 * 60 * 1000);

            // fill in certificate fields
            X500Name subject = new X500NameBuilder(BCStyle.INSTANCE)
                    .addRDN(BCStyle.CN, "Nuvolect LLC")
                    .addRDN(BCStyle.O, "Nuvolect LLC")
                    .addRDN(BCStyle.OU, "Development")
                    .addRDN(BCStyle.C, "US")
                    .addRDN(BCStyle.L, "Orlando")
                    .addRDN(BCStyle.ST, "FL")
                    .build();
            byte[] id = new byte[20];
            random.nextBytes(id);
            BigInteger serial = new BigInteger(160, random);
            X509v3CertificateBuilder certificate = new JcaX509v3CertificateBuilder(
                    subject,
                    serial,
                    validityBeginDate,
                    validityEndDate,
                    subject,
                    keypair.getPublic());
            certificate.addExtension(Extension.subjectKeyIdentifier, false, id);
            certificate.addExtension(Extension.authorityKeyIdentifier, false, id);

            BasicConstraints constraints = new BasicConstraints(false);// cannot sign other certificates
            certificate.addExtension(
                    Extension.basicConstraints,
                    true,
                    constraints.getEncoded());
            KeyUsage usage = new KeyUsage(
                    KeyUsage.digitalSignature |
                            KeyUsage.nonRepudiation |
                            KeyUsage.keyCertSign |
                            KeyUsage.keyEncipherment |
                            KeyUsage.dataEncipherment
            );
            certificate.addExtension(Extension.keyUsage, false, usage.getEncoded());
            ExtendedKeyUsage usageEx = new ExtendedKeyUsage(new KeyPurposeId[] {
                    KeyPurposeId.id_kp_serverAuth,
                    KeyPurposeId.id_kp_clientAuth
            });
            certificate.addExtension(
                    Extension.extendedKeyUsage,
                    false,
                    usageEx.getEncoded());


            // build BouncyCastle certificate
            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .build(keypair.getPrivate());
            X509CertificateHolder holder = certificate.build(signer);

            // convert to JRE certificate
            JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
            converter.setProvider(new BouncyCastleProvider());
            X509Certificate x509Certificate = converter.getCertificate(holder);

            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load( null, storePassword);// Initialize it
            keyStore.setCertificateEntry("deepdive_cert", x509Certificate);

            X509Certificate[] chain = new X509Certificate[1];
            chain[0] = x509Certificate;
            KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(keypair.getPrivate(), chain);
            keyStore.setEntry("CA-Key", privateKeyEntry, new KeyStore.PasswordProtection(storePassword));

            FileOutputStream fos = new FileOutputStream( keystoreFile.getStdFile());
            keyStore.store( fos, storePassword);
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        } catch (OperatorCreationException e) {
            e.printStackTrace();
            success = false;
        } catch (CertificateException e) {
            e.printStackTrace();
            success = false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            success = false;
        } catch (KeyStoreException e) {
            e.printStackTrace();
            success = false;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            success = false;
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
            success = false;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            success = false;
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            success = false;
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }
}

