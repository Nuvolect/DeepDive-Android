package com.nuvolect.deepdive.util;//

import android.content.Context;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

/**
 * Omni provides a elFinder compatible thin layer between the app and native file utilities.
 * It allows common code to interact with volumes of different types such
 * as clear text volumes, encrypted volumes and (future) network volumes.
 * It provides an abstraction for the root path of a file system.
 *
 * An physical file path is derived from two parts, a volumeId and a path.
 * The volumeId represents root of the path upto the root '/';
 * The path is appended to the root to make the full physical path.
 *
 * A volume can be an entire disk or a sub-tree inside a filesystem.
 * The volume root can be hidden and inaccessable from the user interface.
 * For example userVolumeId "u0_" can be positioned at:
 *     /Users/mattkraus/.deep_dive/
 * The path /com.company.appname combined with the volume root makes:
 *     /Users/mattkraus/.deep_dive/com.company.appname
 * Browsing the filesystem the user only sees /com.company.appname.
 *
 * Class organization
 * Omni.java - This is the base class for the Omni system with a few utilities to access Omni member data
 * OmniFile - Analogise to java 'File' class
 * OmniFiles - Analogise to java 'Files' class
 * OmniHash - Utilities dealing with encoding and decoding hashes
 * OmniImage - Utilities specific to imaging functions
 * OmniZip - Utilities for zip archive management
 * OmniUtil - All other Omni utilities
 */
public class Omni {

    private static JSONObject volRoot; // key: vId, value: path to root, ending in '/'
    private static JSONObject volName; // key: vId, value: volume name
    private static JSONObject volHash; // key: vHash, value: vId

    public static String localVolumeId = "l0_"; // Local Volume 0, sdcard on Android, root on Linux
    public static String userVolumeId = "u0_"; // User Volume 0, relative to user file
    public static String cryptoVolumeId = "c0_"; // Encrypted Volume 0
    private  static String[] allVolumeIds = {localVolumeId, userVolumeId, cryptoVolumeId};
    private  static String[] allVolumeNames = {"sdcard", "private", "crypto"};
    private  static String[] activeVolumeIds = {};
    public static String localRoot;
    public static String userRoot;
    public static String crypRoot;
    public static final String THUMBNAIL_FOLDER_PATH = "/.tmb/";

    /**
     * Build data structures for managing volumes.
     * Probe the device to get current volumes.
     * @param ctx
     */
    public static boolean init(Context ctx) {

        /**
         * Each root starts and ends with SLASH
         */
        crypRoot = CConst.ROOT;
        localRoot = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
        userRoot = ctx.getApplicationInfo().dataDir+"/omni/";

        boolean success = true;
        volRoot = new JSONObject();
        volName = new JSONObject();
        volHash = new JSONObject();

        try {
            volRoot.put( localVolumeId, localRoot);
            volRoot.put( userVolumeId, userRoot);
            volRoot.put( cryptoVolumeId, crypRoot);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            for( int i = 0; i < allVolumeIds.length; i++){

                String id = allVolumeIds[i];
                // Save a logical name for each volume
                volName.put( id, allVolumeNames[i]);

                // Save the hash of each volume
                volHash.put( id + OmniHash.encode( CConst.ROOT ),  id);
            }

            /**
             * Give the user two volumes, the SDCARD and a private volume
             */
            activeVolumeIds = new String[]{ localVolumeId, userVolumeId};
            /**
             * Create thumbnail folder for each volume if necessary.
             */
            for ( String volumeId : activeVolumeIds) {

                /*
                 * mkdirs() method creates the directory mentioned by this abstract
                 * pathname including any necessary but nonexistent parent directories.
                 *
                 * Accordingly it will return TRUE or FALSE if directory created
                 * successfully or not. If this operation fails it may have
                 * succeeded in creating some of the necessary parent directories.
                 */
                OmniFile f = new OmniFile( volumeId, THUMBNAIL_FOLDER_PATH);

                boolean folderCreated = f.mkdirs();
                if( folderCreated)
                    LogUtil.log( Omni.class, "Thumbnail folder created: "+volumeId + f.getPath());
                else
                    LogUtil.log( Omni.class, "Thumbnail folder exists: " +volumeId+ f.getPath());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            success = false;
            LogUtil.log( Omni.class, "Volume JSON exception");
        }

        return success;
    }

    /**
     * Return a string array of active volume Ids.
     * @return
     */
    public static String[] getActiveVolumeIds() {

        return activeVolumeIds;
    }

    /**
     * Test if a volumeId is among the active volume IDs
     * @param volumeId
     * @return
     */
    public static boolean isActiveVolume( String volumeId){

        for( String vol : activeVolumeIds) {

            if( vol.contentEquals( volumeId))
                return true;
        }
        return false;
    }

    /**
     * Test of the uri references a managed volume.
     *
     * @param uri
     * @return
     */
    public static boolean matchVolume(String uri) {

        String segments[] = uri.split("_");
        String part1 = segments[0] + "_";

        // Strip off leading slash if there is one
        String possibleVolumeId = part1;
        if( possibleVolumeId.startsWith("/"))
            possibleVolumeId = part1.substring(1);

        return isActiveVolume( possibleVolumeId);
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
            return volRoot.getString(volumeId);
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

        String volumeId = segments[0] + "_";

        /**
         * Identify the volumeId by testing if it is a key in the map.
         */
        if( isActiveVolume( volumeId))
            return volumeId;
        else
            return "";
    }

    public static String getVolumeName( String volumeId){

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
     * Determine if a file is a root directory.
     * Examples for root: l0_Lw, m0_lw
     *
     * @param volumeHash : volumeId followed by the encoded short path
     * @return
     */
    public static boolean isRoot(String volumeHash) {

        return volHash.has( volumeHash);
    }

    /**
     * Determine if a file is a root directory.
     *
     * @param volumeId
     * @param path
     * @return
     */
    public static boolean isRoot(String volumeId, String path) {

        return volRoot.has( volumeId) && path.contentEquals(CConst.ROOT);
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
        if( segments.length == 0 || segments[1].isEmpty())
            return "";

        /**
         * Get the volumeId from the segment and confirm it is an active volume.
         */
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


}
