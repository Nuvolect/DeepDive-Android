package com.nuvolect.deepdive.ddUtil;
//
//TODO create class description
//

import android.content.Context;

import org.apache.commons.io.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {

    public static String readFile(Context ctx, File file) {

        return readFile( file);
    }

    public static String readFile( File file){

        String fileContents = "";
        StringBuilder sb = new StringBuilder();

        try {
            InputStream is = new FileInputStream(file);

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
            LogUtil.logException( FileUtil.class, e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileContents;
    }

    public static void writeFile(File file, String fileContents) {

        try {
            OutputStream out = null;

            FileUtils.forceMkdirParent( file);

            out = new BufferedOutputStream( new FileOutputStream( file));

            out.write(fileContents.getBytes());

            if( out != null)
                out.close();
        }
        catch (IOException e) {
            LogUtil.log( FileUtil.class, "File write failed: " + e.toString());
        }
    }
}
