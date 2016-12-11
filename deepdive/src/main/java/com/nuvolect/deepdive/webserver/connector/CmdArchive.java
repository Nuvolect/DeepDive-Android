package com.nuvolect.deepdive.webserver.connector;//

import android.content.Context;

import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniZip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

/**
 * archive

 Packs directories / files into an archive.

 Arguments:

 cmd : archive
 type : mime-type for the archive
 current : hash of the directory that are added to the archive directory / files
 targets : an array of hashes of the directory / files
 Response: Client-Server-API-2.1#open, select - hash of the new archive

 Example:
 http://hypweb.net/elFinder-nightly/demo/2.1/php/connector.minimal.php?
   cmd=archive
   &name=Archive.zip
   &target=l2_TmV3Rm9sZGVyL3Rlc3Q
   &targets%5B%5D=l2_TmV3Rm9sZGVyL3Rlc3QvZW1wdHkgZm9sZGVy
   &targets%5B%5D=l2_TmV3Rm9sZGVyL3Rlc3QvY29sb3Vyc29mZmFsbC5qcGc
   &targets%5B%5D=l2_TmV3Rm9sZGVyL3Rlc3Qv0YLQtdGB0YIuanBn
   &type=application%2Fzip
   &_=1460671443521
 */
public class CmdArchive {

    public static InputStream go(Context ctx, Map<String, String> params) {

        String httpIpPort = params.get("url");
        /**
         * Create a file for the target archive.
         * Assume the file does not already exist.
         * "target" is the folder containing the zip
         * "name" is the name of the zip file
         */
        OmniFile parentOmniFile = new OmniFile(params.get("target"));
        String volumeId = parentOmniFile.getVolumeId();
        String zipPath = parentOmniFile.getPath()+"/"+params.get("name");
        OmniFile zipOmniFile = new OmniFile( volumeId, zipPath);

        ArrayList<String> targetsArrayList = new ArrayList<>();

        /**
         * Params only has the first element of the targets[] array.
         * This is fine if there is only one target but an issue for multiple file operations.
         * Manually parse the query parameter strings to get all targets.
         */
        String[] qps = params.get("queryParameterStrings").split("&");

        for(String candidate : qps){

            if( candidate.contains("targets")){
                String[] parts = candidate.split("=");
                targetsArrayList.add( parts[1]);
            }
        }

        OmniFile[] targets = new OmniFile[ targetsArrayList.size()];
        for( int i = 0; i < targetsArrayList.size(); i++){

            targets[i] = new OmniFile( targetsArrayList.get( i ));
        }

        boolean success = OmniZip.zipFiles(ctx, targets, zipOmniFile, 0);

        try {
            JSONArray added = new JSONArray();
            added.put( zipOmniFile.getFileObject(httpIpPort));

            JSONObject wrapper = new JSONObject();
            wrapper.put("added", added);

            if( ! success){

                JSONArray warning = new JSONArray();
                warning.put("errPerm");
                wrapper.put("warning", warning);
            }

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
