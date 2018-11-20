/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver;

import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DERSequence;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.Extension;
import org.spongycastle.asn1.x509.KeyPurposeId;
import org.spongycastle.asn1.x509.KeyUsage;
import org.spongycastle.asn1.x509.SubjectKeyIdentifier;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.cert.bc.BcX509ExtensionUtils;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.SSLSocket;

//
//TODO create class description
//
public class CertMrPublic {

    private String host;
    private int port;
    private byte[] nonce;
    private byte[] keyData;
    private SSLSocket server;
    private DataOutputStream writer;

    private static final String PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    private static final String SIGNATURE_ALGORITHM = "SHA512withECDSA";
    private static final String KEY_GENERATION_ALGORITHM = "ECDH";
    private static final String SSL_CONTEXT = "TLSv1.2";
    private static final String KEY_STORE_INSTANCE = "JKS";
    private static final String KMF_INSTANCE = "PKIX";
    private static final Date BEFORE = new Date(System.currentTimeMillis() - 5000);
    private static final Date AFTER = new Date(System.currentTimeMillis() + 600000);

    /**
     * Generates a one time use secure random number to be used as the password
     * for a keystore
     *
     * @return Returns void on completion
     */
    private void genNonce() {
        SecureRandom rand = new SecureRandom();
        this.nonce = new byte[2048];
        rand.nextBytes(nonce);
        return;
    }
    /**
     * Generates a one time use keystore for use with an SSL session
     *
     * @return Returns void on completion
     */
    private void genKeystore() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_GENERATION_ALGORITHM, PROVIDER_NAME);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            X509Certificate cert = createCACert(keyPair.getPublic(), keyPair.getPrivate());

            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null, (new String(this.nonce)).toCharArray());
            byte[] tempPass = new byte[2048];
            new SecureRandom().nextBytes(tempPass);
            ks.setKeyEntry("foo.bar", keyPair.getPrivate(), new String(tempPass).toCharArray(), new java.security.cert.Certificate[] { cert });
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ks.store(os, (new String(this.nonce)).toCharArray());
            this.keyData = os.toByteArray();
            //System.out.println("Client Key Data: " + new String(this.keyData));
            //System.out.println("Client Public Cert Key: " + cert.getPublicKey());
            //System.out.println("Client Public Key: " + keyPair.getPublic());
            //System.out.println("Client Private Key: " + keyPair.getPrivate());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    /**
     * Create a certificate to use by a Certificate Authority
     *
     * Retrieved from http://www.programcreek.com/java-api-examples/index.php?class=org.bouncycastle.cert.X509v3CertificateBuilder&method=addExtension
     *
     * @param publicKey Public key
     * @param privateKey Private key
     * @return Generated X509 Certificate
     */
    private X509Certificate createCACert(PublicKey publicKey, PrivateKey privateKey) throws Exception {
        X500Name issuerName = new X500Name("CN=127.0.0.1, O=FOO, L=BAR, ST=BAZ, C=QUX");

        X500Name subjectName = issuerName;

        BigInteger serial = BigInteger.valueOf(new SecureRandom().nextInt());

        X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(issuerName, serial, BEFORE, AFTER, subjectName, publicKey);
        builder.addExtension(Extension.subjectKeyIdentifier, false, createSubjectKeyIdentifier(publicKey));
        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));

        KeyUsage usage = new KeyUsage(KeyUsage.keyCertSign | KeyUsage.digitalSignature | KeyUsage.keyEncipherment | KeyUsage.dataEncipherment | KeyUsage.cRLSign);
        builder.addExtension(Extension.keyUsage, false, usage);

        ASN1EncodableVector purposes = new ASN1EncodableVector();
        purposes.add(KeyPurposeId.id_kp_serverAuth);
        purposes.add(KeyPurposeId.id_kp_clientAuth);
        purposes.add(KeyPurposeId.anyExtendedKeyUsage);
        builder.addExtension(Extension.extendedKeyUsage, false, new DERSequence(purposes));

        X509Certificate cert = signCertificate(builder, privateKey);
        cert.checkValidity(new Date());
        cert.verify(publicKey);

        return cert;
    }

    /**
     * Helper method
     *
     * Retrieved from http://www.programcreek.com/java-api-examples/index.php?api=org.bouncycastle.cert.bc.BcX509ExtensionUtils
     *
     * @param key
     * @return
     * @throws Exception
     */
    private static SubjectKeyIdentifier createSubjectKeyIdentifier(Key key) throws Exception {
        ASN1InputStream is = new ASN1InputStream(new ByteArrayInputStream(key.getEncoded()));
        ASN1Sequence seq = (ASN1Sequence) is.readObject();
        is.close();
        @SuppressWarnings("deprecation")
        SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(seq);
        return new BcX509ExtensionUtils().createSubjectKeyIdentifier(info);
    }

    /**
     * Helper method
     *
     * Retrieved from http://www.programcreek.com/java-api-examples/index.php?source_dir=mockserver-master/mockserver-core/src/main/java/org/mockserver/socket/KeyStoreFactory.java
     *
     * @param certificateBuilder
     * @param signedWithPrivateKey
     * @return
     * @throws Exception
     */
    private static X509Certificate signCertificate(X509v3CertificateBuilder certificateBuilder, PrivateKey signedWithPrivateKey) throws Exception {
        ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(PROVIDER_NAME).build(signedWithPrivateKey);
        return new JcaX509CertificateConverter().setProvider(PROVIDER_NAME).getCertificate(certificateBuilder.build(signer));
    }
}
