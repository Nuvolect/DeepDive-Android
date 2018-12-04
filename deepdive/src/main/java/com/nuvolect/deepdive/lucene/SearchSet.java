/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.lucene;

import android.content.Context;

import com.nuvolect.deepdive.main.CConst;
import com.nuvolect.deepdive.util.Analytics;
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
 * Manage search sets. Default sets are copied from Assets to the user
 * folder and can be edited or deleted. New sets can be added. The
 * system keeps track of the most recent set used.
 */
public class SearchSet {//NEXTSPRINT expand wiki search set docs to include basic sets
    //NEXTSPRINT edit certificates to find files with specific file extensions.

    static String SEARCH_SET_FOLDER_PATH = "/.search_set/";
    static String DEFAULT_SEARCH_SET_FILENAME = "default_search_set.json";
    static String DEFAULT_SET_PATH = SEARCH_SET_FOLDER_PATH + DEFAULT_SEARCH_SET_FILENAME;
    static String CURRENT_SET = "current_set";

    static String[] m_search_sets = new String[]{
            "analytics.json",
            "certificates.json",
            "default_search_set.json",
            "HummingWhale.json",
            "jakhar.aseem.diva.json",
    };

    /**
     * Get the current set of sets and if called for the first time, create a folder in the
     * user area and copy the default sets into it.
     *
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
            OmniUtil.forceMkdir(searchSetFolder);

            // Cycle through the default search sets and if they do not exists, copy each to user area
            for( String fileName : m_search_sets){

                String assetFilePath = CConst.ASSET_DATA_FOLDER+fileName;
                OmniFile destinationFile = new OmniFile( volumeId, SEARCH_SET_FOLDER_PATH+fileName);
                if( ! destinationFile.exists()){

                    OmniUtil.copyAsset( ctx, assetFilePath, destinationFile);
                    LogUtil.log(LogUtil.LogType.SEARCH_SET,"" +
                            "Default search set added: "+destinationFile.getName());
                }
            }
        } catch (IOException e) {
            LogUtil.logException(LogUtil.LogType.SEARCH_SET, e);
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

            // Remove excess data that will automatically get rebuilt on next use.
            for( int i = 0 ; i < set.length(); i++){

                JSONObject obj = set.getJSONObject( i );

                if( obj.has("num_hits")){
                    obj.remove("num_hits");
                }
                if( obj.has("encodedQuery")){
                    obj.remove("encodedQuery");
                }
                set.put( i, obj);
            }
            String content = set.toString(2);
            success = OmniUtil.writeFile( omniFile, content);
        } catch (Exception e) {
            LogUtil.logException(SearchSet.class, e);
            success = false;
        }
        String category = Analytics.SEARCH_SET;
        String action = fileName;
        String label = set.toString();
        long value = set.length();

        Analytics.send( ctx, category, action, label, value);

//            LogUtil.log(Search.class, "cat: "+category+", act: "+action+", lab: "+label+", hits: "+value);

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
    public static String getCurrentSetName(Context ctx, String volumeId){

        return Persist.get(ctx, CURRENT_SET, DEFAULT_SEARCH_SET_FILENAME);
    }
}
