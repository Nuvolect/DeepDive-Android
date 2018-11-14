/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.probe;

import android.support.annotation.NonNull;

import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class ProgressStream extends OutputStream {

    private JSONArray m_array;
    private PrintWriter m_printWriter = null;

    public ProgressStream() {

        synchronized (ProgressStream.class) {

            m_array = new JSONArray();
        }
    }

    /**
     * Create a ProgressStream and append the stream to a log file.
     * @param log
     */
    public ProgressStream(OmniFile log) {

        FileWriter fw = null;
        try {
            fw = new FileWriter( log.getStdFile(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = new BufferedWriter(fw);
        m_printWriter = new PrintWriter(bw);

        synchronized (ProgressStream.class) {

            m_array = new JSONArray();
        }
    }

    public void init(){

        synchronized (ProgressStream.class) {

            m_array = new JSONArray();
        }
    }

    public void close(){

        if( m_printWriter != null ){

            m_printWriter.flush();
            m_printWriter.close();
            m_printWriter = null;
        }
    }

    /**
     * Get the stream and return it in reverse order, the newest entry is
     * at [0] or at the top.
     * @return
     */
    public JSONArray getStream(){

        synchronized (ProgressStream.class){

            JSONArray arrayCopy = new JSONArray();
            try {
                for( int i=m_array.length()-1; i>=0; i--)
                    arrayCopy.put( m_array.get(i));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            m_array = new JSONArray();
            return arrayCopy;
        }
    }

    public void putStream(String addString){

        synchronized (ProgressStream.class) {

            m_array.put(addString);

            if( m_printWriter != null){

                m_printWriter.println( addString);
            }
        }
    }

    public void write(@NonNull byte[] data, int i1, int i2) {
        String str = new String(data).trim();
        str = str.replace("\n", "").replace("\r", "");
        str = str.replace("INFO:", "").replace("ERROR:", "").replace("WARN:","");
        str = str.replace("\n\r", "");
        str = str.replace("... done", "").replace("at","");
        str = str.trim();
        if (!str.equals("")) {
            LogUtil.log(LogUtil.LogType.DECOMPILE, str);

            putStream( str);

//                Log.i("PS",str);
//                broadcastStatus("progress_stream", str);mkk
        }
    }

    @Override
    public void write(int arg0) throws IOException {
    }
}
