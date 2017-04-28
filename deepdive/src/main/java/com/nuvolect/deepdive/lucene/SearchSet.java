package com.nuvolect.deepdive.lucene;//

import android.content.Context;

import com.nuvolect.deepdive.main.CConst;
import com.nuvolect.deepdive.util.FileUtil;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniUtil;
import com.nuvolect.deepdive.util.Persist;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Manage search sets
 */
public class SearchSet {

    static String SEARCH_SET_FOLDER_PATH = "/.search_set/";
    static String DEFAULT_SEARCH_SET_FILENAME = "default_search_set.json";
    static String DEFAULT_SET_PATH = SEARCH_SET_FOLDER_PATH + DEFAULT_SEARCH_SET_FILENAME;
    static String CURRENT_SET = "current_set";

    /**
     * Return the current set of sets in the following form:
     * wrapper
     *   setss:[
     *       {
     *         "id": 1,
     *         "name": "name_without_extension",
     *         "filename": "name_with_extension"
     *       },
     *       repeat
     *   ]
     *   success: t/f
     * @param ctx
     * @param volumeId
     * @return
     */
    public static JSONObject getSetss(Context ctx, String volumeId) {

        JSONObject wrapper = new JSONObject();

        OmniFile searchSetFolder = new OmniFile(volumeId, SEARCH_SET_FOLDER_PATH);
        try {
            boolean created = OmniUtil.forceMkdir(searchSetFolder);

            if( created){
                String assetFilePath = CConst.ASSET_DATA_FOLDER+DEFAULT_SEARCH_SET_FILENAME;
                OmniFile destinationFolder = new OmniFile( volumeId, DEFAULT_SET_PATH);
                OmniUtil.copyAsset( ctx, assetFilePath, destinationFolder);
            }
        } catch (IOException e) {
            LogUtil.logException(SearchSet.class, e);
        }

        JSONArray sets = new JSONArray();
        OmniFile[] searchFiles = searchSetFolder.listFiles();

        try {
            int i=0;

            for (OmniFile searchFile : searchFiles) {

                JSONObject o = new JSONObject();
                o.put("id",i++);
                o.put("filename", searchFile.getName());
                o.put("name", searchFile.getName().replace(".json",""));
                sets.put( o);
            }
            wrapper.put("sets", sets);
            wrapper.put("success", i>0);

        } catch (JSONException e) {
            LogUtil.logException( SearchSet.class, e);
        }

        return wrapper;
    }

    /**
     * Save the contents of a set object to the sets folder.
     * @param ctx
     * @param volumeId
     * @param set
     * @return
     */
    public static JSONObject putSet(Context ctx, String volumeId, String fileName, JSONArray set) {

        boolean success;
        try {
            OmniFile omniFile = new OmniFile( volumeId, SEARCH_SET_FOLDER_PATH +fileName);
            String content = set.toString();
            success = OmniUtil.writeFile( omniFile, content);
        } catch (Exception e) {
            LogUtil.logException(SearchSet.class, e);
            success = false;
        }
        JSONObject result = successResult( success);
        try {
            result.put("name",fileName);
        } catch (JSONException e) {
            LogUtil.logException(SearchSet.class, e);
        }

        return result;
    }

    /**
     * Return a JSONObject with a key "success".
     * @param success
     * @return
     */
    private static JSONObject successResult(boolean success) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("success", success);
        } catch (JSONException e) {
            LogUtil.logException(SearchSet.class, e);
        }
        return jsonObject;
    }

    /**
     * Delete a set.
     * @param ctx
     * @param volumeId
     * @param filename_to_delete
     * @return JSONObject with key "success"
     */
    public static JSONObject deleteSet(Context ctx, String volumeId, String filename_to_delete) {
        OmniFile omniFile = new OmniFile( volumeId, SEARCH_SET_FOLDER_PATH + filename_to_delete);
        boolean success = omniFile.delete();

        // Set the current set to default if the current set was just deleted
        String current_filename = Persist.get(ctx, CURRENT_SET, DEFAULT_SEARCH_SET_FILENAME);
        if( current_filename.contentEquals(filename_to_delete))
            Persist.put( ctx, CURRENT_SET, DEFAULT_SEARCH_SET_FILENAME);

        return successResult( success);
    }

    /**
     * Persist the filename of the current set. Use putSet to save the contents of the current set.
     * @param ctx
     * @param volumeId
     * @param set_filename
     * @return JSONObject with key "success"
     */
    public static JSONObject setCurrentSetFileName(Context ctx, String volumeId, String set_filename) {

        return successResult( Persist.put(ctx, CURRENT_SET, set_filename));
    }

    /**
     * Return an object including:
     * CURRENT_SET: array
     * CURRENT_SET_NAME: name of the set (filename without .json extension)
     * CURRENT_SET_FILE_NAME: file name of the set, including .json extension
     * @param ctx
     * @param volumeId
     * @return
     */
    public static JSONObject getSet(Context ctx, String volumeId, String setFileName) {

        JSONObject wrapper = new JSONObject();

        try{
            OmniFile setFile = new OmniFile( volumeId, SEARCH_SET_FOLDER_PATH + setFileName);

            if( ! setFile.exists()) {
                setFile = new OmniFile(volumeId, DEFAULT_SET_PATH);
                Persist.put( ctx, CURRENT_SET, DEFAULT_SEARCH_SET_FILENAME);
            }

            String content = FileUtil.readFile( ctx, setFile.getStdFile()).replace("\n","");
            JSONArray set = new JSONArray( content);
            wrapper.put("set", set);
            wrapper.put("filename", setFile.getName());
            wrapper.put("name", setFile.getName().replace(".json",""));

        } catch (JSONException e) {
            LogUtil.logException( SearchSet.class, e);
        }

        return wrapper;
    }
    public static JSONObject getCurrentSet(Context ctx, String volumeId) {

        String currentSetName = Persist.get(ctx, CURRENT_SET, DEFAULT_SEARCH_SET_FILENAME);
        JSONObject currentSet = getSet(ctx, volumeId, currentSetName);

        return currentSet;
    }
}
