package com.nuvolect.deepdive.util;//

import android.app.Activity;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nuvolect.deepdive.webserver.WebService;
import com.nuvolect.deepdive.webserver.WebUtil;

//TODO create class description
//
public class MyWebViewClient extends WebViewClient {

    private final Activity m_act;

    public MyWebViewClient(Activity act) {

        m_act = act;
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//        super.onReceivedSslError(view, handler, error);

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
            LogUtil.log(LogUtil.LogType.MY_WEB_VIEW_CLIENT, "My Certificate  : "+WebService.CERTIFICATE_DETAILS);

            LogUtil.log(LogUtil.LogType.MY_WEB_VIEW_CLIENT, "Url: "+url);
        }

        if( url.startsWith(WebUtil.getServerUrl(m_act)))
            handler.proceed();
    }
}
