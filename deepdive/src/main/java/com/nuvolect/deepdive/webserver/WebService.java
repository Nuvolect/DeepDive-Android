/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver;//

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.webserver.connector.ServerInit;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;


/**
 * Long running Service that operates the LAN server.
 */
public class WebService extends Service {


    private static Context m_ctx;
    private Handler mHandler;
    public static String HTTP_PROTOCOL = "https://";
    public static String CERTIFICATE_DETAILS = "CN=Nuvolect LLC,OU=Development,O=Nuvolect LLC,L=Orlando,ST=FL,C=US;";

    private static SSLServerSocketFactory sslServerSocketFactory;
    private static SSLSocketFactory sslSocketFactory;
    private static OkHttpClient okHttpClient = null;

    private static String keyFile = "/assets/keystore.bks";
    private static char[] passPhrase = "27@NDMQu0cLY".toCharArray();//SPRINT remove static passPhrase

    @Override
    public void onCreate() {
        super.onCreate();
//SPRINT cleanup test code, create a SecurityCertificates.md file with extensive notes
        m_ctx = getApplicationContext();

        /**
         * Initialize web service command data
         */
        ServerInit.init( m_ctx);

        WebServiceThread looper = new WebServiceThread();
        looper.start();
        try {
            looper.ready.acquire();
        } catch (InterruptedException e) {
            LogUtil.log(LogUtil.LogType.WEB_SERVICE,
                    "Interrupted during wait for the CommServiceThread to start, prepare for trouble!");
            LogUtil.logException(m_ctx, LogUtil.LogType.WEB_SERVICE, e);
        }

        CrypServer server = new CrypServer(m_ctx, WebUtil.getPort(m_ctx));

        try {
            okHttpClient = null;

            // Copy the mac made certificate to private_0
//            OmniUtil.copyAsset(m_ctx, "keystore.bks", new OmniFile( Omni.userVolumeId_0,"keystore.bks"));

            // Create a self signed certificate and put it in a BKS keystore
            String VazanFilename = "VazanKeystore.bks";

            KeystoreVazen.makeKeystore( m_ctx, VazanFilename, false);
//
//            if( LogUtil.DEBUG){
//
//                //SPRINT why do SSL comms fail with release Build Variant
//                SSLUtil.probeCert( VazanFilename, passPhrase);
//                SSLUtil.probeCert( "keystore.bks", passPhrase);
//            }

//            String s = "the quick brown fox jumped over the lazy dog";
//            try {
//                byte[] cipherBytes = KeystoreUtil.encrypt(m_ctx, CConst.APP_KEY_ALIAS, s.getBytes());
//                byte[] clearBytes = KeystoreUtil.decrypt(CConst.APP_KEY_ALIAS, cipherBytes);
//                String result = new String( clearBytes);
//                LogUtil.log("done");
//            } catch (KeyStoreException e) {
//                e.printStackTrace();
//            } catch (CertificateException e) {
//                e.printStackTrace();
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            } catch (UnrecoverableEntryException e) {
//                e.printStackTrace();
//            } catch (NoSuchPaddingException e) {
//                e.printStackTrace();
//            } catch (InvalidKeyException e) {
//                e.printStackTrace();
//            } catch (NoSuchProviderException e) {
//                e.printStackTrace();
//            } catch (InvalidAlgorithmParameterException e) {
//                e.printStackTrace();
//            }

//            char[] chars = Passphrase.generateRandomPassword( 10000, Passphrase.SYSTEM_MODE);
//            int lenChars = chars.length;
//            byte[] bytes = Passphrase.toBytes( chars );
//            int lenBytes = bytes.length;
//            char[] backToChars = Passphrase.toChars( bytes);
//            int lenChars2 = backToChars.length;
//            String str2 = new String( backToChars);
//            int lenStr2 = str2.length();
//            boolean sameStr = str2.contentEquals( new String(chars));

//            try {
//                char[] chars = Passphrase.generateRandomPassword( 32, Passphrase.SYSTEM_MODE);
//                String s = new String( chars);
//                Persist.putEncrypt(m_ctx, "testkey", s.toCharArray());
//                char[] clearResult = Persist.getDecrypt( m_ctx, "testkey");
//                String result = new String( clearResult);
//                boolean strMatch = result.contentEquals( s );
//                LogUtil.log("done");
//            } catch (CertificateException e) {
//                e.printStackTrace();
//            } catch (InvalidKeyException e) {
//                e.printStackTrace();
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            } catch (KeyStoreException e) {
//                e.printStackTrace();
//            } catch (NoSuchPaddingException e) {
//                e.printStackTrace();
//            } catch (UnrecoverableEntryException e) {
//                e.printStackTrace();
//            } catch (NoSuchProviderException e) {
//                e.printStackTrace();
//            } catch (InvalidAlgorithmParameterException e) {
//                e.printStackTrace();
//            }
            sslServerSocketFactory = SSLUtil.configureSSLPath( m_ctx, VazanFilename);

            // This one loads a working certificate from assets
//            sslServerSocketFactory = SSLUtil.configureSSLAsset( keyFile, passPhrase);
            // This one loads a working certificate from path
//            sslServerSocketFactory = SSLUtil.configureSSLPath( "keystore.bks", passPhrase);

            if( HTTP_PROTOCOL.startsWith("https"))
                server.makeSecure( sslServerSocketFactory, null);
            server.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtil.log(LogUtil.LogType.WEB_SERVICE, "Server started: " + WebUtil.getServerUrl(m_ctx));
    }

    public static Context getContext(){

        return m_ctx;
    }

    private class WebServiceThread extends Thread {
        public Semaphore ready = new Semaphore(0);

        WebServiceThread() {
            this.setName("webServiceThread");
        }

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    _handleMessage(msg);
                }
            };
            ready.release(); // Signal the looper and handler are created
            Looper.loop();
        }
    }

    private void _handleMessage(Message msg) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.log(LogUtil.LogType.WEB_SERVICE, "onDestroy()");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.log(LogUtil.LogType.WEB_SERVICE, "onBind()");
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        LogUtil.log(LogUtil.LogType.WEB_SERVICE, "onReBind()");
    }

    public static OkHttpClient getOkHttpClient() {

        if( okHttpClient == null) {

            okHttpClient = new OkHttpClient();
            okHttpClient.setHostnameVerifier(WebUtil.NullHostNameVerifier.getInstance());
            okHttpClient.setSslSocketFactory(sslSocketFactory);
        }
        return okHttpClient;
    }
}
