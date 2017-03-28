/*
 * Copyright (c) 2017. Nuvolect LLC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Contact legal@nuvolect.com for a less restrictive commercial license if you would like to use the
 * software without the GPLv3 restrictions.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
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
