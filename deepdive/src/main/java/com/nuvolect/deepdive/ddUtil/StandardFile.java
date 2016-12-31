package com.nuvolect.deepdive.ddUtil;//

import android.content.Context;
import android.media.MediaScannerConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//TODO create class description
//
public class StandardFile {


    // based on http://stackoverflow.com/a/16646691/115145

    private static void zipDirectory(Context ctxt, File dir,
                                     File zipFile) throws IOException {
        FileOutputStream fout = new FileOutputStream(zipFile);
        ZipOutputStream zout = new ZipOutputStream(fout);

        zipSubDirectory(ctxt, "", dir, zout);
        zout.flush();
        fout.getFD().sync();
        zout.close();
    }

    private static void zipSubDirectory(Context ctxt,
                                        String basePath, File dir,
                                        ZipOutputStream zout)
            throws IOException {
        byte[] buffer = new byte[4096];
        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                String path = basePath + file.getName() + "/";
                zout.putNextEntry(new ZipEntry(path));
                zipSubDirectory(ctxt, path, file, zout);
                zout.closeEntry();
            }
            else {
                MediaScannerConnection.scanFile(
                        ctxt,
                        new String[]{file.getAbsolutePath()},
                        null,
                        null);

                FileInputStream fin = new FileInputStream(file);

                zout.putNextEntry(new ZipEntry(basePath + file.getName()));

                int length;

                while ((length = fin.read(buffer)) > 0) {
                    zout.write(buffer, 0, length);
                }

                zout.closeEntry();
                fin.close();
            }
        }
    }


    /**
     * Recursively deletes a directory and its contents.
     *
     * @param f The directory (or file) to delete
     * @return true if the delete succeeded, false otherwise
     */
    public boolean delete(Context ctx, File f) {

        if (f.isDirectory()) {
            for (File child : f.listFiles()) {
                if (!delete(ctx, child)) {
                    return(false);
                }
            }
        }

        boolean result=f.delete();

        MediaScannerConnection.scanFile(
                ctx,
                new String[]{f.getAbsolutePath()},
                null,
                null);

        return(result);
    }

}
