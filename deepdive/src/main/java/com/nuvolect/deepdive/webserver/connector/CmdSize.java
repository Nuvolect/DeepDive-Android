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
 * size

 Returns the size of a directory or file.

 Arguments:

 cmd : size
 targets[] : hash paths of the nodes
 Response:

 size: The total size for all the supplied targets.
 */
public class CmdSize {

    public static ByteArrayInputStream go(Map<String, String> params) {

        String[] targets = new String[100];

        String relativePath = "";
        String volumeId = "";

        if( params.containsKey("targets[]"))
            targets[0] = params.get("targets[]");
        else if( params.containsKey("targets[0]"))
            targets[0] = params.get("targets[0]");

        OmniFile targetFile = new OmniFile( targets[0]);

        JSONObject size = new JSONObject();

        long sizeBytes = calcSize(targetFile);
        LogUtil.log(LogUtil.LogType.SIZE, "Target " + relativePath+", size: "+sizeBytes);

        try {

            size.put("size", sizeBytes);
            return new ByteArrayInputStream(size.toString().getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static long calcSize(OmniFile targetFile) {

        if (targetFile == null) return 0;
        if (targetFile.isFile()) return targetFile.length();
        if (!targetFile.isDirectory()) return targetFile.length();

        long size = 0;

        OmniFile[] tmp = targetFile.listFiles();
        if ( tmp != null ) {
            for (OmniFile file : targetFile.listFiles()) { // NPE gone
                if( file == null) continue;
                if (file.isFile())
                    size += file.length();
                else
                    size += calcSize(file);
            }
        }
        return size;
    }
}
