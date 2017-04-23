package com.nuvolect.deepdive.webserver.admin;//

import android.content.Context;

import com.nuvolect.deepdive.main.App;
import com.nuvolect.deepdive.util.Omni;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniFiles;
import com.nuvolect.deepdive.webserver.connector.CmdRm;

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

    private static final long testFileSize = 10 * 1024 * 1024;

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
                        delete_sdcard_test_folder( ctx);
                        break;
                    case delete_crypto_test_folder:
                        delete_crypto_test_folder( ctx);
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

    private static void delete_crypto_test_folder( Context ctx) {
        String path = "/_pvtest";
        OmniFile file = new OmniFile(Omni.cryptoVolumeId, path);
        CmdRm.delete( ctx, file);
    }

    private static void delete_sdcard_test_folder( Context ctx) {
        String path = "/_pvtest";
        OmniFile file = new OmniFile(App.getUser().getDefaultVolumeId(), path);
        CmdRm.delete( ctx, file);
    }

    private static void delete_crypto_test_file() {
        String path = "/_pvtest/test.bin";
        OmniFile file = new OmniFile(Omni.cryptoVolumeId, path);
        file.delete();
    }

    private static void delete_sdcard_test_file() {
        String path = "/_pvtest/test.bin";
        OmniFile file = new OmniFile(App.getUser().getDefaultVolumeId(), path);
        file.delete();
    }

    private static void copy_sdcard_file_to_crypto() {

        String inPath = "/_pvtest/test.bin";
        OmniFile in = new OmniFile(App.getUser().getDefaultVolumeId(), inPath);

        String outPath = "/_pvtest/~test.bin";
        OmniFile out = new OmniFile(Omni.cryptoVolumeId, outPath);

        OmniFiles.copyFile(in, out);
    }

    private static void copy_crypto_file_to_sdcard() {

        String inPath = "/_pvtest/test.bin";
        OmniFile in = new OmniFile(Omni.cryptoVolumeId, inPath);

        String outPath = "/_pvtest/~test.bin";
        OmniFile out = new OmniFile(App.getUser().getDefaultVolumeId(), outPath);

        OmniFiles.copyFile(in, out);
    }

    private static void duplicate_sdcard_test_file() {
        String inPath = "/_pvtest/test.bin";
        OmniFile in = new OmniFile(App.getUser().getDefaultVolumeId(), inPath);
        String outPath = "/_pvtest/duplicate.bin";
        OmniFile out = new OmniFile(App.getUser().getDefaultVolumeId(), outPath);
        OmniFiles.copyFile( in, out);
    }

    private static void duplicate_crypto_test_file() {
        String inPath = "/_pvtest/test.bin";
        OmniFile in = new OmniFile(Omni.cryptoVolumeId, inPath);
        String outPath = "/_pvtest/duplicate.bin";
        OmniFile out = new OmniFile(Omni.cryptoVolumeId, outPath);
        OmniFiles.copyFile( in, out);
    }

    private static void read_crypto_test_file() throws IOException {
        String path = "/_pvtest/test.bin";
        OmniFile file = new OmniFile(Omni.cryptoVolumeId, path);
        file.getParentFile().mkdirs();
        OmniFiles.countBytes( file );
    }

    private static void read_sdcard_test_file() throws IOException {
        String path = "/_pvtest/test.bin";
        OmniFile file = new OmniFile(App.getUser().getDefaultVolumeId(), path);
        OmniFiles.countBytes( file );
    }
    private static void write_crypto_test_file() throws IOException {
        String path = "/_pvtest/test.bin";
        OmniFile file = new OmniFile(Omni.cryptoVolumeId, path);
        file.getParentFile().mkdirs();
        OmniFiles.createFile( file, testFileSize);
    }

    private static void write_sdcard_test_file() throws IOException {
        String path = "/_pvtest/test.bin";
        OmniFile file = new OmniFile(App.getUser().getDefaultVolumeId(), path);
        file.getParentFile().mkdirs();
        OmniFiles.createFile( file, testFileSize);
    }
}
