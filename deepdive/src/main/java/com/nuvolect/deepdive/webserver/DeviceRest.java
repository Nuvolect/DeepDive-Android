/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver;

import android.content.Context;

import com.nuvolect.deepdive.survey.DeviceSurvey;
import com.nuvolect.deepdive.survey.SurveyExec;
import com.nuvolect.deepdive.util.KeystoreUtil;
import com.nuvolect.deepdive.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Handle RESTful services relating to the device.
 */
public class DeviceRest {

    private enum CMD_ID {
        NIL,
        app_detail,
        info,
        keystore,
        logcat,
        shell,
    }
    public static InputStream process(Context ctx, Map<String, String> params) {

        long timeStart = System.currentTimeMillis();
        CMD_ID cmd_id = CMD_ID.NIL;
        String error = "";

        try {
            String uri = params.get("uri");
            String segments[] = uri.split("/");
            cmd_id = CMD_ID.valueOf( segments[2]);
        } catch (IllegalArgumentException e) {
            error = "Error, invalid command: "+params.get("cmd");
            LogUtil.logException(DeviceRest.class, e);
        }

        JSONObject wrapper = new JSONObject();
        String extra = "";

        try {
            switch ( cmd_id){

                case NIL:
                    break;
                case app_detail:{
                    String package_name = params.get("package_name");
                    extra = package_name;
                    JSONObject app_detail = SurveyExec.getAppDetail( ctx, package_name);
                    wrapper.put("app_detail", app_detail.toString());
                    break;
                }
                case info:{
                    JSONObject device = DeviceSurvey.getInfo(ctx);
                    wrapper.put("device", device.toString());
                    break;
                }
                case keystore:{
                    JSONObject result = KeystoreUtil.dispatch(ctx, params);
                    wrapper.put("result", result);
                    break;
                }
                case logcat: {
                    JSONArray logcat = DeviceSurvey.getLogCat();
                    wrapper.put("logcat", logcat.toString());
                    break;
                }
                case shell:{
                    String shell_cmd = params.get("shell_cmd");
                    extra = shell_cmd;
                    JSONArray shell_log = DeviceSurvey.getShell( shell_cmd );
                    wrapper.put("shell_log", shell_log.toString());
                    break;
                }
            }
            if( ! error.isEmpty())
                LogUtil.log( DeviceRest.class, "Error: "+error);

            wrapper.put("error", error);
            wrapper.put("cmd_id", cmd_id.toString());
            wrapper.put("delta_time", String.valueOf(System.currentTimeMillis() - timeStart) + " ms");

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch (Exception e) {
            LogUtil.logException(DeviceRest.class, e);
        }

        return null;
    }
}
