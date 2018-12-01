# Keystore and Security Certificate

DeepDive self-generates a self-signed certificate using a password that is locally encrypted 
by the Android Keystore. Only the DeepDive Android app can decrypt the password and only the 
decrypted password can activate the self-signed certificate. The encrypted password is stored
in the app private data area (it can be stored anywhere ) and the self-signed certificate 
is password protected. 

Given the app now has a secure security certificate, https communications can be used to download 
any additional certificates, passwords, etc., from a server, if necessary but generally, 
this will not be necessary.

## Invalid Certificate Error
This solution is good, and communications are secure but your web browser does not know this and 
cannot confirm this. This is why you will see warnings and/or errors relating to an 
invalid certificate. The web browser inspects the certificate and attempts to confirm its validity. 
It does this by matching the embedded domain name and/or static IP address. Since DeepDive is 
hosted on Android, it does not have a domain name and it uses a dynamic IP address assigned by the 
associated WiFi router.

## Diagnostic code removed from WebService.java

For testing, a keystore.bks file can be loaded into Assets. This code will copy the file from 
Assets to the root that can be access from Finder.

```
// Copy the mac made certificate to private_0
OmniUtil.copyAsset(m_ctx, "keystore.bks", new OmniFile( Omni.userVolumeId_0,"keystore.bks"));
```

DeepDive creates a self-signed certificate in private storage. To create the certificate in 
public storage reachable by Finder, this approach works.
```
OmniFile keystoreFile = new OmniFile("u0", keystoreFilename);
String absolutePath = keystoreFile.getAbsolutePath();
KeystoreVazen.makeKeystore( m_ctx, absolutePath, true);
```
By default, the self-signed certificate is self-generated in private storage

```
// Create a self signed certificate and put it in a BKS keystore
String keystoreFilename = "VazanKeystore.bks";

File file = new File( m_ctx.getFilesDir(), keystoreFilename);
String absolutePath = file.getAbsolutePath();
KeystoreVazen.makeKeystore( m_ctx, absolutePath, false);
sslServerSocketFactory = SSLUtil.configureSSLPath( m_ctx, absolutePath);
```

It can also be loaded from assets

```
// Load a working certificate from assets
sslServerSocketFactory = SSLUtil.configureSSLAsset( keyFile, passPhrase);
```

## Testing Tools
Several methods in SSLUtil are useful for probing existing keystore and certificates
See SSLUtil.probeCert(). an dSSLUtil.probeKeystore().

