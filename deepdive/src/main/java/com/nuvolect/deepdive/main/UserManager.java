/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.main;//

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

import com.nuvolect.deepdive.R;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.Persist;
import com.nuvolect.deepdive.util.Safe;
import com.nuvolect.deepdive.webserver.Comm;
import com.nuvolect.deepdive.webserver.WebUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Manage users.  Initially manage a single user, although hooks are
 * in place to support multiple users.
 */
public class UserManager {

    private static UserManager singleton = null;
    private static Context m_ctx;
    private JSONArray users = new JSONArray();
    private JSONArray emptyArray = new JSONArray();

    public static UserManager getInstance(Context ctx) {

        m_ctx = ctx;

        if( singleton == null){
            singleton = new UserManager();
        }
        return singleton;
    }

    private UserManager() {

        try {
            String userString = Persist.getUsers( m_ctx, emptyArray.toString());
            users = new JSONArray( userString );

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean validateUser(String username, String password){

        for(int i = 0; i < users.length(); i++){

            try {
                JSONObject user = users.getJSONObject(i);
                if(
                        username.contentEquals( user.getString("username")) &&
                                password.contentEquals( user.getString("password")))

                    return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Get a summary list of users for display in the settings fragment.
     * @return
     */
    public String getSummary() {

        String summary = "";
        String delimiter = "";
        for(int i = 0; i < users.length(); i++){

            try {
                JSONObject user = users.getJSONObject(i);
                summary += delimiter + user.getString("username");
                delimiter = ", ";

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return summary.isEmpty()?"Select to add web app user":summary;
    }

    /**
     * Save username and password to encrypted storage.
     * Save an empty user list in the case that username and password are empty.
     *
     * @param username
     * @param password
     */
    private void updateUser(String username, String password){

        users = new JSONArray();
        if( ! (username.isEmpty() && password.isEmpty())){

            JSONObject user = new JSONObject();
            try {
                user.put("username", username);
                user.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            users.put(user);
        }
        Persist.putUsers( m_ctx, users.toString());
    }

    public Dialog showDialog(Activity act){

        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        // Get the layout inflater
        LayoutInflater inflater = act.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.user_manager_single_user, null);
        final EditText usernameEt = (EditText) view.findViewById(R.id.usernameEt);
        final EditText passwordEt = (EditText) view.findViewById(R.id.passwordEt);

        if( users.length() > 0){

            try {
                JSONObject user = users.getJSONObject(0);
                usernameEt.setText( user.getString( "username"));
                passwordEt.setText(user.getString("password"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        builder.setView( view )
                // Add action buttons
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        String username = Safe.safeString(usernameEt.getText().toString().trim());
                        String password = Safe.safeString(passwordEt.getText().toString().trim());

                        updateUser(username, password);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });
        AlertDialog myDialog = builder.create();
        myDialog.show();

        return  myDialog;
    }

    public boolean isWideOpen() {

        return users.length() == 0;
    }

    /**
     * Log the user into the server if the user has set authentication credentials.
     */
    public void authenticateEmbeddedUser() {

        if( isWideOpen())
            return;

        String thisDeviceUrl = WebUtil.getServerUrl(m_ctx)+"/admin?cmd=login";
        Map<String, String> params = new HashMap<String, String>();
        try {
            JSONObject user = users.getJSONObject(0);
            params.put("username", user.getString("username"));
            params.put("password", user.getString("password"));
            params.put(CConst.UNIQUE_ID, CConst.EMBEDDED_USER);
            params.put("cmd", "login");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Comm.sendPostUi(m_ctx, thisDeviceUrl, params, new Comm.CommPostCallbacks() {
            @Override
            public void success(String jsonObject) {
                LogUtil.log(LogUtil.LogType.USER_MANAGER, "User authenticated");
            }

            @Override
            public void fail(String error) {

                LogUtil.log(LogUtil.LogType.USER_MANAGER, "User authenticate ERROR");
            }
        });
    }

    /**
     * The internal user may require authentication to use the the web server.
     * Internal users already have an entry passphrase/keypad or YubiKey for entry,
     * are automatically authenticated and do not use the login page.
     * External users go through a login.htm page if the system is not wide open.
     */
    public void validateWebUser(Activity act){

        String url = this.getLoginUrl();
        String postData = this.getPostData();

        WebView webView = new WebView(act);
        webView.postUrl(url, postData.getBytes());

        LogUtil.log(LogUtil.LogType.USER_MANAGER, "user validated");
    }


    private String getLoginUrl() {

        return WebUtil.getServerUrl(m_ctx)+"/admin?cmd=login";
    }

    private String getPostData() {

        String postData = "cmd=login&username=";
        try {
            JSONObject user = users.getJSONObject(0);
            postData += user.getString("username")+"&password=" + user.getString("password");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postData;
    }
}
