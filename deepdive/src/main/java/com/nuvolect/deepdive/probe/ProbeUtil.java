package com.nuvolect.deepdive.probe;

import android.content.Context;

import com.nuvolect.deepdive.util.CConst;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.Omni;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniHash;
import com.nuvolect.deepdive.util.OmniUtil;
import com.nuvolect.deepdive.main.App;
import com.nuvolect.deepdive.webserver.connector.FileObj;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Manage user uploaded APK folders.
 */
public class ProbeUtil {

    /**
     * Create the application packag eand return an updated list of packages.
     * @param ctx
     * @param volumeId
     * @param folderPath
     * @return
     */
    public static JSONObject createPackage(Context ctx, String volumeId, String folderPath) {

        OmniFile folder = new OmniFile( volumeId, folderPath);

        boolean result = folder.mkdirs();
        String error = result?"":"mkdirs error";

        JSONObject list = getPackageList(ctx);
        list = appendError( list, error);

        return list;
    }

    /**
     * Delete the application package and return an updated list of packages.
     * @param ctx
     * @param volumeId
     * @param folderPath
     * @return
     */
    public static JSONObject deletePackage(Context ctx, String volumeId, String folderPath) {

        OmniFile folder = new OmniFile( volumeId, folderPath);
        int count = OmniUtil.deleteRecursive(ctx, folder);
        String error = count > 0 ? "":"Delete error";

        JSONObject list = getPackageList(ctx);
        list = appendError( list, error);

        return list;
    }

    /**
     * Return an object containing a list of folder objects. Each folder object
     * has a path (same as the name) and a URL for elFinder.
     * @param ctx
     * @return
     */
    public static JSONObject getPackageList(Context ctx) {

        String volumeId = App.getUser().getDefaultVolumeId();
        OmniFile apkFolder = new OmniFile( volumeId, CConst.USER_FOLDER_PATH);
        OmniFile[] packages = apkFolder.listFiles();
        JSONArray array = new JSONArray();

        for( OmniFile file : packages){

            if( ! file.getName().startsWith(".")) {// Ignore folders starting with '.'
                array.put( getAppListObject( ctx, file ));
            }
        }

        JSONObject wrapper = new JSONObject();
        try {
            wrapper.put("error", "");
            wrapper.put("list", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return wrapper;
    }

    private static JSONObject getAppListObject(Context ctx, OmniFile apkPackageFolder) {

        JSONObject object = new JSONObject();
        String name = apkPackageFolder.getName();
        try {
            object.put("name", name);
            object.put("url", OmniHash.getHashedServerUrl( ctx, Omni.userVolumeId, CConst.USER_FOLDER_PATH+ name));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * Append additional error to an object's error list.
     * When the additional error is empty, append nothing.
     * @param list
     * @param error
     * @return
     */
    private static JSONObject appendError(JSONObject list, String error) {

        try {
            String errorIn = list.getString("error");
            if( errorIn.isEmpty())
                return list;

            list.put("error", error +", "+errorIn);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * Convert a simple array of folder names to an array of objects
     * with each object contacting a folder name and a URL.
     * @param ctx
     * @param array
     * @return
     */
    private static JSONArray addFolderUrl(Context ctx, JSONArray array) {

        JSONArray fatList = new JSONArray();
        String slash = CConst.SLASH;
        try {

            for( int i = 0; i < array.length(); i++){

                JSONObject object = new JSONObject();
                object.put("name", array.get( i ));

                String url = OmniHash.getHashedServerUrl(
                    ctx, Omni.userVolumeId, slash+array.get( i ));
                object.put("url", url);
                fatList.put( object);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fatList;
    }

    /**
     * Requires spring MultipartFile
     * @param encodedPath
     * @return
     */
//    public static JSONObject uploadFile( String path, String fileName, MultipartFile multipartFile) {
//
//        JSONObject wrapper = new JSONObject();
//
//        String volumeId = App.getUser().getDefaultVolumeId();
//        String error = "";
//        String result = "";
//
//        path = ("/" + path + "/").replace("//","/");
//        new OmniFile( volumeId, path).mkdirs();
//        OmniFile file = new OmniFile( volumeId, path+fileName);
//        result = (file.delete()?"File deleted, ":"");
//
//        try {
//            multipartFile.transferTo( file.getStdFile());
//            result += "File upload successful: "+ file.getName();
//
//        } catch (IOException e) {
//            LogUtil.logException( ProbeUtil.class, e);
//            error = e.toString();
//        }
//        try {
//            long length = file.length();
//            String size = NumberFormat.getNumberInstance(Locale.US).format( length);
//            wrapper.put("size", size);
//            wrapper.put("name", file.getName());
//            wrapper.put("error", error);
//            wrapper.put("result", result);
//
//        } catch (JSONException e) {
//            LogUtil.logException( ProbeUtil.class, e);
//        }
//
//        return wrapper;
//    }

    public static JSONObject getInfo( String encodedPath) {

        JSONObject wrapper = new JSONObject();

        String error = "";

        OmniFile file = new OmniFile( encodedPath);
        JSONObject fileObj = FileObj.makeObj( file, "");

        try {
            wrapper.put("error", error);
            wrapper.put("file_object", fileObj);
        } catch (JSONException e) {
            LogUtil.logException( ProbeUtil.class, e);
        }

        return wrapper;
    }
}

