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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

/**
 * <pre>
 * Return a list of file names in the target directory.
 *
 * Arguments:
 *
 * cmd : ls
 * target : hash of directory,
 * intersect[] : list of files to match
 *
 * Response:
 * list : (Array) file names list. Return only duplicate files if the intersect[] is specified.
 *
 * Example:
 * cmd=ls&target=l2_Lw&intersect%5B%5D=Very+Nice.txt&_=1459218951937
 * Example2:
 {
 intersect[]=splash.jpg,
 queryParameterStrings=cmd=ls
 &target=l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC90bXA
 &intersect%5B%5D=publiclink.jpg
 &intersect%5B%5D=signup.jpg
 &intersect%5B%5D=splash.jpg
 &_=1459457554754,
 _=1459457554754,
 cmd=ls,
 target=l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC90bXA
 }
 * </pre>
 */
public class CmdLs {

    public static ByteArrayInputStream go(Map<String, String> params) {

        String httpIpPort = params.get("url");
        String target = params.get("target");
        ArrayList<String> intersects = new ArrayList<>();

        /**
         * Params only has the first element of the targets[] array.
         * This is fine if there is only one target but an issue for multiple file operations.
         * Manually parse the query parameter strings to get all targets.
         */
        String[] qps = params.get("queryParameterStrings").split("&");

        for(String candidate : qps){

            if( candidate.contains("intersect")){
                String[] parts = candidate.split("=");
                intersects.add( parts[1]);
            }
        }

        OmniFile targetFile = new OmniFile( target);
        String volumeId = targetFile.getVolumeId();
        OmniFile[] files = targetFile.listFiles();
        JSONArray list = new JSONArray();
        String hit;

        try {

            if( intersects.isEmpty()){
                /**
                 * Build a list of all files in the target folder.
                 */
                for(OmniFile file : files){

                    list.put( FileObj.makeObj(volumeId, file, httpIpPort));
                }
            }else{

                /**
                 * Build a list of intersect files that exist in the target folder.
                 *
                 * Iterate over all the files looking for intersects.
                 * When an intersect is found, remove it from consideration.
                 */
                for(OmniFile file : files){

                    hit = "";

                    for(String intersect : intersects){

                        if( file.getName().contentEquals( intersect )){

                            list.put( FileObj.makeObj(volumeId, file, httpIpPort));
                            LogUtil.log(LogUtil.LogType.CMD_LS, "File hit: " + intersect);
                            hit = intersect;
                        }
                    }
                    // Remove it from the list to speed the search
                    if( ! hit.isEmpty())
                        intersects.remove(hit);
                    // Quit early when all intersects are satisfied
                    if( intersects.isEmpty())
                        break;
                }

            }
            if( LogUtil.DEBUG){

                LogUtil.log(LogUtil.LogType.CMD_LS, "list: " + list.toString(2));
                if( !intersects.isEmpty() && list.length() == 0)
                    LogUtil.log(LogUtil.LogType.CMD_LS, "File MISS: " + intersects.get(0));
            }
            JSONObject wrapper = new JSONObject();
            wrapper.put("list", list);

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch (JSONException e) {
            LogUtil.logException( CmdLs.class, e);
        } catch (UnsupportedEncodingException e) {
            LogUtil.logException( CmdLs.class, e);
        }

        return null;
    }
}
