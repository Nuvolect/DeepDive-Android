package com.nuvolect.deepdive.main;

import android.app.Application;
import android.content.Context;

import java.util.HashMap;

/**
 * Provide an ability to get a context without
 * having to use an Activity or Service context.
 */
public class App extends Application {

    private static Context m_ctx;
    private static HashMap<String, User> m_userByName = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        m_ctx = this;
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
}
