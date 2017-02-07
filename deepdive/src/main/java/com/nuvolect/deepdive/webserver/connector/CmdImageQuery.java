package com.nuvolect.deepdive.webserver.connector;//

import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniImage;
import com.nuvolect.deepdive.main.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * image_query
 * <pre>
 *    Query the images of a volume
 * </pre>
 */
public class CmdImageQuery {

    private static boolean DEBUG = false; //LogUtil.DEBUG;
    private static String NO_IMAGE_FILENAME = "no_image_found.jpg";

    public static ByteArrayInputStream go(Map<String, String> params) {

        long startTime = System.currentTimeMillis();
        String httpIpPort = params.get("url");

        String volumeId = App.getUser().getDefaultVolumeId();
        if( params.containsKey("volume_id"))
            volumeId = params.get("volume_id");

        OmniFile targetFile = new OmniFile( volumeId, "/DCIM/Camera");
        OmniFile[] files = targetFile.listFiles();
        JSONArray list = new JSONArray();

        try {

            /**
             * Iterate over all the files looking for intersects.
             * When an intersect is found, remove it from consideration.
             */
            for(OmniFile file : files){

                if(OmniImage.isImage( file )){

                    JSONObject psObj = file.getPsObject(httpIpPort);
                    list.put(psObj);
                }
            }

            if( list.length() == 0){

                JSONObject image = new JSONObject();
                image.put( "name", NO_IMAGE_FILENAME);
                image.put("h", 270);
                image.put("w", 270);
                image.put("src", httpIpPort+"/img/"+NO_IMAGE_FILENAME);

                list.put(image);
            }

            JSONObject wrapper = new JSONObject();
            wrapper.put("list", list);

            if( DEBUG ){

                String msg = "Elapsed time: "+String.valueOf(System.currentTimeMillis()-startTime);
                LogUtil.log(LogUtil.LogType.CMD_IMAGE_QUERY, msg);

                LogUtil.log(LogUtil.LogType.CMD_IMAGE_QUERY, wrapper.toString(2));
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
