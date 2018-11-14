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
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.webserver.connector.ServerInit;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.security.KeyStore;
import java.util.concurrent.Semaphore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


/**
 * Long running Service that operates the LAN server.
 */
public class WebService extends Service {


    private static Context m_ctx;
    private Handler mHandler;
    public static String HTTP_PROTOCOL = "https://";
    public static String CERTIFICATE_DETAILS = "Issued to: CN=Nuvolect LLC,OU=Development,O=Nuvolect LLC,L=Orlando,ST=FL,C=US;";

    private static SSLServerSocketFactory sslServerSocketFactory;
    private static SSLSocketFactory sslSocketFactory;
    private static SSLContext sslContext;
    private static OkHttpClient okHttpClient = null;

    private static String keyFile = "/assets/keystore.bks";
    private static char[] passPhrase = "27@NDMQu0cLY".toCharArray();// TODO update passphrase

    @Override
    public void onCreate() {
        super.onCreate();

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

            configureSSL(keyFile, passPhrase);

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

    /**
     * Return the IP in 4 number 3 dot format, or null if unable to get host address.
     * @param context
     * @return
     */
    protected String wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e("WIFIIP", "Unable to get host address.");
            ipAddressString = null;
        }

        return ipAddressString;
    }

    /**
     * Creates an SSLSocketFactory for HTTPS.
     *
     * Pass a KeyStore resource with your certificate and passphrase
     */
    public static void configureSSL(String keyAndTrustStoreClasspathPath, char[] passphrase) throws IOException {

        try {
            // Android does not have the default jks but uses bks
            KeyStore keystore = KeyStore.getInstance("BKS");
            InputStream keystoreStream = WebService.class.getResourceAsStream(keyAndTrustStoreClasspathPath);
            keystore.load(keystoreStream, passphrase);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keystore);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, passphrase);
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            sslServerSocketFactory = sslContext.getServerSocketFactory();
            sslSocketFactory = sslContext.getSocketFactory();

        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
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
