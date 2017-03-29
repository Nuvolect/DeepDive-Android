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

package com.nuvolect.deepdive.webserver.connector;//

import android.content.Context;

import com.nuvolect.deepdive.main.CConst;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniFiles;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.regex.Pattern;

import static com.nuvolect.deepdive.util.LogUtil.logException;

/**
 * <pre>
 * Process file upload requests. Client may request the upload of multiple files at once.
 *
 * Arguments (HTTP POST):
 *
 * cmd : upload
 *> target : hash of the directory to upload
 *> upload[] : array of multipart files to upload
 *> upload_path[] : array of target directory hash, it has been a pair with upload[]. (specified at folder upload)
 *> mtime[] : array of files UNIX time stamp, it has been a pair with upload[].
 *> renames[] : array of rename request filenames
 *> suffix : rename suffix
 *> hashes[hash] : array of hash: filename pairs
 *
 * Response: An array of successfully uploaded files if success, an error otherwise.
 *
 *> added : (Array) of files that were successfully uploaded. Information about File/Directory
 *
 * If the files could not be uploaded, also return warning.
 *
 *> warning : (Array) of error messages like a errors
 *
 * Chunked uploads
 *
 * Chunking Extra arguments:
 *
 *> chunk : chunk name "filename.[NUMBER]_[TOTAL].part"
 *> cid : unique id of chunked uploading file
 *> range : Bytes range of file "Start byte,Chunk length,Total bytes"
 *
 * Response:
 *
 *> added : (Array) empty array
 *> _chunkmerged : (String) file name of server side.　ONLY when the upload of all the chunk file has been completed.
 *> _name : (String) uploading file name. 　ONLY when the upload of all the chunk file has been completed.
 *
 * Chunk merge request (When receive _chunkmerged, _name)
 *
 * Extra arguments:
 *
 *> upload[] : Value of _name
 *> chunk : Value of _chunkmerged
 *
 * Response:
 *
 *> added : (Array) of files that were successfully uploaded. Information about File/Directory
 *
 * POST DATA:
 * ------WebKitFormBoundary79xNBbwg7czVfc7l
 Content-Disposition: form-data; name="cmd"

 upload
 ------WebKitFormBoundary79xNBbwg7czVfc7l
 Content-Disposition: form-data; name="target"

 l2_Lw
 ------WebKitFormBoundary79xNBbwg7czVfc7l
 Content-Disposition: form-data; name="upload[]"; filename="Very Nice.txt"
 Content-Type: text/plain

 With nice content.
 ------WebKitFormBoundary79xNBbwg7czVfc7l--

 Files over 10MB are chunked

 0 = {java.util.HashMap$HashMapEntry@5093} "cmd" -> "upload"
 1 = {java.util.HashMap$HashMapEntry@5094} "chunk" -> "Kepler jumpy puppy.mp4.0_4.part"
 2 = {java.util.HashMap$HashMapEntry@5095} "target" -> "l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC9Nb3ZpZXMvS2VwbGVy"
 3 = {java.util.HashMap$HashMapEntry@5096} "cid" -> "1463563961531"
 4 = {java.util.HashMap$HashMapEntry@5097} "upload[]" -> "blob"
 5 = {java.util.HashMap$HashMapEntry@5098} "upload_path[]" ->
 6 = {java.util.HashMap$HashMapEntry@5099} "range" -> "0,10485760,47858929"
 7 = {java.util.HashMap$HashMapEntry@5100} "queryParameterStrings" -> "null"

 0 = {java.util.HashMap$HashMapEntry@5124} "cmd" -> "upload"
 1 = {java.util.HashMap$HashMapEntry@5125} "chunk" -> "Kepler jumpy puppy.mp4.1_4.part"
 2 = {java.util.HashMap$HashMapEntry@5126} "target" -> "l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC9Nb3ZpZXMvS2VwbGVy"
 3 = {java.util.HashMap$HashMapEntry@5127} "cid" -> "1463563961531"
 4 = {java.util.HashMap$HashMapEntry@5128} "upload[]" -> "blob"
 5 = {java.util.HashMap$HashMapEntry@5129} "upload_path[]" ->
 6 = {java.util.HashMap$HashMapEntry@5130} "range" -> "10485760,10485760,47858929"
 7 = {java.util.HashMap$HashMapEntry@5131} "queryParameterStrings" -> "null"

 0 = {java.util.HashMap$HashMapEntry@5156} "cmd" -> "upload"
 1 = {java.util.HashMap$HashMapEntry@5157} "chunk" -> "Kepler jumpy puppy.mp4.3_4.part"
 2 = {java.util.HashMap$HashMapEntry@5158} "target" -> "l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC9Nb3ZpZXMvS2VwbGVy"
 3 = {java.util.HashMap$HashMapEntry@5159} "cid" -> "1463563961531"
 4 = {java.util.HashMap$HashMapEntry@5160} "upload[]" -> "blob"
 5 = {java.util.HashMap$HashMapEntry@5161} "upload_path[]" ->
 6 = {java.util.HashMap$HashMapEntry@5162} "range" -> "31457280,10485760,47858929"
 7 = {java.util.HashMap$HashMapEntry@5163} "queryParameterStrings" -> "null"

 0 = {java.util.HashMap$HashMapEntry@5188} "cmd" -> "upload"
 1 = {java.util.HashMap$HashMapEntry@5189} "chunk" -> "Kepler jumpy puppy.mp4.4_4.part"
 2 = {java.util.HashMap$HashMapEntry@5190} "target" -> "l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC9Nb3ZpZXMvS2VwbGVy"
 3 = {java.util.HashMap$HashMapEntry@5191} "cid" -> "1463563961531"
 4 = {java.util.HashMap$HashMapEntry@5192} "upload[]" -> "blob"
 5 = {java.util.HashMap$HashMapEntry@5193} "upload_path[]" ->
 6 = {java.util.HashMap$HashMapEntry@5194} "range" -> "41943040,5915889,47858929"
 7 = {java.util.HashMap$HashMapEntry@5195} "queryParameterStrings" -> "null"

 0 = {java.util.HashMap$HashMapEntry@5222} "cmd" -> "upload"
 1 = {java.util.HashMap$HashMapEntry@5223} "chunk" -> "Kepler jumpy puppy.mp4.2_4.part"
 2 = {java.util.HashMap$HashMapEntry@5224} "target" -> "l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC9Nb3ZpZXMvS2VwbGVy"
 3 = {java.util.HashMap$HashMapEntry@5225} "cid" -> "1463563961531"
 4 = {java.util.HashMap$HashMapEntry@5226} "upload[]" -> "blob"
 5 = {java.util.HashMap$HashMapEntry@5227} "upload_path[]" ->
 6 = {java.util.HashMap$HashMapEntry@5228} "range" -> "20971520,10485760,47858929"
 7 = {java.util.HashMap$HashMapEntry@5229} "queryParameterStrings" -> "null"



 * </pre>
 */
public class CmdUpload {

    private static boolean DEBUG = LogUtil.DEBUG;
    /**
     * Keep a list of all chunks that have been uploaded.
     * For multi-file upload, chunks can be mixed from different files.
     * Chunks arrive in random order.
     * Count chunks matching the base filename to know when all chunks
     * are uploaded for a specific file.
     */
    private static JSONObject m_fileUploads;
    private static String m_chunkDirPath;

    public static synchronized void init(Context ctx) {

        /**
         * An object keyed by filename holds an objects of file chunks.
         * fileUploads
         *     filename1
         *         chunk_0_5
         *         chunk_3_5
         *     filename2
         *         chunk_2_5
         *         chunk_4_5
         */
        m_fileUploads = new JSONObject();

        m_chunkDirPath = ctx.getFilesDir() + CConst.CHUNK;

        clearChunkFiles( ctx);
    }

    private static void clearChunkFiles(Context ctx){

        String chunkDirPath = ctx.getFilesDir() + CConst.CHUNK;
        File chunkDir = new File( m_chunkDirPath);
        chunkDir.mkdirs();
        File[] files = chunkDir.listFiles();

        for(File file : files){

            file.delete();
        }
    }

    public static synchronized ByteArrayInputStream go(Context ctx, Map<String, String> params) {

        String httpIpPort = params.get("url");
        String target = params.get("target");
        String error = "";

        // Get file to target directory
        OmniFile targetDirectory = new OmniFile(target);
        if (!targetDirectory.isDirectory())
            error = "Unable to upload files";

        String destPath = targetDirectory.getPath();
        String targetVolumeId = targetDirectory.getVolumeId();
        JSONArray added = new JSONArray();
        JSONObject wrapper = new JSONObject();

        /**
         * Non-chucked files are uploaded in a single upload.
         * Chunked files are broken into parts.
         */
        if (!params.containsKey("chunk")) {

            /**
             * Parse the uploads array and copy from temporary storage each
             * file to the destination folder.
             */
            try {
                JSONArray postUploads = null;
                postUploads = new JSONArray(params.get("post_uploads"));

                for (int i = 0; i < postUploads.length(); i++) {

                    JSONObject postUpload = postUploads.getJSONObject(i);
                    String uploadFileName = postUpload.getString(CConst.FILE_NAME);
                    String filePath = postUpload.getString(CConst.FILE_PATH);

                    File srcFile = new File(filePath);
                    OmniFile destFile = new OmniFile(targetVolumeId, destPath + "/" + uploadFileName);

                    error = OmniFiles.copyFile(srcFile, destFile) ? "" : "File copy failure";
                    if (!error.isEmpty())
                        break;

                    JSONObject fileObj = FileObj.makeObj(targetVolumeId, destFile, httpIpPort);
                    added.put(fileObj);
                    LogUtil.log(LogUtil.LogType.CMD_UPLOAD, "File upload success: " + destFile.getPath());
                }

                wrapper.put("added", added);

                if (!error.isEmpty()) {

                    JSONArray warning = new JSONArray();
                    JSONObject errorObj = new JSONObject();
                    errorObj.put("error", error);
                    warning.put(errorObj);
                    wrapper.put("warning", warning);
                }

                return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

            } catch (JSONException | UnsupportedEncodingException e) {
                logException(CmdUpload.class, e);
            }

        }
/**
 * Collect chunk data until last chuck is received, then assemble the uploaded file.
 *
 * EXAMPLE, 60.3MB file:
 * "cmd" -> "upload"
 * "mtime[]" -> "1489684754"
 * "cid" -> "697767115"
 * "upload_path[]" -> "l0_L3Rlc3QvdG1w"
 * "range" -> "0,10485760,60323475"
 x "post_uploads" -> "[{"file_path":"\/data\/user\/0\/com.nuvolect.deepdive.debug\/cache\/NanoHTTPD-340250228","file_name":"blob"}]"
 * "dropWith" -> "0"
 * "chunk" -> "kepler_7_weeks.mp4.0_5.part"
 x "target" -> "l0_L3Rlc3QvdG1w"
 * "unique_id" -> "1489174097708"
 * "upload[]" -> "blob"
 * "queryParameterStrings" -> "cmd=ls&target=l0_L3Rlc3QvdG1w&intersect%5B%5D=kepler_7_weeks.mp4&_=1489697705506"
 * "uri" -> "/connector"
 */

        String chunk = "";
        int chunkNumber = 0;
        int chunkMax = 0;
        String[] parts = new String[0];

        chunk = params.get("chunk");
        parts = chunk.split(Pattern.quote("."));
        String countOfMax = parts[parts.length - 2];
        String[] twoNumbers = countOfMax.split("_");
        chunkNumber = Integer.valueOf(twoNumbers[0]);
        chunkMax = Integer.valueOf(twoNumbers[1]);

        String targetFilename = parts[0] + "." + parts[1];
        JSONObject fileChunks = new JSONObject(); // Chunks for the target file

        try {

            /**
             * Parse the uploads array and collect specific of the current chunk.
             * Metadata for each chunk is saved in a JSONObject using the chunk filename as the key.
             * Move each chunk from the app:/cache folder to app:/chunk.
             * When all chunks are uploaded, the target is assembled and chunks deleted.
             */
            JSONArray postUploads = new JSONArray(params.get("post_uploads"));
            error = "";

            for (int i = 0; i < postUploads.length(); i++) {

                JSONObject postUpload = postUploads.getJSONObject(i);
                String cachePath = postUpload.getString(CConst.FILE_PATH); // app: /cache/xxx
                File cacheFile = new File( cachePath);

                String chunkPath = m_chunkDirPath + FilenameUtils.getName( cachePath);
                File chunkFile = new File(chunkPath);

                /**
                 * Move the chunk, otherwise Nanohttpd will delete it.
                 */
                cacheFile.renameTo( chunkFile);

                JSONObject chunkObj = new JSONObject();
                chunkObj.put("filepath", chunkPath);
                chunkObj.put("range", params.get("range"));

                if( m_fileUploads.has( targetFilename)){

                    fileChunks = m_fileUploads.getJSONObject( targetFilename);
                }else{

                    fileChunks = new JSONObject();
                }
                fileChunks.put(chunk, chunkObj);
                m_fileUploads.put( targetFilename, fileChunks);
            }

            /**
             * If not complete, return with intermediate results
             */
            if ( fileChunks.length() <= chunkMax) {

                wrapper.put("added", new JSONArray());
                if (!error.isEmpty()){

                    wrapper.put("error", error);
                    LogUtil.log(LogUtil.LogType.CMD_UPLOAD, "File upload error: " + error);
                }
                return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));
            }

        } catch (JSONException | UnsupportedEncodingException e) {
            error = "Error parsing file chunks";
        }

        if (!error.isEmpty()){

            return null;
        }

        try {

            int totalSize = 0;
            /**
             * All chunks are uploaded.  Iterate over the chunk meta data and assemble the file.
             * Open the target file.
             */
            OmniFile destFile = new OmniFile(targetVolumeId, targetDirectory.getPath() + "/" + targetFilename);
            OutputStream destOutputStream = destFile.getOutputStream();

            for (int i = 0; i <= chunkMax; i++) {

                String chunkKey = targetFilename + "." + i + "_" + chunkMax + ".part";

                if (!fileChunks.has(chunk)) {

                    error = "Missing chunk: " + chunkKey;
                    break;
                }

                JSONObject chunkObj = fileChunks.getJSONObject(chunkKey);
                String chunkPath = chunkObj.getString("filepath");
                File sourceFile = new File(chunkPath);
                if( sourceFile.exists()) {
                    LogUtil.log(LogUtil.LogType.CMD_UPLOAD, "File exists: " + sourceFile.getPath());
                }
                else{
                    LogUtil.log(LogUtil.LogType.CMD_UPLOAD, "File NOT exists: "+sourceFile.getPath());
                    break;
                }

                FileInputStream fis = new FileInputStream(sourceFile);

                //TODO error check range of bytes from each chunk and compare with chunk bytes copied

                /**
                 * Append next chunk to the destination file.
                 */
                int bytesCopied = OmniFiles.copyFileLeaveOutOpen(fis, destOutputStream);

                totalSize += bytesCopied;

                LogUtil.log(LogUtil.LogType.CMD_UPLOAD, "Bytes copied, total: " + bytesCopied + ", " + totalSize);

                // Delete temp file
                if (!sourceFile.delete()) {

                    error = "Delete temp file failed : " + sourceFile.getPath();
                    LogUtil.log(LogUtil.LogType.CMD_UPLOAD, error);
                    break;
                } else {
                    LogUtil.log(LogUtil.LogType.CMD_UPLOAD, "Removed " + sourceFile.getName());
                }
            }
            destOutputStream.flush();
            destOutputStream.close();

            // Done with this file, clean up.
            m_fileUploads.remove( targetFilename);

            if( error.isEmpty()){

                JSONObject fileObj = FileObj.makeObj(targetVolumeId, destFile, httpIpPort);
                added.put(fileObj);
                wrapper.put("added", added);
                LogUtil.log(LogUtil.LogType.CMD_UPLOAD, "File upload success: " + destFile.getPath());

            }else{

                wrapper = Util.errorWrapper( error);
            }

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch ( IOException | JSONException e) {
            logException(CmdUpload.class, e);
            clearChunkFiles( ctx);
        }
        return null;
    }
}
