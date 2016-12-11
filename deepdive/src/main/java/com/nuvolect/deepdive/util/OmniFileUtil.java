package com.nuvolect.deepdive.util;//

import com.nuvolect.deepdive.webserver.connector.VolUtil;

import org.apache.commons.io.FilenameUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

//TODO create class description
//
public class OmniFileUtil {

    public static String getTimeDateFilename(String extension) {

        String filename = "mm"
                + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + extension;
        filename = filename.replaceAll(" ", "");
        return filename;
    }

    public static OmniFile getMediaOmniFile(String extension, Boolean secureMode) {

        /**
         * Get a time date stamped jpg path.
         */
        String filename = getTimeDateFilename( extension);
        OmniFile omniFile;

        if (secureMode) {

            // Root is simply '/' for encrypted storage
            String path = "/DCIM/Camera/" + filename;
            omniFile = new OmniFile(VolUtil.cryptoVolumeId, path);

        } else {

            String rootPath = VolUtil.getRoot(VolUtil.sdcardVolumeId);
            String path = rootPath + "DCIM/Camera/" + filename;
            omniFile = new OmniFile(VolUtil.sdcardVolumeId, path);
        }

        return omniFile;
    }

    /**
     * Get path of the DCIM/Camera folder.
     * Deal with special case for rootPath ending in '/'
     * @param volumeId
     * @return
     */
    public static String getDcimCameraPath(String volumeId){

        if( volumeId.contentEquals( VolUtil.cryptoVolumeId))
            return "/DCIM/Camera/";
        else
            return VolUtil.getRoot( volumeId)+"DCIM/Camera";
    }


    /**
     * Return a human readable status of a file system.
     * @param file
     * @return
     */
    public static String getFilesystemStatus(OmniFile file) {

        long freeSpace = file.getFreeSpace();
        long totalSpace = file.getTotalSpace();

        //
        String s = "Free "+humanReadableByteCount(freeSpace, true)
                +" of "+humanReadableByteCount(totalSpace, true);
        return s;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Return a unique file given the current file as a model.
     * Example if file exists: /Picture/mypic.jpg > /Picture/mypic~.jpg
     * @return
     */
    public static OmniFile makeUniqueName(OmniFile initialFile) {

        String path = initialFile.getPath();
        String basePath = FilenameUtils.getFullPath(path);    // path without name
        String baseName = FilenameUtils.getBaseName(path);    // name without extension
        String extension = FilenameUtils.getExtension(path);  // extension
        String volumeId = initialFile.getVolumeId();
        String dot = ".";
        if( extension.isEmpty())
            dot = "";
        OmniFile file = initialFile;

        while( file.exists()) {

//            LogUtil.log("File exists: "+file.getPath());
            baseName += "~";
            String fullPath = basePath+baseName+dot+extension;
            file = new OmniFile(volumeId, fullPath);
        }
//        LogUtil.log("File unique: "+file.getPath());
        return file;
    }

    public static OmniFile getOmniFileFromUri(String uri) {

        String uriSegments[] = uri.split("/");
        String volumeId = VolUtil.getVolumeId( uriSegments[1]);
        String basePath = OmniHash.decode(uriSegments[1].substring(3));
        if( uriSegments.length == 2)
            return new OmniFile( volumeId, basePath); // Typically a thumbnail file
        String fileName = FilenameUtils.getName( uri );
        String path = basePath+"/"+fileName;

        return new OmniFile( volumeId, path);
    }
}
