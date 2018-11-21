/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver;

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

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

//
//TODO create class description
//
public class CertGen2 {

    public static void selfSignedCert() throws NoSuchAlgorithmException {

        try {
            SecureRandom random = new SecureRandom();

// create keypair
            KeyPairGenerator keypairGen = KeyPairGenerator.getInstance("RSA");
            keypairGen.initialize(256, random);
            KeyPair keypair = keypairGen.generateKeyPair();

            // yesterday
            Date validityBeginDate = new Date(System.currentTimeMillis() - 24L * 60 * 60 * 1000);
            // in 20 years

            Date validityEndDate = new Date(System.currentTimeMillis() + 20L * 365 * 24 * 60 * 60 * 1000);

// fill in certificate fields
            X500Name subject = new X500NameBuilder(BCStyle.INSTANCE)
                    .addRDN(BCStyle.CN, "stackoverflow.com")
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
            BasicConstraints constraints = new BasicConstraints(true);
            certificate.addExtension(
                    Extension.basicConstraints,
                    true,
                    constraints.getEncoded());
            KeyUsage usage = new KeyUsage(KeyUsage.keyCertSign | KeyUsage.digitalSignature);
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
            X509Certificate x509 = converter.getCertificate(holder);

// serialize in DER format
            byte[] serialized = x509.getEncoded();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OperatorCreationException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } finally {
        }
    }
}