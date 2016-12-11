package com.nuvolect.deepdive.webserver.connector;//

import android.content.Context;

import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniFiles;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * test
 *
 * Run a specific test and return the result
 */
public class CmdTest {

    private static Context m_ctx;
    private static long testFileSize = 10 * 1024 * 1024;

    enum TEST_ID {
        write_sdcard_test_file,
        write_crypto_test_file,
        read_sdcard_test_file,
        read_crypto_test_file,
        duplicate_crypto_test_file,
        duplicate_sdcard_test_file,
        copy_crypto_file_to_sdcard,
        copy_sdcard_file_to_crypto,
        delete_sdcard_test_file,
        delete_crypto_test_file,
        delete_sdcard_test_folder,
        delete_crypto_test_folder,
    }

    public static ByteArrayInputStream go(Context ctx, Map<String, String> params) {

        m_ctx = ctx;

        try {
            JSONObject wrapper = new JSONObject();

            String error = "";

            TEST_ID test_id = null;
            try {
                test_id = TEST_ID.valueOf(params.get("test_id"));
            } catch (IllegalArgumentException e) {
                error = "Error, invalid command: "+params.get("cmd");
            }
            long timeStart = System.currentTimeMillis();

            assert test_id != null;

            try {
                switch ( test_id ){

                    case write_sdcard_test_file:
                        write_sdcard_test_file();
                        break;
                    case write_crypto_test_file:
                        write_crypto_test_file();
                        break;
                    case read_sdcard_test_file:
                        read_sdcard_test_file();
                        break;
                    case read_crypto_test_file:
                         read_crypto_test_file();
                        break;
                    case duplicate_crypto_test_file:
                         duplicate_crypto_test_file();
                        break;
                    case duplicate_sdcard_test_file:
                         duplicate_sdcard_test_file();
                        break;
                    case copy_crypto_file_to_sdcard:
                         copy_crypto_file_to_sdcard();
                        break;
                    case copy_sdcard_file_to_crypto:
                         copy_sdcard_file_to_crypto();
                        break;
                    case delete_sdcard_test_file:
                        delete_sdcard_test_file();
                        break;
                    case delete_crypto_test_file:
                        delete_crypto_test_file();
                        break;
                    case delete_sdcard_test_folder:
                        delete_sdcard_test_folder();
                        break;
                    case delete_crypto_test_folder:
                        delete_crypto_test_folder();
                        break;
                    default:
                        error = "Invalid test: "+test_id;
                }
            } catch (Exception e) {
                error = "Exception";
            }

            wrapper.put("error", error);
            wrapper.put("test_id", test_id.toString());
            wrapper.put("delta_time",
                    String.valueOf(System.currentTimeMillis() - timeStart) + " ms");

            return new ByteArrayInputStream(wrapper.toString(2).getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void delete_crypto_test_folder() {
        String path = VolUtil.getRoot(VolUtil.cryptoVolumeId)+"_pvtest";
        OmniFile file = new OmniFile(VolUtil.cryptoVolumeId, path);
        CmdRm.delete(m_ctx, file);
    }

    private static void delete_sdcard_test_folder() {
        String path = VolUtil.getRoot(VolUtil.sdcardVolumeId)+"_pvtest";
        OmniFile file = new OmniFile(VolUtil.sdcardVolumeId, path);
        CmdRm.delete(m_ctx, file);
    }

    private static void delete_crypto_test_file() {
        String path = VolUtil.getRoot(VolUtil.cryptoVolumeId)+"_pvtest/test.bin";
        OmniFile file = new OmniFile(VolUtil.cryptoVolumeId, path);
        file.delete();
    }

    private static void delete_sdcard_test_file() {
        String path = VolUtil.getRoot(VolUtil.sdcardVolumeId)+"_pvtest/test.bin";
        OmniFile file = new OmniFile(VolUtil.sdcardVolumeId, path);
        file.delete();
    }

    private static void copy_sdcard_file_to_crypto() {

        String inPath = VolUtil.getRoot(VolUtil.sdcardVolumeId)+"_pvtest/test.bin";
        OmniFile in = new OmniFile(VolUtil.sdcardVolumeId, inPath);

        String outPath = VolUtil.getRoot(VolUtil.cryptoVolumeId)+"_pvtest/~test.bin";
        OmniFile out = new OmniFile(VolUtil.cryptoVolumeId, outPath);

        OmniFiles.copyFile(in, out);
    }

    private static void copy_crypto_file_to_sdcard() {

        String inPath = VolUtil.getRoot(VolUtil.cryptoVolumeId)+"_pvtest/test.bin";
        OmniFile in = new OmniFile(VolUtil.cryptoVolumeId, inPath);

        String outPath = VolUtil.getRoot(VolUtil.sdcardVolumeId)+"_pvtest/~test.bin";
        OmniFile out = new OmniFile(VolUtil.sdcardVolumeId, outPath);

        OmniFiles.copyFile(in, out);
    }

    private static void duplicate_sdcard_test_file() {
        String inPath = VolUtil.getRoot(VolUtil.sdcardVolumeId)+"_pvtest/test.bin";
        OmniFile in = new OmniFile(VolUtil.sdcardVolumeId, inPath);
        String outPath = VolUtil.getRoot(VolUtil.sdcardVolumeId)+"_pvtest/duplicate.bin";
        OmniFile out = new OmniFile(VolUtil.sdcardVolumeId, outPath);
        OmniFiles.copyFile( in, out);
    }

    private static void duplicate_crypto_test_file() {
        String inPath = VolUtil.getRoot(VolUtil.cryptoVolumeId)+"_pvtest/test.bin";
        OmniFile in = new OmniFile(VolUtil.cryptoVolumeId, inPath);
        String outPath = VolUtil.getRoot(VolUtil.cryptoVolumeId)+"_pvtest/duplicate.bin";
        OmniFile out = new OmniFile(VolUtil.cryptoVolumeId, outPath);
        OmniFiles.copyFile( in, out);
    }

    private static void read_crypto_test_file() throws IOException {
        String path = VolUtil.getRoot(VolUtil.cryptoVolumeId)+"_pvtest/test.bin";
        OmniFile file = new OmniFile(VolUtil.cryptoVolumeId, path);
        file.getParentFile().mkdirs();
        OmniFiles.countBytes( file );
    }

    private static void read_sdcard_test_file() throws IOException {
        String path = VolUtil.getRoot(VolUtil.sdcardVolumeId)+"_pvtest/test.bin";
        OmniFile file = new OmniFile(VolUtil.sdcardVolumeId, path);
        OmniFiles.countBytes( file );
    }
    private static void write_crypto_test_file() throws IOException {
        String path = VolUtil.getRoot(VolUtil.cryptoVolumeId)+"_pvtest/test.bin";
        OmniFile file = new OmniFile(VolUtil.cryptoVolumeId, path);
        file.getParentFile().mkdirs();
        OmniFiles.createFile( file, testFileSize);
    }

    private static void write_sdcard_test_file() throws IOException {
        String path = VolUtil.getRoot(VolUtil.sdcardVolumeId)+"_pvtest/test.bin";
        OmniFile file = new OmniFile(VolUtil.sdcardVolumeId, path);
        file.getParentFile().mkdirs();
        OmniFiles.createFile( file, testFileSize);
    }
}
