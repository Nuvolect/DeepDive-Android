/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;//

import android.app.Activity;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nuvolect.deepdive.webserver.WebUtil;

/**
 * Provide a class to receive SSL errors.
 */
public class MyWebViewClient extends WebViewClient {

    private final Activity m_act;

    public MyWebViewClient(Activity act) {

        m_act = act;
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

        /**
         * Only allow the certificate to function if the request originates from
         * this server IP and port number
         *
         * This is necessary for internal WebView usage to avoid the error:
         * I/X509Util: Failed to validate the certificate chain,
         *      error: java.security.cert.CertPathValidatorException:
         *      Trust anchor for certification path not found.
         */
        String url = error.getUrl();

        if( LogUtil.DEBUG){

            String certificate = error.getCertificate().toString();
            LogUtil.log(LogUtil.LogType.MY_WEB_VIEW_CLIENT, "SSL certificate : "+certificate);
            LogUtil.log(LogUtil.LogType.MY_WEB_VIEW_CLIENT, "Url: "+url);
        }

        if( url.startsWith(WebUtil.getServerUrl(m_act)))
            handler.proceed();
    }
}
