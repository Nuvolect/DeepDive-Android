/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver.connector;//

import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.Omni;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

//TODO create class description
//
public class CmdRename {

    public static InputStream go(Map<String, String> params) {

        String target = "";// Target is a hashed volume and path
        if( params.containsKey("target"))
            target = params.get("target");

        OmniFile targetFile = OmniUtil.getFileFromHash(target);
        LogUtil.log(LogUtil.LogType.CMD_RENAME, "Target " + targetFile.getPath());

        String name = "";
        if( params.containsKey("name"))
            name = params.get("name");

        String volumeId = Omni.getVolumeId(target);
        String newPath = targetFile.getParentFile().getPath()+"/"+name;
        OmniFile newFile = new OmniFile(volumeId, newPath);

        boolean success = targetFile.renameFile( newFile );

        JSONArray added = new JSONArray();
        JSONArray removed = new JSONArray();
        JSONObject wrapper = new JSONObject();

        try {

            if( success ){
                added.put( newFile);
                removed.put( targetFile);
            }
            wrapper.put("added", added);
            wrapper.put("removed", removed);

            return new ByteArrayInputStream(wrapper.toString(2).getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
