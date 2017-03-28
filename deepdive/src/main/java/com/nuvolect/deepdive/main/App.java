package com.nuvolect.deepdive.main;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.nuvolect.deepdive.R;
import com.nuvolect.deepdive.util.LogUtil;

import java.util.HashMap;

/**
 * Provide an ability to get a context without
 * having to use an Activity or Service context.
 */
public class App extends Application {

    private static Context m_ctx;
    public static String DEFAULT_IP_PORT = "0.0.0.0:0000";
    public static int DEFAULT_PORT = 0;

    private static HashMap<String, User> m_userByName = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        m_ctx = this;

        /**
         * Load build-dependent data into static variables that can be accessed without context.
         */
        LogUtil.setVerbose( Boolean.valueOf( m_ctx.getString(R.string.verbose_logging)));

        DEFAULT_IP_PORT = m_ctx.getString(R.string.default_ip_port);
        DEFAULT_PORT = Integer.valueOf(m_ctx.getString(R.string.default_port));
    }

    public static User getUser(){

        String userName = System.getProperty("user.name");

        if( m_userByName.containsKey( userName))
            return m_userByName.get( userName);
        else{

            User user = new User();
            m_userByName.put( userName, user);

            return user;
        }
    }

    public static Context getContext(){
        return m_ctx;
    }

    public static String fileSeparator() {

        return System.getProperty("file.separator");
    }


    public static boolean hasPermission(String perm) {
        return(ContextCompat.checkSelfPermission( m_ctx, perm)== PackageManager.PERMISSION_GRANTED);
    }
}
