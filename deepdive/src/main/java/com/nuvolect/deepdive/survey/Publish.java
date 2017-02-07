package com.nuvolect.deepdive.survey;//

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import com.nuvolect.deepdive.ddUtil.Analytics;
import com.nuvolect.deepdive.ddUtil.AnalyticsThrottle;
import com.nuvolect.deepdive.ddUtil.CConst;
import com.nuvolect.deepdive.ddUtil.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Singleton class that publishes survey results.  Specific survey elements are published only
 * one time each 7 days to time-normalize the data, i.e., a user that runs the app frequently will
 * not dominate the overall survey data but will publish one time in 7 days.
 */
public class Publish {
    private static Publish singleton = null;
    private static Context m_ctx;
    private JSONObject m_pubDb = null;
    private static final String PUB_DB       = "pub_db";
    private static final String PERSIST_NAME = "pub_persist";

    private Publish(){

        try {
            /**
             * Load publish db via a persisted string and convert it to a JSON object.
             */
            m_pubDb = new JSONObject( getPubDb(m_ctx));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Publish getInstance(Context ctx){

        m_ctx = ctx;

        if( singleton == null){
            singleton = new Publish();
        }
        return singleton;
    }

    public void surveySave(){

        putPubDb(m_ctx, m_pubDb.toString());
    }

    /**
     * Publish an item to analytics if it has not been published already in the last 7 days.
     * @param jsonString
     * @param category
     * @param packageName
     * @return true - published, false - too recent to publish
     */
    public int publishCond7Days(String jsonString, String category, String packageName) {

        /**
         * Package name is unique and is the key
         */
        if( m_pubDb.has( packageName)){

            long lastPubTime = 0L;
            try {
                lastPubTime = JsonUtil.getLong(packageName, m_pubDb);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if( lastPubTime + CConst.WEEK_MS > System.currentTimeMillis()){
                return 0;
            }
        }
        try {
            m_pubDb.put( packageName, System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AnalyticsThrottle.getInstance(m_ctx).send(
                category,
                jsonString,
                Analytics.COUNT, 1);

        return 1;
    }

    /**
     * Publish a total and app metrics no more frequently than once in each 7 days.
     * @param publishTotal
     * @return true when published, false when within 7 days and no publish
     */
    public boolean publishCondTotal7Days(int publishTotal) {

        String UNIQUE_TOTAL_KEY = "uniqueAppTotalKey";
        /**
         * Reference the publish db, total key is unique
         */
        if( m_pubDb.has( UNIQUE_TOTAL_KEY)){

            long lastPubTime = 0L;
            try {
                lastPubTime = JsonUtil.getLong( UNIQUE_TOTAL_KEY, m_pubDb);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Check if app posted a total within the last week
            if( lastPubTime + CConst.WEEK_MS > System.currentTimeMillis()){
                return false;
            }
        }
        // App is going to publish, record the current time
        try {
            m_pubDb.put( UNIQUE_TOTAL_KEY, System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int appVersion = 0;
        try {
            appVersion = m_ctx.getPackageManager().getPackageInfo(
                    m_ctx.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        AnalyticsThrottle.getInstance(m_ctx).send(
                Analytics.SURVEY_TOTAL,
                "appVer:" + appVersion + ", sdk:" + Build.VERSION.SDK_INT,
                Analytics.COUNT, publishTotal);

        return true;
    }

    public static String getPubDb(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getString(PUB_DB, "{}");
    }

    public static void putPubDb(Context ctx, String jsonString) {

        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PUB_DB, jsonString).commit();
    }
}
