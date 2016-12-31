package com.nuvolect.deepdive.main;//

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.nuvolect.deepdive.ddUtil.ActionBarUtil;
import com.nuvolect.deepdive.ddUtil.CConst;
import com.nuvolect.deepdive.ddUtil.LogUtil;
import com.nuvolect.deepdive.ddUtil.MyWebViewClient;
import com.nuvolect.deepdive.ddUtil.MyWebViewFragment;
import com.nuvolect.deepdive.webserver.NanoHTTPD;

import java.util.HashMap;
import java.util.Map;

public class WebFragment extends MyWebViewFragment {

    private static final String KEY_FILE="file";

    public static WebFragment newInstance(String file){

        WebFragment f = new WebFragment();

        Bundle args = new Bundle();

        args.putString(KEY_FILE, file);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBarUtil.hide(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View result=super.onCreateView(inflater, container, savedInstanceState);

        WebView webView = getWebView();
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);

        webView.setWebViewClient(new MyWebViewClient(getActivity()));
        webView.setWebChromeClient(new WebChromeClient());
        webView.clearCache( true );
        webView.clearHistory();

        clearCookies(getActivity());

        CookieManager.getInstance().setAcceptThirdPartyCookies( webView, true );
        CookieManager.getInstance().flush();

        String cookie = "unique_id="+ CConst.EMBEDDED_USER
                +" ; path=/;"
                +" expires="+ NanoHTTPD.Cookie.getHTTPTime( 7 )+";";
//                +" secure; ";
//                    +";domain="+ WebUtil.getServerIp(getActivity()));

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Cookie", cookie);
        webView.loadUrl(getPage(), headers);

//        webView.loadUrl(getPage());

        return(result);
    }

      private String getPage() {
        return(getArguments().getString(KEY_FILE));
    }

    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context)
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            LogUtil.log(LogUtil.LogType.WEB_FRAGMENT, "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else
        {
            LogUtil.log(LogUtil.LogType.WEB_FRAGMENT, "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }
}
