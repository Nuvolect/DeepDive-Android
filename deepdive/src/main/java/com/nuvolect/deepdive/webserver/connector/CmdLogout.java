package com.nuvolect.deepdive.webserver.connector;//

import android.content.Context;

import com.nuvolect.deepdive.webserver.CrypServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * logout
 *
 * Clear authentication credentials and respond with user confirmation message.
 */
public class CmdLogout {

    public static ByteArrayInputStream go(Context ctx, Map<String, String> params) {

        try {
            JSONObject object = new JSONObject();

            if(CrypServer.clearCredentials(ctx))
                object.put("confirmation", "Logout confirmed");
            else
                object.put("confirmation", "Logout FAILED!");

            return new ByteArrayInputStream(object.toString(2).getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
