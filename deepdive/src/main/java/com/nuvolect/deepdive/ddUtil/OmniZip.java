package com.nuvolect.deepdive.ddUtil;//

import android.content.Context;
import android.media.MediaScannerConnection;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

//TODO create class description
//
public class OmniZip {

    public static boolean zipFiles(Context ctx, OmniFile[] files, OmniFile zipOmni, int destination) {

        ZipOutputStream zos = null;
        LogUtil.log(LogUtil.LogType.OMNI_ZIP, "ZIPPING TO: " + zipOmni.getPath());

        try {
            zos = new ZipOutputStream( zipOmni.getOutputStream());

            for(OmniFile file : files){

                if( file.isDirectory()){

                    zipSubDirectory( ctx,  "", file, zos);
                }else{

                    LogUtil.log(LogUtil.LogType.OMNI_ZIP,
                            "zipping up: " + file.getPath() + " (bytes: " + file.length() + ")");

                    /**
                     * Might get lucky here, can it associate the name with the copyLarge that follows
                     */
                    ZipEntry ze = new ZipEntry(file.getName());
                    zos.putNextEntry(ze);

                    IOUtils.copyLarge(file.getFileInputStream(), zos);

                    zos.flush();
                }
            }

            zos.close();
            return true;
        } catch(IOException e) {
            LogUtil.logException(LogUtil.LogType.OMNI_ZIP, e);
            e.printStackTrace();
        }

        return false;
    }

    private static void zipSubDirectory(
            Context ctx, String basePath, OmniFile dir, ZipOutputStream zos) throws IOException {

        LogUtil.log(LogUtil.LogType.OMNI_ZIP, "zipSubDirectory : " + dir.getPath());

        OmniFile[] files = dir.listFiles();

        for (OmniFile file : files) {

            if (file.isDirectory()) {
                String path = basePath + file.getName() + "/";
                zos.putNextEntry(new ZipEntry(path));
                zipSubDirectory(ctx, path, file, zos);
                zos.closeEntry();
            } else {
                if (file.isStd())// don't scan crypto volume
                    MediaScannerConnection.scanFile(
                            ctx,
                            new String[]{file.getAbsolutePath()},
                            null,
                            null);

                zipFile(basePath, file, zos);
            }
        }
    }

    public static void zipFile(String basePath, OmniFile file, ZipOutputStream zout)
            throws IOException {

        LogUtil.log(LogUtil.LogType.OMNI_ZIP, "zipFile : " + file.getPath());

        byte[] buffer = new byte[4096];

        InputStream fin = file.getFileInputStream();

        zout.putNextEntry(new ZipEntry(basePath + file.getName()));

        int length;

        while ((length = fin.read(buffer)) > 0) {
            zout.write(buffer, 0, length);
        }

        zout.closeEntry();
        fin.close();
    }

    /**
     * Unzip the file into the target directory.
     * The added structure is updated to reflect any new files and directories created.
     * Overwrite files when requested.
     * @param zipOmni
     * @param targetDir
     * @param added
     * @param httpIpPort
     * @return
     */
    public static boolean unzipFile (
            OmniFile zipOmni, OmniFile targetDir, JSONArray added, String httpIpPort) {

        String volumeId = zipOmni.getVolumeId();
        String rootFolderPath = targetDir.getPath();
        boolean DEBUG = true;

        /**
         * Keep a list of all directories created.
         * Defer creating the directory object files until the zip is extracted.
         * This way the dir=1/0 settings can be set accurately.
         */
        ArrayList<OmniFile> directories = new ArrayList<>();

        ZipInputStream zis = new ZipInputStream( zipOmni.getFileInputStream());

        ZipEntry entry = null;
        try {
            while((entry = zis.getNextEntry()) != null) {

                if(entry.isDirectory()) {

                    OmniFile dir = new OmniFile( volumeId, entry.getName());
                    if( dir.mkdir()){

                        directories.add( dir );
                        if( DEBUG )
                            LogUtil.log(LogUtil.LogType.OMNI_ZIP, "dir created: "+dir.getPath());
                    }
                }else{

                    String path = rootFolderPath+"/"+entry.getName();
                    OmniFile file = new OmniFile( volumeId, path);
                    // Create any necessary directories
                    file.getParentFile().mkdirs();

                    if( file.exists()){
                        file = OmniUtil.makeUniqueName( file );
                    }
                    OutputStream out = file.getOutputStream();

                    OmniFiles.copyFileLeaveInOpen( zis, out);
                    if( DEBUG )
                        LogUtil.log(LogUtil.LogType.OMNI_ZIP, "file created: "+file.getPath());

                    if( added != null)
                        added.put(file.getFileObject(httpIpPort));
                }
            }
            zis.close();

            if( added != null){

                /**
                 * Iterate over the list of directories created and
                 * create object files for each.
                 * The full tree is now expanded such that the dir=1/0
                 * can be set accurately.
                 */
                for( OmniFile dir : directories)
                    added.put(dir.getFileObject( httpIpPort ));
            }

        } catch (IOException e) {
            LogUtil.logException(LogUtil.LogType.OMNI_ZIP, e);
            return false;
        }

        return true;
    }

    public static List<String> getFilesList(OmniFile zipOmni) {

        ZipInputStream zis = new ZipInputStream( zipOmni.getFileInputStream());

        List<String> arrayList = new ArrayList<>();

        ZipEntry entry = null;
        try {
            while((entry = zis.getNextEntry()) != null) {

                arrayList.add( entry.getName());
            }
            zis.close();

        } catch (IOException e) {
            LogUtil.logException(LogUtil.LogType.OMNI_ZIP, e);
        }

        return arrayList;
    }

    public static List<String> getDirsList(OmniFile zipOmni) {

        ZipInputStream zis = new ZipInputStream( zipOmni.getFileInputStream());

        List<String> arrayList = new ArrayList<>();

        ZipEntry entry = null;
        try {
            while((entry = zis.getNextEntry()) != null) {

                if( entry.isDirectory())
                    arrayList.add( entry.getName());
            }
            zis.close();

        } catch (IOException e) {
            LogUtil.logException(LogUtil.LogType.OMNI_ZIP, e);
        }

        return arrayList;
    }
}
