package com.nuvolect.deepdive.lucene;

import android.content.Context;
import android.os.Environment;

import com.nuvolect.deepdive.license.AppSpecific;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniHash;
import com.nuvolect.deepdive.util.Util;
import com.nuvolect.deepdive.webserver.connector.VolUtil;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.nuvolect.deepdive.webserver.MimeUtil.TEXT_FILE_EXTENSION;

public class IndexUtil {

    /**
     * Return a list of current Lucene search indexes and purge any indexes for
     * folders that no longer exist.
     * Currently search index are a list of objects and each object has a relative path.
     * This design is limted to operating with a single volume.
     *
     * FIXME Future design to support multiple volumes.
     * Return a list of objects with a relative path and a hashed path.
     * The hashed path contains volume information.
     * @return
     */
    public static JSONArray getCurrentIndexes(Context ctx) {

        String lucenePath = Util.createAppPublicFolder(ctx) + "/.lucene/";
        File luceneFolder = new File( lucenePath);
        JSONArray searchPaths = new JSONArray();

        File[] hashedDirs = luceneFolder.listFiles();
        if( hashedDirs == null)
            hashedDirs = new File[0];
        ArrayList<File> hashedDirsToDelete = new ArrayList<>();

        for( File hashedDir : hashedDirs){

            if( hashedDir.isDirectory()){

                String hashedDirPath = hashedDir.getName();
                LogUtil.log("hashedDirPath: "+hashedDirPath);

                String volumeId = hashedDirPath.substring(0,3);
                String hash = hashedDirPath.substring(3, hashedDirPath.length());
                String path = OmniHash.decode(hash);
                String root = VolUtil.getRoot(volumeId);
                String absolutePath = (root+path).replace("//","/");

                LogUtil.log("volumeId: "+volumeId);
                LogUtil.log("absolutePath: "+absolutePath);

                OmniFile omniFile = new OmniFile( volumeId, absolutePath);
//                OmniFile omniFile = new OmniFile( hashedDirPath);
                LogUtil.log("omniFile.getName: "+omniFile.getName());
                LogUtil.log("omniFile.getAbsolutePath: "+omniFile.getAbsolutePath());

                if( omniFile.exists()){

                    // put on list as an active searchable path
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("path", path);
                    } catch (JSONException e) { }

                    searchPaths.put(jsonObject);

                }else{

                    // Folder no longer exists, save to delete below
                    hashedDirsToDelete.add( hashedDir);
                }
            }
        }

        /**
         * Delete any index folders that no longer have
         */
        for( File hashDirToDelete : hashedDirsToDelete)
            hashDirToDelete.delete();

        return searchPaths;
    }

    /**
     * Return a list of search indexes to include existing indexes and additional
     * default indexes. The returned list will not have duplicates.
     * @param ctx
     * @return
     */
    public static JSONArray getIndexes(Context ctx) {

        JSONArray currentIndexes = getCurrentIndexes(ctx);

        // Add common paths always to be displayed.
        String rootPath = "/";//VolUtil.getRoot(VolUtil.sdcardVolumeId);
        String ptFolderPath = AppSpecific.getAppFolderPath(ctx);

        addPath( currentIndexes, rootPath);
        addPath( currentIndexes, ptFolderPath);

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
     * @param ctx
     * @param new_path
     * @return
     */
    public static JSONObject newIndex(Context ctx, String new_path) {

        JSONArray currentIndexes = getIndexes( ctx);
        JSONObject result = new JSONObject();

        String root_path = VolUtil.getRoot(VolUtil.sdcardVolumeId);
        File newFolder = new File( root_path + new_path);
        try {
            if( newFolder.exists() && newFolder.isDirectory()){

                result.put("error","");
                addPath( currentIndexes, new_path);
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
     * Get a Lucene cache folder.
     * @param relativeSearchPath
     * @return
     */
    public static File getLuceneCacheDir(Context ctx, String relativeSearchPath) {

        String volId = VolUtil.sdcardVolumeId;
        String hashedSearchPath = volId+ OmniHash.encode( relativeSearchPath);
        String lucenePath = Util.getAppPublicFolderPath(ctx) + "/.lucene/" + hashedSearchPath;
        return new File( lucenePath);
    }

    public static String getLuceneCacheDirPath(Context ctx, String relativeSearchPath) {

        String volId = VolUtil.sdcardVolumeId;
        String hashedSearchPath = volId+OmniHash.encode( relativeSearchPath);
        String lucenePath = Util.getAppPublicFolderPath(ctx) + "/.lucene/" + hashedSearchPath;
        return lucenePath;
    }

    public static boolean folderExists(String relativePath) {

        File dir = new File( Environment.getExternalStorageDirectory()+relativePath);
        return dir != null && dir.exists() && dir.isDirectory();
    }

    public static Collection getFilePaths(String topDirRelativePath) {

        File topDir = new File( Environment.getExternalStorageDirectory()+topDirRelativePath);
        Collection<File> files = FileUtils.listFiles(topDir, TEXT_FILE_EXTENSION, true);

        return files;
    }
}
