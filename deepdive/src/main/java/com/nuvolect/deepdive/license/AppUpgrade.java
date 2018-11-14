/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.license;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nuvolect.deepdive.R;
import com.nuvolect.deepdive.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

// Check app version on background thread and present results to UI
public class AppUpgrade {

    private static AppUpgrade singleton = null;
    private static Activity m_act;
    private static View m_view;
    private Dialog m_dialog = null;

    public static AppUpgrade getInstance(Activity act){

        m_act = act;

        if( singleton == null){
            singleton = new AppUpgrade();
        }
        return singleton;
    }

    private AppUpgrade() {
    }

    public void showDialog(){

        m_dialog = new Dialog(m_act);

        LayoutInflater myInflater = (LayoutInflater) m_act.getSystemService(m_act.LAYOUT_INFLATER_SERVICE);
        View view = myInflater.inflate(R.layout.app_upgrade, null);

        m_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_dialog.setContentView(view);
        m_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setButtonOnClicks(view); // Configure onClick callbacks for each button

        m_dialog.show();

        /**
         * Set the view to find fields from the Dialog
         */
        m_view = view;

        new CheckAppUpdate().execute();
    }

    private void setButtonOnClicks(View view){

        view.findViewById(R.id.closeAppUpgradeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayout ll = (LinearLayout) m_view.findViewById(R.id.appUpgradeLl);
                ll.setVisibility(View.GONE);

                if( m_dialog != null && m_dialog.isShowing()){

                    m_dialog.dismiss();
                    m_dialog = null;
                }
            }
        });
    }

    private class CheckAppUpdate extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            JSONArray jsonArray = new JSONArray();
            String result = "No update available. You are running the current version.";
            String communicationError = "Error, cannot reach server.";

            try {
                jsonArray = JsonReader.readJsonFromUrl("https://nuvolect.com/deepdive/output.json");
                if (updateAvaliable(jsonArray)) {

                    result = getVersionMessage( jsonArray );
                }
            } catch (IOException e) {
                result = communicationError;
            } catch (JSONException e) {
                result = communicationError;
            } catch ( Exception e){
                result = communicationError;
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            TextView tv = (TextView) m_view.findViewById(R.id.upgradeDialogTv);
            tv.setText(Html.fromHtml( result, new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    Drawable drawFromPath;
                    int path = m_act.getResources().getIdentifier(source, "drawable", m_act.getPackageName());
                    drawFromPath = (Drawable) m_act.getResources().getDrawable(path);
                    drawFromPath.setBounds(0, 0, drawFromPath.getIntrinsicWidth(),
                            drawFromPath.getIntrinsicHeight());

                    return drawFromPath;
                }
            }, null));

            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    /** output.json, produced by Android Studio
     * [
     *   {
     *     "outputType": {
     *       "type": "APK"
     *     },
     *     "apkInfo": {
     *       "type": "MAIN",
     *       "splits": [
     *
     *       ],
     *       "versionCode": 9401,
     *       "versionName": "0.9.4",
     *       "enabled": true,
     *       "outputFile": "deepdive-release.apk",
     *       "fullName": "release",
     *       "baseName": "release"
     *     },
     *     "path": "deepdive-release.apk",
     *     "properties": {
     *
     *     }
     *   }
     * ]
     * @param jsonArray
     * @return
     * @throws JSONException
     */

    private boolean updateAvaliable(JSONArray jsonArray) throws JSONException {

        JSONObject json = jsonArray.getJSONObject(0).getJSONObject("apkInfo");

        long versionAval  = json.getInt( "versionCode");

        int versionRunning = 0;
        try {
            PackageInfo pInfo = m_act.getPackageManager().getPackageInfo(m_act.getPackageName(), 0);
            versionRunning = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e1) { }

        return versionAval > versionRunning;
    }

    private String getVersionMessage(JSONArray jsonArray) throws JSONException {

        JSONObject json = jsonArray.getJSONObject(0).getJSONObject("apkInfo");
        String versionName = json.getString( "versionName");

        String message = json.getString("outputFile")
                + " Version "+versionName+" is available."+
                "<br><br>"+
                "<a href='https://nuvolect.com/deepdive/deepdive-release.apk'>Download DeepDive</a>";
        return message;
    }
}
