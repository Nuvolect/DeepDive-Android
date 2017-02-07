package com.nuvolect.deepdive.probe;
//
//TODO create class description
//

import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniFiles;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ApkZipUtil {

    /**
     * Unzip while excluding XML files into the target directory.
     * The added structure is updated to reflect any new files and directories created.
     * Overwrite files when requested.
     * @param zipOmni
     * @param targetDir
     * @param progressStream
     * @return
     */
    public static boolean unzipAllExceptXML(OmniFile zipOmni, OmniFile targetDir, ProgressStream progressStream) {

        String volumeId = zipOmni.getVolumeId();
        String rootFolderPath = targetDir.getPath();
        boolean DEBUG = true;

        ZipInputStream zis = new ZipInputStream( zipOmni.getFileInputStream());
        ZipEntry entry = null;
        try {
            while((entry = zis.getNextEntry()) != null) {

                if(entry.isDirectory()) {

                    OmniFile dir = new OmniFile( volumeId, entry.getName());
                    if( dir.mkdir()){

                        if( DEBUG )
                            LogUtil.log(LogUtil.LogType.OMNI_ZIP, "dir created: "+dir.getPath());
                    }
                }else{

                    String path = rootFolderPath+"/"+entry.getName();
                    OmniFile file = new OmniFile( volumeId, path);

                    if( ! FilenameUtils.getExtension( file.getName()).contentEquals("xml")){

                        // Create any necessary directories
                        file.getParentFile().mkdirs();

                        OutputStream out = file.getOutputStream();

                        OmniFiles.copyFileLeaveInOpen( zis, out);
                        if( DEBUG )
                            LogUtil.log(LogUtil.LogType.OMNI_ZIP, "file created: "+file.getPath());

                        progressStream.putStream( "Unpacked: "+entry.getName());
                    }
                }
            }
            zis.close();

        } catch (IOException e) {
            LogUtil.logException(LogUtil.LogType.OMNI_ZIP, e);
            return false;
        }

        return true;
    }


    public static boolean unzip(OmniFile zipOmni, OmniFile targetDir, ProgressStream progressStream) {

        String volumeId = zipOmni.getVolumeId();
        String rootFolderPath = targetDir.getPath();
        boolean DEBUG = true;

        ZipInputStream zis = new ZipInputStream( zipOmni.getFileInputStream());
        ZipEntry entry = null;
        try {
            while((entry = zis.getNextEntry()) != null) {

                if(entry.isDirectory()) {

                    OmniFile dir = new OmniFile( volumeId, entry.getName());
                    if( dir.mkdir()){

                        if( DEBUG )
                            LogUtil.log(LogUtil.LogType.OMNI_ZIP, "dir created: "+dir.getPath());
                    }
                }else{

                    String path = rootFolderPath+"/"+entry.getName();
                    OmniFile file = new OmniFile( volumeId, path);

                    // Create any necessary directories
                    file.getParentFile().mkdirs();

                    OutputStream out = file.getOutputStream();

                    OmniFiles.copyFileLeaveInOpen( zis, out);
                    if( DEBUG )
                        LogUtil.log(LogUtil.LogType.OMNI_ZIP, "file created: "+file.getPath());

                    progressStream.putStream( "Unpacked: "+entry.getName());
                }
            }
            zis.close();

        } catch (IOException e) {
            LogUtil.logException(LogUtil.LogType.OMNI_ZIP, e);
            return false;
        }

        return true;
    }
}
