/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver;//

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.nuvolect.deepdive.license.AppSpecific;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.webserver.connector.ServerInit;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.util.concurrent.Semaphore;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import androidx.core.app.NotificationCompat;


/**
 * Long running Service that operates the LAN server.
 */
public class WebService extends Service {


    private static Context m_ctx;
    private Handler mHandler;
    public static String HTTP_PROTOCOL = "https://";

    private static SSLServerSocketFactory sslServerSocketFactory;
    private static SSLSocketFactory sslSocketFactory;
    private static OkHttpClient okHttpClient = null;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1,new Notification());

        LogUtil.log(LogUtil.LogType.WEB_SERVICE, "Starting web service");
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

            // Create a self signed certificate and put it in a BKS keystore
            String keystoreFilename = "SelfSigned.bks";

            File file = new File( m_ctx.getFilesDir(), keystoreFilename);
            String absolutePath = file.getAbsolutePath();

            SelfSignedCertificate.makeKeystore( m_ctx, absolutePath, true);

            sslServerSocketFactory = SSLUtil.configureSSLPath( m_ctx, absolutePath);

            if( HTTP_PROTOCOL.startsWith("https"))
                server.makeSecure( sslServerSocketFactory, null);
            server.start();

        } catch (Exception e) {
            LogUtil.logException(m_ctx, LogUtil.LogType.WEB_SERVICE, e);
        }
        LogUtil.log(LogUtil.LogType.WEB_SERVICE, "Server started: " + WebUtil.getServerUrl(m_ctx));
    }

    private void startMyOwnForeground(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            String NOTIFICATION_CHANNEL_ID = "com.nuvolect.deepdive";
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(AppSpecific.SMALL_ICON)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);
        }
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
