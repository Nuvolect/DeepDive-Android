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

package com.nuvolect.deepdive.util;//

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Mirror Java Files to handle encrypted and standard files and directories.
 */
public class OmniFiles {

    /**
     * Copy bytes from one file to another.
     * @param fromFile
     * @param toFile
     */
    public static boolean copyFile(OmniFile fromFile, OmniFile toFile) {

        boolean success = true;
        try {
            copyFile(fromFile.getFileInputStream(), toFile.getOutputStream());
            toFile.setLastModified( fromFile.lastModified());

        } catch (IOException e) {
            LogUtil.logException(OmniFiles.class, e);
            success = false;
        }
        return success;
    }

    /**
     * Mixed File to Omni file copy. This is used for files that cannot be accessed by Omni
     * such as app/cache/NanoHttp file uploads.
     * @param fromFile
     * @param toFile
     * @return
     */
    public static boolean copyFile(File fromFile, OmniFile toFile) {
        boolean success = true;
        try {
            FileInputStream fis = new FileInputStream( fromFile);
            copyFile( fis, toFile.getOutputStream());
            toFile.setLastModified( fromFile.lastModified());

        } catch (IOException e) {
            LogUtil.logException(OmniFiles.class, e);
            success = false;
        }
        return success;
    }

    /**
     * Copy entire directory structures.  Works with mixed volume types.
     * @param srcDir
     * @param destDir
     * @return
     */
    public static boolean copyDirectory(OmniFile srcDir, OmniFile destDir) {

        boolean success = true;
        try {

            copyFolder(srcDir, destDir);

        } catch (IOException e) {
            LogUtil.logException(OmniFiles.class, e);
            success = false;
        }
        return success;
    }

    /**
     * Simple file copy that works with all file types.
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024*8];// IOCipher works best with 8K blocks
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
        in.close();
        out.close();
    }

    /**
     * Simple file copy that works with all file types.
     * Leaves input stream open.
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copyFileLeaveInOpen(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024*8];// IOCipher works best with 8K blocks
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
        // in.close(); // leave input stream open
        out.close();
    }

    /**
     * Simple file copy that works with all file types.
     * Leaves output stream open.
     * @param in
     * @param out
     * @throws IOException
     */
    public static int copyFileLeaveOutOpen(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[1024*8];// IOCipher works best with 8K blocks
        int read;
        int totalRead = 0;

        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
            totalRead += read;
        }
        in.close();
//        out.close(); // leave output stream open
        return totalRead;
    }

    /**
     * Copy folders and files.  It calls itself recursively operating with mixed volume types.
     * @param srcDir
     * @param destDir
     * @throws IOException
     */
    private static void copyFolder(OmniFile srcDir, OmniFile destDir) throws IOException {

        String destVolumeId = destDir.getVolumeId();

        if(srcDir.isDirectory()){

            //if directory not exists, create it
            if(!destDir.exists()){
                destDir.mkdir();
                destDir.setLastModified( srcDir.lastModified());
                LogUtil.log(LogUtil.LogType.OMNI_FILES, "Directory created: "+ destDir.getPath());
            }

            // get directory contents
            OmniFile files[] = srcDir.listFiles();

            for (OmniFile file : files) {
                //construct dest file structure
                OmniFile destFile = new OmniFile(destVolumeId, destDir.getPath()+"/"+file.getName());
                //recursive copy
                copyFolder( file, destFile);
            }

        }else{
            //if file, then copy it
            //Use bytes stream to support all file types
            InputStream in = srcDir.getFileInputStream();
            OutputStream out = destDir.getOutputStream();

            copyFile(in, out);

            LogUtil.log(LogUtil.LogType.OMNI_FILES, "File copied from "
                    + srcDir.getPath() + " to " + destDir.getPath());
        }
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
}

