/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;//

import com.nuvolect.deepdive.main.CConst;
import com.nuvolect.deepdive.webserver.MimeUtil;
import com.nuvolect.deepdive.webserver.connector.FileObj;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * Represent a file that can either be an encrypted file or a standard java file.
 * Mirror support methods as necessary.
 */
public class OmniFile {

    private static boolean DEBUG = LogUtil.DEBUG;

    /**
     * Volume ID for the specified file. The volume ID ends with the '_' underscore character.
     */
    private final String m_volumeId;
    /**
     * Standard unix clear-text file. When the crypt file is in use this file is null.
     */
    private java.io.File m_std_file;
    /**
     * Encrypted unix file. When the standard java file is in use thsi file is null.
     */
    private info.guardianproject.iocipher.File m_cry_file;

    /**
     * Convenience boolean for standard(clear)/encrypted file.
     */
    private boolean m_isCryp;
    /**
     * Flag set when file is created indicating file is a root directory of the volume.
     */
    private boolean m_isRoot;      // File is root of the filesystem
    /**
     * Hash, if the file was created from a hash, otherwise null;
     */
    private String m_volumeHash;

    /**
     * Debugging only
     */
    private String m_name;         // Name as reported by file system
    private String m_absolutePath; // Complete path including root
    private String m_path;         // Path relative to the volume
    private String m_lastModified; // Text of last time modified

    public void calcDebug(String myTag){

            m_name = this.getName();
            m_path = this.getPath();
            m_absolutePath = this.getAbsolutePath();
            m_lastModified = TimeUtil.friendlyTimeMDYM( this.lastModified());
//            LogUtil.log( OmniFile.class, myTag+ " OmniFile:  "+ m_name+" last modified: "+ m_lastModified);
    }

    /**
     * Create an OmniFile from volumeId and path components.
     * @param volumeId
     * @param path
     */
    public OmniFile(String volumeId, String path) {

        m_volumeId = volumeId;
        m_isRoot = Omni.isRoot( volumeId, path);
        m_isCryp = m_volumeId.contentEquals(Omni.cryptoVolumeId);
        m_volumeHash = null;

        if( m_isCryp ){
            m_cry_file = new info.guardianproject.iocipher.File( path );
            /**
             * It appears as though IOCipher is ignoring file/folder timestamps, a big issue.
             */
//            long time = System.currentTimeMillis();
//            boolean success = m_cry_file.setLastModified( time );
//            if( ! success)
//                LogUtil.log(LogUtil.LogType.OMNI_FILE, "setLastModfiedFailed for: "+path);
//            if( time != lastModified())
//                LogUtil.log(LogUtil.LogType.OMNI_FILE,
//                        "time: "+time+" not equal "+lastModified());
        }
        else {
            String root = Omni.getRoot( volumeId);
            String absolutePath = (root + path).replace("//","/");
            m_std_file = new java.io.File(absolutePath);
        }

        if( DEBUG)
            calcDebug("OmniFile( vId, path)");
    }

    /**
     * Create an OmniFile from a volumeHash which has the
     * volume id on the front end of a hash.
     * @param volumeHash
     */
    public OmniFile( String volumeHash){

        if( volumeHash.startsWith("/"))
            volumeHash = volumeHash.substring(1);

        m_volumeHash = volumeHash;
        String segments[] = volumeHash.split("_");
        m_volumeId = segments[0];
        m_isRoot = Omni.isRoot( volumeHash);
        m_isCryp = m_volumeId.contentEquals(Omni.cryptoVolumeId);

        String path = (OmniHash.decode(segments[1])+"/").replace("//","/");
        String rootPath = Omni.getRoot( m_volumeId);
        if( ! rootPath.contentEquals("/"))
            path = (rootPath + path).replace("//","/");

        if( m_isCryp )
            m_cry_file = new info.guardianproject.iocipher.File( path );
        else
            m_std_file = new java.io.File( path );

        if( DEBUG)
            calcDebug("OmniFile( volumeHash)");
    }

    /**
     * Get the hash of the file not including the volume root.
     * @return
     */
    public String getHash(){

        return m_volumeId+"_"+OmniHash.encode( getPath());
    }

    /**
     * Returns a new file made from the pathname of the parent of this file.
     * This is the path up to but not including the last name in the path. {@code null} is
     * returned when there is no parent.
     *
     * @return a new file representing this file's parent or {@code null}.
     */
    public OmniFile getParentFile(){

        if( m_isCryp ){

            info.guardianproject.iocipher.File parent = m_cry_file.getParentFile();
            if( parent == null)
                return null;
            else
            return new OmniFile( m_volumeId, parent.getPath());
        }
        else {
            if( m_isRoot)
                return null;
            java.io.File parent = m_std_file.getParentFile();
            if( parent == null)
                return null;
            else{
                String parentPath = parent.getPath();
                String path = OmniUtil.getShortPath( m_volumeId, parentPath);
                return new OmniFile(m_volumeId, path);
            }
        }
    }

    public String getVolumeId() {
        return m_volumeId;
    }

    /**
     * Return a short path of the file, not including root.
     * Member vars m_volumeId and m_std_file must be set.
     * @return
     */
    public String getPath() {

        if( m_isRoot)
            return CConst.ROOT;

        String absolutePath;
        if( m_isCryp )
            absolutePath = m_cry_file.getPath();
        else
            absolutePath = m_std_file.getPath();//FIXME confirm works for private filesystem

            String root = Omni.getRoot( this.m_volumeId);
            String path = ("/"+StringUtils.removeStart( absolutePath, root)).replace("//","/");
            return path;
    }

    public String getAbsolutePath() {

        if( m_isCryp )
            return m_cry_file.getAbsolutePath();
        else
            return m_std_file.getAbsolutePath();
    }

    public String getCanonicalPath() throws IOException {

        if( m_isCryp )
            return m_cry_file.getCanonicalPath();
        else
            return m_std_file.getCanonicalPath();
    }

    public String getName() {

        if( this.isRoot())
            return Omni.getVolumeName( m_volumeId);

        String name;
        if( m_isCryp )
            name = m_cry_file.getName();
        else
            name = m_std_file.getName();

        return name;
    }

    public String getExtension() {

        return FilenameUtils.getExtension( getName()).toLowerCase(Locale.US);
    }

    public OmniFile[] listFiles() {

        if( m_isCryp ) {

            info.guardianproject.iocipher.File files[] = m_cry_file.listFiles();
            if( files == null || files.length == 0)
                return new OmniFile[0];
            OmniFile[] omniFiles = new OmniFile[files.length];

            for( int i = 0; i < files.length; i++)
                omniFiles[i] = new OmniFile(m_volumeId, files[i].getPath());

            return omniFiles;
        }
        else {
            java.io.File files[] = m_std_file.listFiles();
            if( files == null || files.length == 0)
                return new OmniFile[0];
            OmniFile[] omniFiles = new OmniFile[files.length];

            for( int i = 0; i < files.length; i++){
                String path = OmniUtil.getShortPath( m_volumeId, files[i].getPath());
                omniFiles[i] = new OmniFile(m_volumeId, path);
            }

            return omniFiles;
        }
    }

    public OmniFile[] listFiles(FileFilter filter) {

        java.io.File files[] = m_std_file.listFiles( filter);
        if( files == null || files.length == 0)
            return new OmniFile[0];
        OmniFile[] omniFiles = new OmniFile[files.length];

        for( int i = 0; i < files.length; i++){
            String path = OmniUtil.getShortPath( m_volumeId, files[i].getPath());
            omniFiles[i] = new OmniFile(m_volumeId, path);
        }

        return omniFiles;
    }


    public long lastModified() {
        if( m_isCryp )
            return m_cry_file.lastModified();
        else
            return m_std_file.lastModified();
    }

    public void setLastModified(long timeInSec) {

        if( timeInSec <= 0)
            return;

        if( m_isCryp )
            m_cry_file.setLastModified( timeInSec);
        else
            m_std_file.setLastModified( timeInSec);
    }

    /**
     * Set last modfied time based on the current time.
     */
    public void setLastModified() {
        long timeInSec = System.currentTimeMillis() / 1000;
        if( m_isCryp )
            m_cry_file.setLastModified( timeInSec);
        else
            m_std_file.setLastModified( timeInSec);
    }

    public long length() {
        if( m_isCryp )
            return m_cry_file.length();
        else
            return m_std_file.length();
    }

    public long getTotalSpace() {
        if( m_isCryp )
            return m_cry_file.getTotalSpace();
        else
            return m_std_file.getTotalSpace();
    }

    public long getFreeSpace() {
        if( m_isCryp )
            return m_cry_file.getFreeSpace();
        else
            return m_std_file.getFreeSpace();
    }

    public boolean canRead() {
        if( m_isCryp )
            return m_cry_file.canRead();
        else
            return m_std_file.canRead();
    }

    public boolean canWrite() {
        if( m_isCryp )
            return m_cry_file.canWrite();
        else
            return m_std_file.canWrite();
    }

    public boolean exists() {
        if( m_isCryp )
            return m_cry_file.exists();
        else
            return m_std_file.exists();
    }

    public boolean isFile() {
        if( m_isCryp )
            return m_cry_file.isFile();
        else
            return m_std_file.isFile();
    }

    public boolean isDirectory() {
        if( m_isCryp )
            return m_cry_file.isDirectory();
        else
            return m_std_file.isDirectory();
    }

    public boolean mkdirs() {
        if( m_isCryp )
            return m_cry_file.mkdirs();
        else
            return m_std_file.mkdirs();
    }

    public boolean mkdir() {
        if( m_isCryp )
            return m_cry_file.mkdir();
        else
            return m_std_file.mkdir();
    }

    public boolean createNewFile() {
        try {
            if( m_isCryp )
                return m_cry_file.createNewFile();
            else
                return m_std_file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean renameFile(OmniFile newFile) {
        if( m_isCryp )
            return m_cry_file.renameTo(newFile.getCryFile());
        else
            return m_std_file.renameTo( newFile.getStdFile());
    }

    public boolean isCryp() {
        return m_isCryp;
    }

    public boolean isStd() {
        return ! m_isCryp;
    }

    public info.guardianproject.iocipher.File getCryFile() {
        return m_cry_file;
    }

    public java.io.File getStdFile() {
        return m_std_file;
    }

    public OutputStream getOutputStream() throws FileNotFoundException {
        if( m_isCryp )
            return new info.guardianproject.iocipher.FileOutputStream(m_cry_file);
        else
            return new java.io.FileOutputStream(m_std_file);
    }

    public InputStream getFileInputStream() {

        try {

            if( m_isCryp)
                return new info.guardianproject.iocipher.FileInputStream( m_cry_file );
            else
                return new java.io.FileInputStream( m_std_file );

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean delete() {
        if( m_isCryp )
            return m_cry_file.delete();
        else
            return m_std_file.delete();
    }

    public String getMime() {

        if( this.isDirectory())
            return "directory";

        if( m_volumeHash != null){

            String hash = m_volumeHash;

            String segments[] = m_volumeHash.split("_");
            if( segments.length > 1){
                hash = segments[1];
            }

            try {
                String path = OmniHash.decodeWithException( hash);
                String extension = FilenameUtils.getExtension(path).toLowerCase(Locale.US);
                return MimeUtil.getMime( extension);
            } catch (UnsupportedEncodingException e) {
                return "";
            }
        }

        return MimeUtil.getMime( this );
    }

    public boolean isRoot() {

        return m_isRoot;
    }

    /**
     * Return an arrayList of file objects for this file.
     * @param httpIpPort
     * @return
     */
    public JSONArray listFileObjects(String httpIpPort) {

        if( DEBUG )
            LogUtil.log( OmniFile.class,"ListFileObjects from:  "+ this.getPath()
                + ", hash: " + this.getHash());

        JSONArray filesArray = new JSONArray();
        OmniFile[] files = this.listFiles();
        if( files == null || files.length == 0)
            return filesArray;

        int i = 0;
        String indent = "";
        String volumeId = this.getVolumeId();

        for( OmniFile file: files){

            JSONObject fileObj = FileObj.makeObj(volumeId, file, httpIpPort);
            filesArray.put( fileObj);

            String type = file.isDirectory()? " dir  ": " file ";
            if( DEBUG )
                LogUtil.log( OmniFile.class,"ListFileObjects "+ indent + ++i + type + file.getName()
                    + ", hash: " + file.getHash());
        }

        return filesArray;
    }

    public JSONObject getFileObject(String httpIpPort) {

        String volumeId = this.getVolumeId();
        JSONObject fileObj = FileObj.makeObj(volumeId, this, httpIpPort);

        return fileObj;
    }

    /**
     * Return contents of a file as a string.
     * @return
     */
    public String readFile() {

        String fileContents = "";
        StringBuilder sb = new StringBuilder();

        try {
            InputStream is = this.getFileInputStream();

            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) > 0) {

                String s = new String( buffer, 0, len, "UTF-8");
                sb.append( s );
            }
            fileContents = sb.toString();

            if( is != null)
                is.close();
        } catch (FileNotFoundException e) {
            LogUtil.logException(OmniFile.class, e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileContents;
    }

    /**
     * Write a string to the file.
     * @param fileContents
     * @return
     */
    public boolean writeFile(String fileContents){

        boolean success = true;
        try {
            OutputStream out = null;

            out = new BufferedOutputStream( this.getOutputStream());

            out.write(fileContents.getBytes());

            if( out != null)
                out.close();
        }
        catch (IOException e) {
            LogUtil.logException( OmniFile.class, "File write failed: ",e);
            success = false;
        }
        return success;
    }

    /**
     * Return the dimensions of an file if it is an image.
     * Return an empty string if the file is not an image.
     * @return
     */
    public String getDim() {

        return OmniImage.getDim( this );
    }

    /**
     * Return a PhotoSwipe object
     * @param httpIpPort
     * @return JSONObject
     */
    public JSONObject getPsObject(String httpIpPort) {

        JSONObject psObject = new JSONObject();

        try {

            psObject.put("name", this.getName());
            psObject.put("src", httpIpPort+"/"+this.getHash());
            OmniImage.addPsImageSize(this, psObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return psObject;
    }
}

