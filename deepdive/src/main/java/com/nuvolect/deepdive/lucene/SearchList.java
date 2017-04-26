package com.nuvolect.deepdive.lucene;//

import android.content.Context;

import com.nuvolect.deepdive.util.FileUtil;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniUtil;
import com.nuvolect.deepdive.util.Persist;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Manage search lists
 */
public class SearchList {//SPRINT rename SearchSet

    static String SEARCH_LIST_FOLDER_PATH = "/.search_list/";
    static String DEFAULT_SEARCH_LIST_FILENAME = "default_search_list.json";
    static String DEFAULT_LIST_PATH = SEARCH_LIST_FOLDER_PATH + DEFAULT_SEARCH_LIST_FILENAME;
    static String CURRENT_LIST = "current_list";

    /**
     * Return the current set of lists in the following form:
     * wrapper
     *   lists:[
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
    public static JSONObject getLists(Context ctx, String volumeId) {

        JSONObject wrapper = new JSONObject();

        OmniFile searchListFolder = new OmniFile(volumeId, SEARCH_LIST_FOLDER_PATH);
        try {
            boolean created = OmniUtil.forceMkdir(searchListFolder);

            if( created){
                InputStream inputStream = ctx.getAssets().open(DEFAULT_SEARCH_LIST_FILENAME);
                OutputStream outputStream = new OmniFile( volumeId, DEFAULT_LIST_PATH).getOutputStream();
                IOUtils.copy( inputStream, outputStream);
                inputStream.close();
                outputStream.close();
            }
        } catch (IOException e) {
            LogUtil.logException(SearchList.class, e);
        }

        JSONArray lists = new JSONArray();
        OmniFile[] searchFiles = searchListFolder.listFiles();

        try {
            int i=0;

            for (OmniFile searchFile : searchFiles) {

                JSONObject o = new JSONObject();
                o.put("id",i++);
                o.put("filename", searchFile.getName());
                o.put("name", searchFile.getName().replace(".json",""));
                lists.put( o);
            }
            wrapper.put("lists", lists);
            wrapper.put("success", i>0);

        } catch (JSONException e) {
            LogUtil.logException( SearchList.class, e);
        }

        return wrapper;
    }

    /**
     * Save the contents of a list object to the lists folder.
     * @param ctx
     * @param volumeId
     * @param list
     * @return
     */
    public static JSONObject putList(Context ctx, String volumeId, String fileName, JSONArray list) {

        boolean success;
        try {
            OmniFile omniFile = new OmniFile( volumeId, SEARCH_LIST_FOLDER_PATH+fileName);
            String content = list.toString();
            success = OmniUtil.writeFile( omniFile, content);
        } catch (Exception e) {
            LogUtil.logException(SearchList.class, e);
            success = false;
        }
        JSONObject result = successResult( success);
        try {
            result.put("name",fileName);
        } catch (JSONException e) {
            LogUtil.logException(SearchList.class, e);
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
            LogUtil.logException(SearchList.class, e);
        }
        return jsonObject;
    }

    /**
     * Delete a list.
     * @param ctx
     * @param volumeId
     * @param filename_to_delete
     * @return JSONObject with key "success"
     */
    public static JSONObject deleteList(Context ctx, String volumeId, String filename_to_delete) {
        OmniFile omniFile = new OmniFile( volumeId, SEARCH_LIST_FOLDER_PATH+ filename_to_delete);
        boolean success = omniFile.delete();

        // Set the current list to default if the current list was just deleted
        String current_filename = Persist.get(ctx, CURRENT_LIST, DEFAULT_SEARCH_LIST_FILENAME);
        if( current_filename.contentEquals(filename_to_delete))
            Persist.put( ctx, CURRENT_LIST, DEFAULT_SEARCH_LIST_FILENAME);

        return successResult( success);
    }

    /**
     * Persist the filename of the current list. Use putList to save the contents of the current list.
     * @param ctx
     * @param volumeId
     * @param list_filename
     * @return JSONObject with key "success"
     */
    public static JSONObject setCurrentListFileName(Context ctx, String volumeId, String list_filename) {

        return successResult( Persist.put(ctx, CURRENT_LIST, list_filename));
    }

    /**
     * Return an object including:
     * CURRENT_LIST: array
     * CURRENT_LIST_NAME: name of the list (filename without .json extension)
     * CURRENT_LIST_FILE_NAME: file name of the list, including .json extension
     * @param ctx
     * @param volumeId
     * @return
     */
    public static JSONObject getList(Context ctx, String volumeId, String listFileName) {

        JSONObject wrapper = new JSONObject();

        try{
            OmniFile listFile = new OmniFile( volumeId, SEARCH_LIST_FOLDER_PATH + listFileName);

            if( ! listFile.exists()) {
                listFile = new OmniFile(volumeId, DEFAULT_LIST_PATH);
                Persist.put( ctx, CURRENT_LIST, DEFAULT_SEARCH_LIST_FILENAME);
            }

            String content = FileUtil.readFile( ctx, listFile.getStdFile()).replace("\n","");
            JSONArray list = new JSONArray( content);
            wrapper.put("list", list);
            wrapper.put("filename", listFile.getName());
            wrapper.put("name", listFile.getName().replace(".json",""));

        } catch (JSONException e) {
            LogUtil.logException( SearchList.class, e);
        }

        return wrapper;
    }
    public static JSONObject getCurrentList(Context ctx, String volumeId) {

        String currentListName = Persist.get(ctx, CURRENT_LIST, DEFAULT_SEARCH_LIST_FILENAME);
        JSONObject currentList = getList(ctx, volumeId, currentListName);

        return currentList;
    }
}
