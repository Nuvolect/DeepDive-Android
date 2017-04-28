/*
 * Copyright (c) 2017. Nuvolect LLC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Contact legal@nuvolect.com for a less restrictive commercial license if you would like to use the
 * software without the GPLv3 restrictions.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package com.nuvolect.deepdive.util;

import android.content.Context;

import com.nuvolect.deepdive.webserver.MimeUtil;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Various utility methods to operate with Omni files.
 */
public class OmniUtil {

    public static InputStream getFileInputStream(OmniFile request) {

        try {

            if( request.isCryp())
                return new info.guardianproject.iocipher.FileInputStream( request.getCryFile());
            else
                return new java.io.FileInputStream( request.getStdFile());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return a file from a uri containing a hashed file path.
     * File may be null if the uri or hash is improperly formed.
     * @param uri
     * @return
     */
    public static OmniFile getFileFromUri(String uri) {

        String uriSegments[] = uri.split("/");
        if( uriSegments.length == 0)
            return null;

        return new OmniFile( uriSegments[1]);
    }

    /**
     * Return the file of a managed volume object.
     *
     * @param hash
     * @return
     */
    public static OmniFile getFileFromHash(String hash) {

        if( hash.startsWith("/"))
            hash = hash.substring(1);

        String segments[] = hash.split("_");
        String volumeId = segments[0];

        String path = OmniHash.decode(segments[1]);
        OmniFile targetFile = new OmniFile( volumeId, path );

        return targetFile;
    }

    /**
     * Create a file of a specific size. Works with all file types.
     * @param file
     * @param size
     * @throws IOException
     */
    public static void createFile(OmniFile file, long size) throws IOException {

        OutputStream out = file.getOutputStream();

        byte[] buffer = new byte[1024*8];// IOCipher works best with 8K blocks
        long progress = 0;
        int count = buffer.length;
        while( progress < size){
            out.write(buffer, 0, count);
            progress += count;
        }
        out.close();
    }

    public static long countBytes(OmniFile file) throws IOException {

        InputStream in = file.getFileInputStream();
        long count = 0;

        byte[] buffer = new byte[1024*8];// IOCipher works best with 8K blocks
        int read;
        while((read = in.read(buffer)) != -1){
            count += read;
        }
        in.close();

        return count;
    }

    public static String getTimeDateFilename(String extension) {

        String filename = "mm"
            + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + extension;
        filename = filename.replaceAll(" ", "");
        return filename;
    }

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

    /**
     * Get a Omni style relative path given a volume ID and an absolute path.
     * @param volumeId
     * @param absolutePath
     * @return relative Omni path.
     */
    public static String getShortPath(String volumeId, String absolutePath) {

        String root = Omni.getRoot(volumeId);
        String absPath = (absolutePath + "/").replace("//","/");
        return StringUtils.replaceOnce( absPath, root, "/");
    }

    /**
     * Recursively deletes a directory and its contents.
     * Note only the last success case is returned.
     *
     * @param f The directory (or file) to delete
     * @return true if the delete succeeded, false otherwise
     */
    public static int deleteRecursive(Context ctx, OmniFile f) {

        return deleteRecursive( ctx, f, 0);
    }

    private static int deleteRecursive(Context ctx, OmniFile f, int count) {

        if( count == -1)
            return -1;

        if (f.isDirectory()) {
            for (OmniFile child : f.listFiles()) {
                if (-1 == deleteRecursive(ctx, child, count)) {
                    return( -1 );
                }
            }
        }

        /**
         * Delete thumbnail, if there is one
         */
        OmniImage.deleteThumbnail( f );

        boolean success =f.delete();

        /**
         * The crypto storage does not use the media scanner.
         */
        //TODO cleanup
        //TODO consider role of a media scanner and lucene file management
//        if( success && f.isStd())
//            MediaScannerConnection.scanFile(
//                ctx,
//                new String[]{f.getAbsolutePath()},
//                null,
//                null);

        return success? ++count : -1;
    }

    /**
     * Makes a directory, including any necessary but nonexistent parent
     * directories. If a file already exists with specified name but it is
     * not a directory then an IOException is thrown.
     * If the directory cannot be created (or does not already exist)
     * then an IOException is thrown.
     *
     * @param directory directory to create, must not be {@code null}
     * @return true if directory is created
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException          if the directory cannot be created or the file already exists but is not a directory
     */
    public static boolean forceMkdir(final OmniFile directory) throws IOException {

        boolean directoryCreated = false;

        if (directory.exists()) {
            if (!directory.isDirectory()) {
                final String message =
                    "File "
                        + directory
                        + " exists and is "
                        + "not a directory. Unable to create directory.";
                throw new IOException(message);
            }
        } else {

            directoryCreated = directory.mkdirs();

            if (!directoryCreated) {
                // Double-check that some other thread or process hasn't made
                // the directory in the background
                if (!directory.isDirectory()) {
                    final String message =
                        "Unable to create directory " + directory;
                    throw new IOException(message);
                }
            }
        }
        return directoryCreated;
    }

    /**
     * Makes any necessary but nonexistent parent directories for a given File. If the parent directory cannot be
     * created then an IOException is thrown.
     *
     * @param file file with parent to create, must not be {@code null}
     * @throws NullPointerException if the file is {@code null}
     * @throws IOException          if the parent directory cannot be created
     * @since 2.5
     */
    public static boolean forceMkdirParent(final OmniFile file) throws IOException {
        final OmniFile parent = file.getParentFile();
        if (parent == null) {
            return false;
        }
        return forceMkdir(parent);
    }

    public static boolean writeFile(OmniFile file, String fileContents) {

        boolean success = true;
        try {
            OutputStream out = null;

            OmniUtil.forceMkdirParent( file);

            out = new BufferedOutputStream( new FileOutputStream( file.getStdFile()));

            out.write(fileContents.getBytes());

            if( out != null)
                out.close();
        }
        catch (IOException e) {
            LogUtil.log( OmniUtil.class, "File write failed: " + e.toString());
            success = false;
        }
        return success;
    }

    /**
     * Return the contents of a text file. Upon error return feedback to the user
     * in the file_content field. If the volumeId is empty, use the default volumeId.
     * @param volumeId volume of the file
     * @param path     path, relative to the volume
     * @return
     */
    public static JSONObject getText(String volumeId, String path) {

        String file_ext = FilenameUtils.getExtension( path);
        boolean isText = MimeUtil.isText( file_ext);

        String file_content = "";
        if( volumeId.isEmpty())
            volumeId = Omni.getDefaultVolumeId();

        OmniFile omniFile = new OmniFile( volumeId, path );

        if( ! omniFile.exists())
            file_content = "File does not exist: "+path;
        else
        if( omniFile.isDirectory())
            file_content = "File is a directory: "+path;
        else
        if( ! isText)
            file_content = "File is binary: "+path;
        else{
            file_content = FileUtil.readFile( omniFile.getStdFile());
        }
        JSONObject wrapper = new JSONObject();
        try {
            wrapper.put("file_name", omniFile.getName());
            wrapper.put("file_content", file_content);
            wrapper.put("file_path", path);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return wrapper;
    }

    /**
     * Copy a file from assets to an omni destination folder.
     * @param ctx
     * @param assetFilePath
     * @param destinationFolder
     * @return number of bytes copied
     * @throws IOException
     */
    public static int copyAsset(Context ctx, String assetFilePath, OmniFile destinationFolder) throws IOException {

        InputStream inputStream = ctx.getAssets().open( assetFilePath);
        OutputStream outputStream = destinationFolder.getOutputStream();
        int numBytes = IOUtils.copy( inputStream, outputStream);
        inputStream.close();
        outputStream.close();

        return numBytes;
    }
}
