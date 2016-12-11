package com.nuvolect.deepdive.webserver.connector;//

import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

//TODO create class description
//
public class CmdMkdir {

    public static InputStream go(Map<String, String> params) {

        String target = "";// Target is a hashed volume and path
        if( params.containsKey("target"))
            target = params.get("target");

        String httpIpPort = params.get("url");

        OmniFile targetFile = VolUtil.getFileFromHash(target);
        LogUtil.log(LogUtil.LogType.CMD_MKDIR, "Target " + targetFile.getPath());

        String name = "";
        if( params.containsKey("name"))
            name = params.get("name");

        String volumeId = VolUtil.getVolumeId(target);
        String path = targetFile.getPath();

        OmniFile file = new OmniFile(volumeId, path+"/"+name);
        boolean success = file.mkdir();

        JSONArray added = new JSONArray();
        JSONObject wrapper = new JSONObject();

        try {

            if( success ) {
                JSONObject newDir = FileObj.makeObj(volumeId, file, httpIpPort);
                added.put(newDir);
            }
            wrapper.put("added", added);

            return new ByteArrayInputStream(wrapper.toString(2).getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
