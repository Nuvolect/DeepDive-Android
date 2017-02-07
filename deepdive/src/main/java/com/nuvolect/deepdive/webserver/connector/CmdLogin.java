package com.nuvolect.deepdive.webserver.connector;//

import android.content.Context;

import com.nuvolect.deepdive.main.UserManager;
import com.nuvolect.deepdive.util.CConst;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.webserver.CrypServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * login
 *
 * Confirm login credentials
 */
public class CmdLogin {

    public static ByteArrayInputStream go(Context ctx, Map<String, String> params) {

        String password = params.get("password");
        String username = params.get("username");
        String uniqueId = params.get(CConst.UNIQUE_ID);

        try {
            JSONObject object = new JSONObject();

            boolean validatedUser = UserManager.getInstance(ctx).validateUser(username, password);

            if( validatedUser){

//                if( uniqueId.contentEquals(CConst.EMBEDDED_USER))
//                    CrypServer.setCookie( CConst.UNIQUE_ID, uniqueId, 7);
                CrypServer.setValidUser(ctx, uniqueId);
                LogUtil.log(LogUtil.LogType.CMD_LOGIN, "authenticate success: "+uniqueId);
            }
            else
                LogUtil.log(LogUtil.LogType.CMD_LOGIN, "authenticate FAIL: "+uniqueId);

            object.put("user_confirmed", validatedUser);

            return new ByteArrayInputStream(object.toString(2).getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
