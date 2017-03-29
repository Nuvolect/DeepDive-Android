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
