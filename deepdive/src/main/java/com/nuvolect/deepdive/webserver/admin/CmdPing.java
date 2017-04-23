package com.nuvolect.deepdive.webserver.admin;//

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * ping
 *
 * Respond with { timestamp: long}
 */
public class CmdPing {

    public static ByteArrayInputStream go(Map<String, String> params) {

        try {
            JSONObject object = new JSONObject();
            object.put("timestamp", System.currentTimeMillis());

            return new ByteArrayInputStream(object.toString(2).getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
