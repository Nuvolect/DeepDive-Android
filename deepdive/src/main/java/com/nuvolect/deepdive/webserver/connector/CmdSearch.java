package com.nuvolect.deepdive.webserver.connector;//

import com.nuvolect.deepdive.ddUtil.CConst;
import com.nuvolect.deepdive.ddUtil.LogUtil;
import com.nuvolect.deepdive.ddUtil.Omni;
import com.nuvolect.deepdive.ddUtil.OmniFile;
import com.nuvolect.deepdive.ddUtil.OmniUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;


/**
 * search
 *
 * Return a list of files and folders list, that match the search string. arguments:
 *
 * cmd : search
 * q : search string,
 * Response:
 *
 * files : (Array) array of objects - files and folders list,
 * that match the search string.
 *
 * GET '/servlet/connector' {_=1459337627027, q=rose, cmd=search, target=l0_L3N0b3JhZ2UvZW11bGF0ZWQvMA}
 */
public class CmdSearch {

    public static boolean DEBUG = LogUtil.DEBUG;

    public static InputStream go(Map<String, String> params) {

        String httpIpPort = params.get("url");
        /**
         * //TODO offer elFinder documentation
         * Target is one of:
         * 1. A hash for a directory or volume
         * 2. Empty: meaning to search all volumes
         */
        String target = params.get("target");

        String SearchStr = params.get("q");

        if( DEBUG )
            LogUtil.log(LogUtil.LogType.CMD_SEARCH, "Target: " + target+", search: "+SearchStr);

        /**
         * Files contains a list of search results
         */
        JSONArray files = new JSONArray();

        if( target.isEmpty()){

            for(String volumeId : Omni.getActiveVolumeIds()) {

                // Search all volumes
                OmniFile targetFile = new OmniFile( volumeId, CConst.ROOT);
                searchTarget( SearchStr, targetFile, files, httpIpPort);
            }
            
        }else {
            
            // Search a directory
            OmniFile targetFile = OmniUtil.getFileFromHash(target);
            searchTarget( SearchStr, targetFile, files, httpIpPort);
        }
        try {
            JSONObject wrapper = new JSONObject();
            wrapper.put("files", files);
            
            if( DEBUG )
                LogUtil.log(LogUtil.LogType.CMD_SEARCH, wrapper.toString(2));
                
            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Recursively search the target folder or volume for target files containing
     * the search string.
     * 
     * @param SearchStr
     * @param targetFile
     * @param files
     * @param httpIpPort
     */
    private static void searchTarget(
            String SearchStr, OmniFile targetFile, JSONArray files, String httpIpPort) {

        if( DEBUG )
            LogUtil.log(LogUtil.LogType.CMD_SEARCH, "Search target: "+targetFile.getPath());

        if( targetFile.getName().contains( SearchStr))
            files.put( targetFile.getFileObject( httpIpPort));
        
        for( OmniFile file : targetFile.listFiles()){
            
            if( file.isDirectory()){

                /**
                 * Recurse on each directory
                 */
                searchTarget( SearchStr, file, files, httpIpPort);
            }else{

                if( file.getName().contains( SearchStr))
                    files.put( file.getFileObject( httpIpPort));
            }
        }
    }
}
