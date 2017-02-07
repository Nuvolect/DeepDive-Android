package com.nuvolect.deepdive.lucene;

import android.content.Context;

import com.nuvolect.deepdive.ddUtil.CConst;
import com.nuvolect.deepdive.ddUtil.LogUtil;
import com.nuvolect.deepdive.ddUtil.OmniFile;
import com.nuvolect.deepdive.ddUtil.OmniFileFilter;
import com.nuvolect.deepdive.ddUtil.OmniHash;
import com.nuvolect.deepdive.ddUtil.OmniUtil;
import com.nuvolect.deepdive.main.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.nuvolect.deepdive.webserver.MimeUtil.TEXT_FILE_EXTENSION;

//import static com.nuvolect.deepdive.webserver.MimeUtil.TEXT_FILE_EXTENSION;

public class IndexUtil {

    public static final String INDEX_FOLDER = "/.lucene/";
    /**
     * Return a list of current Lucene search indexes and purge any indexes for
     * folders that no longer exist.
     * Currently search index are a list of objects and each object has a relative path.
     * This design is limited to operating with a single volume.
     *
     * //TODO test on multiple volumes
     * Return a list of objects with a relative path and a hashed path.
     * The hashed path contains volume information.
     * @return
     */
    public static JSONArray getCurrentIndexes( String volumeId) {

        OmniFile luceneFolder = new OmniFile(volumeId, INDEX_FOLDER);
        try {
            OmniUtil.forceMkdir(luceneFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray searchPaths = new JSONArray();

        OmniFile[] hashedDirs = luceneFolder.listFiles();
        ArrayList<OmniFile> hashedDirsToDelete = new ArrayList<>();

        /**
         * Each physical lucene folder should match a persisted lucene folder.
         * Iterate over the physical folders and delete any persisted folders that no longer exist.
         */
        for (OmniFile hashedDir : hashedDirs) {

            if (hashedDir.isDirectory()) {

                String hashedDirPath = hashedDir.getName();
                LogUtil.log("hashedDirPath: " + hashedDirPath);

                String hash = hashedDirPath.substring(3, hashedDirPath.length());
                String path = OmniHash.decode(hash);

                OmniFile omniFile = new OmniFile(volumeId, path);
                LogUtil.log("omniFile.getName: " + omniFile.getName());
                LogUtil.log("omniFile.getAbsolutePath: " + omniFile.getAbsolutePath());

                if (omniFile.exists()) {

                    // put on list as an active searchable path
                    JSONObject jsonObject = new JSONObject();
                    try {
                        if (path.startsWith("/"))
                            jsonObject.put("path", path);
                        else
                            jsonObject.put("path", "/" + path);
                    } catch (JSONException e) {
                    }

                    searchPaths.put(jsonObject);

                } else {

                    // Folder no longer exists, save to delete below
                    hashedDirsToDelete.add(hashedDir);
                }
            }
        }

        /**
         * Delete any index folders that no longer needed
         * FIXME confirm hash dir list needs to be maintained
         */
        for (OmniFile hashDirToDelete : hashedDirsToDelete){
            hashDirToDelete.delete();
        }

        return searchPaths;
    }

    /**
     * Return a list of search indexes to include existing indexes and additional
     * default indexes. The returned list will not have duplicates.
     * @param volumeId
     * @return
     */
    public static JSONArray getIndexes( String volumeId) {

        JSONArray currentIndexes = getCurrentIndexes( volumeId);

        // Add common paths always to be displayed.
        // Always include the root path
        String rootPath = CConst.ROOT;
        addPath( currentIndexes, rootPath);

        // Remove duplicate paths that may have just been added
        JSONArray slimList = new JSONArray();
        Set<String> paths = new HashSet<String>();

        try {
            for( int i = 0; i < currentIndexes.length(); i++ ) {

                JSONObject item = currentIndexes.getJSONObject( i );
                String path = item.getString("path");

                if( paths.add( path ))
                    slimList.put( item );

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return slimList;
    }

    private static void addPath(JSONArray currentIndexes, String path) {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("path", path);
            currentIndexes.put(jsonObject);
        } catch (JSONException e) { }
    }

    /**
     * Add a new search index. First check if the path to the folder exists,
     * if not note an error that the folder is not found.
     * Next build the current set of active indexes and return it along with
     * the new search index.
     * @param volumeId
     * @param newPath
     * @return
     */
    public static JSONObject newIndex(String volumeId, String newPath) {

        JSONArray currentIndexes = getIndexes(volumeId);
        JSONObject result = new JSONObject();

        OmniFile newFolder = new OmniFile( volumeId, newPath);
        try {
            if( newFolder.exists() && newFolder.isDirectory()){

                result.put("error","");
                if( newPath.startsWith("/"))
                    addPath( currentIndexes, newPath);
                else
                    addPath( currentIndexes, "/"+newPath);
            }
            else
                result.put("error","Path is not a directory");

            result.put("paths",currentIndexes);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get a Lucene cache folder specific to to the search path.
     * @param volumeId
     * @param searchPath
     * @return
     */
    public static OmniFile getCacheDir(String volumeId, String searchPath){

        return new OmniFile( volumeId, getCacheDirPath( volumeId, searchPath));
    }

    public static String getCacheDirPath(String volumeId, String searchPath) {

        String hashedSearchPath = volumeId+OmniHash.encode(searchPath);
        return INDEX_FOLDER + hashedSearchPath;
    }

    public static boolean folderExists( String volumeId, String path) {

        OmniFile omniFile = new OmniFile( volumeId, path);

        return omniFile.exists() && omniFile.isDirectory();
    }

    public static Collection<OmniFile> getFilePaths( String volumeId, String path) {

        OmniFile topDir = new OmniFile( volumeId, path);
        Collection<OmniFile> files = OmniFileFilter.listFiles( topDir, TEXT_FILE_EXTENSION, true);

        return files;
    }

    /**
     * Delete the index referenced by the volumeId and path.
     * The folder deleted is named using the Omni hash of volumeId+path.
     * @param volumeId
     * @param path
     * @return
     */
    public static JSONObject deleteIndex( String volumeId, String path) {

        String error = "";
        int count = 0;
        String encodedPath = volumeId+OmniHash.encode( path);
        OmniFile indexFolder = new OmniFile( volumeId, INDEX_FOLDER+encodedPath);

        if( indexFolder.exists() && indexFolder.isDirectory()){

        Context ctx = App.getContext();
        count = OmniUtil.deleteRecursive( ctx, indexFolder);

        if( count == -1)
            error = "File delete error: ";
        else
            if( count == 0)
                error = "No files deleted: ";
        }
        else{
            error = "Index does not exist or is not a directory: ";
        }

        if( ! error.isEmpty())
            error = error + indexFolder.getAbsolutePath();

        JSONObject result = new JSONObject();
        try {
            result.put("count", count);
            result.put("error", error);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
