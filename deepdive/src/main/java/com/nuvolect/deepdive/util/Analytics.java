/*******************************************************************************
 * Copyright (c) 2011 - 2014, Nuvolect LLC. All Rights Reserved.
 * All intellectual property rights, including without limitation to
 * copyright and trademark of this work and its derivative works are
 * the property of, or are licensed to, Nuvolect LLC.
 * Any unauthorized use is strictly prohibited.
 ******************************************************************************/
package com.nuvolect.deepdive.util;

import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.nuvolect.deepdive.R;


public class Analytics {

    static EasyTracker easyTracker;

    public static final String CLOSE_TIPS          = "close_tips";
    public static final String COUNT               = "count";
    public static final String DECOMPILE           = "decompile";
    public static final String DEVICE              = "device";
    public static final String HELP                = "help";
    public static final String HIDE_TIPS           = "hide_tips" ;
    public static final String MAIN_MENU           = "main_menu";
    public static final String NEXT_TIP            = "next_tip";
    public static final String OMNI_REST           = "omni_rest";
    public static final String PREVIOUS_TIP        = "previous_tip";
    public static final String REFRESH             = "refresh";
    public static final String SETTINGS            = "settings";
    public static final String SEARCH              = "search";
    public static final String SEARCH_REST         = "search_rest";
    public static final String SEARCH_SET          = "search_set";
    public static final String SHOW_TIP            = "show_tip";
    public static final String UP_BUTTON_EXIT      = "up_button_exit";
    public static final String FINDER              = "finder";

    /**
     * Generate Google Analytics for events
     *
     * @param category - String
     * @param action - String
     * @param label - String
     * @param value - Long
     */
    public static void send(Context ctx, String category,
                            String action, String label, long value) {

        try {
            if( easyTracker == null ){

                easyTracker = EasyTracker.getInstance(ctx);
                LogUtil.log(LogUtil.LogType.ANALYTICS, "Analytics.send, restored from NULL");
            }

            // Post an "event" for next day analysis
            easyTracker.send(MapBuilder.createEvent(category, action, label, value).build());

        } catch (Exception e) {
            LogUtil.log(LogUtil.LogType.ANALYTICS, "exception in Analytics.send");
            LogUtil.logException( ctx, LogUtil.LogType.ANALYTICS, e);
        }
    }

    /**
     * Publish Google Analytics activity state, i.e., screen
     * @param activity
     */
    public static void start(Activity activity) {

        easyTracker = EasyTracker.getInstance(activity);
        easyTracker.activityStart(activity);
    }

    /**
     * Publish Google Analytics activity state, i.e., screen
     * @param activity
     */
    public static void stop(Activity activity) {

        easyTracker.activityStop(activity);
    }

    public static void sendMenuItem(Context ctx, String category, MenuItem item) {

        String action = "unknown";

        switch (item.getItemId()){

            case android.R.id.home:
                action = "home";
                break;
            case R.id.menu_refresh:
                action = "menu_refresh";
                break;
            case R.id.menu_show_tips:
                action = "menu_show_tips";
                break;
            case R.id.menu_help:
                action = "menu_help";
                break;
            case R.id.menu_app_upgrade:
                action = "menu_whats_new";
                break;
            case R.id.menu_roadmap:
                action = "menu_roadmap";
                break;
            case R.id.menu_issues:
                action = "menu_issues";
                break;
            case R.id.menu_developer_feedback:
                action = "menu_developer_feedback";
                break;
            default:
        }
        send(ctx, category, action, "label", 1L);
    }

    /**
     * Send a specific screen manually to analytics. This is used when the app is serving
     * web screens from the embedded server. It avoids confusing web page analytics with
     * mobile app analytics.
     *
     * @param ctx
     * @param screen
     */
    public static void sendScreen(Context ctx, String screen, String data) {

        try {
            if( easyTracker == null ){

                easyTracker = EasyTracker.getInstance(ctx);
                LogUtil.log(LogUtil.LogType.ANALYTICS, "Analytics.sendScreen, restored from NULL");
            }

            send(ctx, "screen", screen, data, 1L);

            // Post a "screen" for next day analysis
            easyTracker.set(Fields.SCREEN_NAME, screen);

            easyTracker.send( MapBuilder
                    .createAppView()
                    .build()
            );

        } catch (Exception e) {
            LogUtil.log(LogUtil.LogType.ANALYTICS, "exception in Analytics.sendScreen");
            LogUtil.logException( ctx, LogUtil.LogType.ANALYTICS, e);
        }
    }

    public static void sendScreenSelect(Context ctx, String uri, String data) {

        if( uri.startsWith("/apps")){
           sendScreen( ctx, "apps.htm", data);
        }
        else if( uri.startsWith("/app.")){
            sendScreen( ctx, "app.htm", data);
        }
        else if( uri.startsWith("/decompile.")){
            sendScreen( ctx, "decompile.htm", data);
        }
        else if( uri.startsWith("/device.")){
            sendScreen( ctx, "device.htm", data);
        }
        else if( uri.startsWith("/keystore.")){
            sendScreen( ctx, "keystore.htm", data);
        }
        else if( uri.startsWith("/lobby.")){
            sendScreen( ctx, "lobby.htm", data);
        }
        else if( uri.startsWith("/logcat.")){
            sendScreen( ctx, "logcat.htm", data);
        }
        else if( uri.startsWith("/login.")){
            sendScreen( ctx, "login.htm", data);
        }
        else if( uri.startsWith("/logout.")){
            sendScreen( ctx, "logout.htm", data);
        }
        else if( uri.startsWith("/search.")){
            sendScreen( ctx, "search.htm", data);
        }
        else if( uri.startsWith("/search_manager.")){
            sendScreen( ctx, "search_manager.htm", data);
        }
        else if( uri.startsWith("/search_set.")){
            sendScreen( ctx, "search_set.htm", data);
        }
        else if( uri.startsWith("/shell.")){
            sendScreen( ctx, "shell.htm", data);
        }
        else if( uri.startsWith("/view_text.")){
            sendScreen( ctx, "view_text.htm", data);
        }
    }
}
