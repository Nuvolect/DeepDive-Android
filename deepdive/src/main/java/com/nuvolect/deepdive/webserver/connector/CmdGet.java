/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver.connector;//

import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * get
 *
 * Returns the content of a plain/text file (preview)
 *
 * Arguments:
 *
 * cmd : read
 * current : hash of the directory where the file is stored
 * target : hash of the file
 * Response:
 *
 * content - text file contents (raw string)
 *
 * {
 * "content": "Hello world!" // (String) contents of the text file
 * }
 *
 * RESTFul example:
 * GET '/servlet/connector'
 * { * cmd=get, conv=1, target=c0_L2NyeXAgZm9sZGVyL3RzIHRlc3QudHh0, * _=1459292135837, }
 */
public class CmdGet {

    public static ByteArrayInputStream go(Map<String, String> params) {

        String target="";
        if (params.containsKey("target"))
            target = params.get("target");

        OmniFile targetFile = new OmniFile( target );

        String content = targetFile.readFile();
        String error = "";

        JSONObject wrapper = new JSONObject();
        try {

            wrapper.put("content", content);

            LogUtil.log(LogUtil.LogType.CMD_GET, targetFile.getName()+" fetched");

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            error = "UnsupportedEncodingException error";
        } catch (JSONException e) {
            e.printStackTrace();
            error = "JSONException error";
        }
        try {
            return new ByteArrayInputStream(error.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
