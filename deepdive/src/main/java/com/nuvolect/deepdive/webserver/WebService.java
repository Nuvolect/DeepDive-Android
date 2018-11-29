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

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.concurrent.Semaphore;

import javax.crypto.NoSuchPaddingException;
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

    @Override
    public void onCreate() {
        super.onCreate();

        m_ctx = getApplicationContext();

        /**
         * Initialize web service command data
         */
        try {
            ServerInit.init( m_ctx);

        } catch (IOException | CertificateException  | NoSuchAlgorithmException  |
        InvalidKeyException  | UnrecoverableEntryException  | InvalidAlgorithmParameterException  |
        NoSuchPaddingException  | NoSuchProviderException  | KeyStoreException e){

            LogUtil.log(LogUtil.LogType.WEB_SERVICE,
                    "Server initialization exception, prepare for trouble!");
            LogUtil.logException(m_ctx, LogUtil.LogType.WEB_SERVICE, e);
        }

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

            // Create a self signed certificate and put it in a BKS keystore
            String keystoreFilename = "VazanKeystore.bks";

            File file = new File( m_ctx.getFilesDir(), keystoreFilename);
            String absolutePath = file.getAbsolutePath();

            KeystoreVazen.makeKeystore( m_ctx, absolutePath, false);

            sslServerSocketFactory = SSLUtil.configureSSLPath( m_ctx, absolutePath);

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
