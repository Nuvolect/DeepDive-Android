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
import java.util.Map;

/**
 *
 * parents
 *
 *         Returns all parent folders and its subfolders on required (in connector options) deep.
 *         This command is invoked when a directory is reloaded in the client.
 *         Data provided by 'parents' command should enable the correct drawing of tree hierarchy
 *         directories.
 *
 *         Arguments:
 *
 *         cmd : parents
 *         target : folder's hash,
 *         Response:
 *
 *         tree : (Array) Folders list. Information about File/Directory
 *         Example:
 *
 *         With the present hierarchy
 *
 *         /root1
 *           /dir1
 *             /dir11
 *               /dir111
 *             /dir12
 *               /dir121
 *           /dir2
 *             /dir22
 *             /dir23
 *               /dir231
 *         /root2
 *
 *         Should 'dir111' be reloaded, 'parents' data should return:
 *         'dir111' parent directories
 *         for each parent directory, its subdirectories (no more depth is needed)
 *         should multiroot nodes be implemented, its root nodes (and optionally children)
 *         This way, client-side component will render the following reloaded hierarchy
 *
 *         /root1
 *           /dir1
 *             /dir11
 *               /dir111
 *             /dir12
 *           /dir2
 *         /root2
 */
public class CmdParents {

    private static boolean DEBUG = false; //LogUtil.DEBUG;

    public static ByteArrayInputStream go(Map<String, String> params) {

        String httpIpPort = params.get("url");

        String target = params.get("target");
        OmniFile targetFile = new OmniFile( target );
        String volumeId = targetFile.getVolumeId();

        if( DEBUG )
            LogUtil.log(LogUtil.LogType.CMD_PARENTS, "volumeId: "+volumeId+", path: "+targetFile.getPath());

        try {
            JSONArray treeArray = new JSONArray();

            /**
             * Iterate upward and capture all parent and parent sibling (aunts and uncles) directories
             */

             while( targetFile != null && ! targetFile.isRoot()){

                targetFile = targetFile.getParentFile();
                if( targetFile == null)
                    break;
                if( DEBUG )
                    LogUtil.log(LogUtil.LogType.CMD_PARENTS, "folder scan:  "+targetFile.getPath());

                for( OmniFile file: targetFile.listFiles()){

                    if( file.isDirectory()){

                        treeArray.put(FileObj.makeObj(volumeId, file, httpIpPort));
                        if( DEBUG )
                            LogUtil.log(LogUtil.LogType.CMD_PARENTS, "tree put: "+file.getPath());
                    }
                }
            }

            JSONObject tree = new JSONObject();
            tree.put("tree", treeArray);

            if( DEBUG)
                LogUtil.log(LogUtil.LogType.CMD_PARENTS, tree.toString(2));

            return new ByteArrayInputStream(tree.toString().getBytes("UTF-8"));

        } catch (JSONException | UnsupportedEncodingException e) {
            LogUtil.logException(LogUtil.LogType.CMD_PARENTS, e);
        }

        return null;
    }
}
