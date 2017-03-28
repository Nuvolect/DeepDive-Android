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
