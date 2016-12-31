package com.nuvolect.deepdive.webserver;
//
//TODO create class description
//

import android.content.Context;

import com.nuvolect.deepdive.ddUtil.LogUtil;
import com.nuvolect.deepdive.survey.DeviceSurvey;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

public class DeviceRest {

    private enum CMD_ID {
        NIL,
        logcat,
        shell,
        info,
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
        }

        JSONObject wrapper = new JSONObject();

        try {
            switch ( cmd_id){

                case NIL:
                    break;
                case logcat: {
                    JSONArray logcat = DeviceSurvey.getLogCat();
                    wrapper.put("logcat", logcat.toString());
                    break;
                }
                case shell:{
                    String shell_cmd = params.get("shell_cmd");
                    JSONArray shell_log = DeviceSurvey.getShell( shell_cmd );
                    wrapper.put("shell_log", shell_log.toString());
                    break;
                }
                case info:{
                    JSONObject device = DeviceSurvey.getInfo(ctx);
                    wrapper.put("device", device.toString());
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
            e.printStackTrace();
        }

        return null;
    }
}
