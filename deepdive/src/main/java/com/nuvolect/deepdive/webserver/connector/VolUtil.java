package com.nuvolect.deepdive.webserver.connector;//

import android.os.Environment;

import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniHash;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Various utility methods and constants to work with encrypted and clear text file volumes.
 */
public class VolUtil {

    private static JSONObject volPath = new JSONObject();
    private static JSONObject volName = new JSONObject();
    private static JSONObject volHash = new JSONObject();

    public static String sdcardVolumeId = "l0_"; // Local Volume 0
    public static String cryptoVolumeId = "c0_"; // Encrypted Volume 0
    private  static String[] volumeIds = {/*cryptoVolumeId, */ sdcardVolumeId};
    public static String crypRoot = "/";
    public static String thumbnailFolderName = "/.tmb";
    public static String dotThumbnailFolderName = ".tmb/";
    public static String thumbnailFolderNameSlash = thumbnailFolderName+"/";

    /**
     * Build data structures for managing volumes.
     * Probe the device to get current volumes.
     */
    public static boolean init() {

        boolean success = true;

        try {
            // Save a logical name for each volume
            volName.put(sdcardVolumeId, "sdcard");
// this app does not use a crypto volume
//            volName.put(cryptoVolumeId, "crypto");

            // Save the default root path for each volume
            volPath.put(sdcardVolumeId, Environment.getExternalStorageDirectory()+"/");
// this app does not use a crypto volume
//            volPath.put(cryptoVolumeId, "/");

            // Save the hash of each volume
            volHash.put(sdcardVolumeId + OmniHash.encode( volPath.getString(sdcardVolumeId)), sdcardVolumeId);
// this app does not use a crypto volume
//            volHash.put(cryptoVolumeId + OmniHash.encode( volPath.getString(cryptoVolumeId)), cryptoVolumeId);

            /**
             * Create thumbnail folder for each volume if necessary.
             */
            Iterator<?> keys = volPath.keys();

            while (keys.hasNext()) {

                String key = (String) keys.next();
                String volumeId = key;

                /*
                 * mkdirs() method creates the directory mentioned by this abstract
                 * pathname including any necessary but nonexistent parent directories.
                 *
                 * Accordingly it will return TRUE or FALSE if directory created
                 * successfully or not. If this operation fails it may have
                 * succeeded in creating some of the necessary parent directories.
                 */
                OmniFile f = new OmniFile( volumeId, volPath.getString(key)+dotThumbnailFolderName);

                boolean folderCreated = f.mkdirs();
                if( folderCreated)
                    LogUtil.log(LogUtil.LogType.VOL_UTIL,
                            "Thumbnail folder created: "+volumeId + f.getPath());
                else
                    LogUtil.log(LogUtil.LogType.VOL_UTIL,
                            "Thumbnail folder exists: " +volumeId+ f.getPath());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            success = false;
            LogUtil.log(LogUtil.LogType.VOL_UTIL, "Volume JSON exception");
        }

        return success;
    }

    /**
     * Return the name of a file with special handling of the root file name
     *
     * @param rootPath
     * @param file
     * @return
     */
    public static String getName(String volId, String rootPath, OmniFile file) {

        if( volPath.length() == 0)
            init();

        String fullPath = file.getAbsolutePath();
        String relativePath = fullPath.replaceFirst(Pattern.quote(rootPath), "");

        String name="";

        if (relativePath.isEmpty())
            try {

                name = volName.getString(volId);

            } catch (JSONException e) {
                e.printStackTrace();
                name = "invalid volId";
            }
        else
            name = file.getName();

        return name;
    }

    /**
     * Return a string array of volume Ids.
     * @return
     */
    public static String[] getVolumeIds() {

        if( volPath.length() == 0)
            init();

        return volumeIds;
    }

    /**
     * Test of the uri references a managed volume.
     *
     * @param uri
     * @return
     */
    public static boolean matchVolume(String uri) {

        if( volPath.length() == 0)
            init();

        String segments[] = uri.split("_");
        String part1 = segments[0] + "_";

        // Strip off leading slash if there is one
        String possibleVolumeId = part1;
        if( possibleVolumeId.startsWith("/"))
            possibleVolumeId = part1.substring(1);

        return volPath.has(possibleVolumeId);
    }

    /**
     * Return the file of a managed volume object.
     *
     * @param hash
     * @return
     */
    public static OmniFile getFileFromHash(String hash) {

        String segments[] = hash.split("_");
        String volumeId = segments[0] + "_";

        String path = OmniHash.decode(segments[1]);
        OmniFile targetFile = new OmniFile( volumeId, path );

//        LogUtil.log(LogUtil.LogType.VOL_UTIL, "Target path: " + path);

        return targetFile;
    }

    public static InputStream getFileInputStream(OmniFile request) {

        try {

            if( request.isCryp())
                return new info.guardianproject.iocipher.FileInputStream(
                        request.getCryFile());
            else
                return new java.io.FileInputStream( request.getStdFile());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <pre>
     * Get the root path of a volume terminated with '/'.
     * Examples:
     * l0_ /storage/emulated/0/
     * c0_ /
     * @param volumeId
     * @return
     * </pre>
     */
    public static String getRoot(String volumeId) {
        try {
            if( volPath == null || volPath.length() == 0)
                init();
            return volPath.getString(volumeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "volumeId error";
    }

    /**
     * Return the volumeId of a hash if it has one, otherwise
     * return an empty string;
     * @param hash
     * @return
     */
    public static String getVolumeId(String hash) {

        String segments[] = hash.split("_");
        if( segments.length == 0)
            return "";

        if( volPath.length() == 0)
            init();

        String volumeId = segments[0] + "_";

        /**
         * Identify the volumeId by testing if it is a key in the map.
         */
        if( volPath.has(volumeId))
            return volumeId;
        else
            return "";
    }

    public static String getVolumeName( String volumeId){

        if( volName == null || volName.length() == 0)
            init();

        try {
            return volName.getString( volumeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "bad volumeId";
    }

    public static InputStream getFileInputStreamFromHash(String hash) {

        OmniFile file = new OmniFile(hash);
        return file.getFileInputStream();
    }

    /**
     * Determine if a file hash is a root directory.
     *
     * @param volumeId
     * @param path
     * @return
     */
    public static boolean isRoot(String volumeId, String path) {

        String hash = volumeId+ OmniHash.encode(path);

        return volHash.has(hash);
    }

    /**
     * Return volumeId of a of a mixed hash/path uri.
     * Example:
     * http://10.0.1.25:8218/l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC9Eb3dubG9hZA/Download/frozen%20rose.jpg
     * returns: l0_
     * @param uri
     * @return
     */
    public static String getVolumeIdFromUri(String uri) {

        String segments[] = uri.split("/");
        if( segments.length == 0)
            return "";

        String volumeId = getVolumeId(segments[1]);

        return volumeId;
    }

    /**
     * Return path of a mixed hash/path uri.
     * Examples:
     * 1. volumeId + hash + path
     *    /l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC9Eb3dubG9hZA/Download/frozen%20rose.jpg
     * returns: /storage/emulated/0/Download/frozen rose.jpg
     * 2. volumeId + hash
     *    /l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC9QaWN0dXJlcy9mcm96ZW4gcm9zZS5qcGc
     * returns: /storage/emulated/0/Pictures/frozen rose.jpg
     * @param uri
     * @return
     */
    public static String getPathFromUri(String uri) {

        String uriSegments[] = uri.split("/");
        if( uriSegments.length == 0)
            return "";

        String volumeHash = uriSegments[1];
        String hashSegments[] = volumeHash.split("_");

        if( hashSegments.length == 0)
            return "";

        if( uri.contains("css"))
            LogUtil.log(LogUtil.LogType.VOL_UTIL, "contains css: "+uri);

        String path;

        if( uriSegments.length > 2){

            String p2 = getVolumeId( uri );
            for( int i = 2; i < uriSegments.length; i++)
                p2 += uriSegments[i];

            path = OmniHash.decode(hashSegments[1])+ "/"+uriSegments[uriSegments.length-1];
        }
        else{

            path = OmniHash.decode(hashSegments[1]);
        }

        return path;
    }

    public static OmniFile getFileFromUri(String uri) {

        String uriSegments[] = uri.split("/");
        if( uriSegments.length == 0)
            return null;

        String volumeHash = uriSegments[1];
        String hashSegments[] = volumeHash.split("_");

        if( hashSegments.length == 0)
            return null;

        String volumeId = hashSegments[0]+"_";

        if( uri.contains("css"))
            LogUtil.log(LogUtil.LogType.VOL_UTIL, "contains css: "+uri);

        String path=getRoot( volumeId);
        String slash = "";

        if( uriSegments.length > 2){

            for( int i = 2; i < uriSegments.length; i++) {
                path += slash + uriSegments[i];
                slash = "/";
            }

        }
        else{

            path = OmniHash.decode(hashSegments[1]);
        }

        return new OmniFile( volumeId, path);
    }
}
