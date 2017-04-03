package com.nuvolect.deepdive.webserver.connector;//

import android.content.Context;

import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniHash;
import com.nuvolect.deepdive.util.Omni;
import com.nuvolect.deepdive.webserver.MimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * test
 *
 * Run a specific test and return the result
 * Input JSON
 * {
 *     "test_id": {decode_uri, encode_uri}
 * }
 * JSON returned
 * {
 *     "error":"",      // empty if no error
 *     "result":"",     // results of test
 *     "test_id":"",    // test id that was executed
 *     "delta_time":"", // String time appended with " ms"
 * }
 */
public class CmdDebug {

    enum TEST_ID {
        decode_hash,
        encode_hash,
        mime,
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

                    case decode_hash:{

                        String result = decode_hash(params.get("data"));
                        wrapper.put("result",result);
                        break;
                    }
                    case encode_hash:{

                        String result = encode_hash(params.get("data"));
                        wrapper.put("result",result);
                        break;
                    }
                    case mime:{

                        String result = MimeUtil.getMime(params.get("data"));
                        wrapper.put("result",result);
                        break;
                    }
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

            if(LogUtil.DEBUG)
                LogUtil.log(LogUtil.LogType.CMD_DEBUG, wrapper.toString(2));

            return new ByteArrayInputStream(wrapper.toString(2).getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String decode_hash(String data) {

        String hash = data;
        if( Omni.matchVolume( data)){

            hash = Omni.getPathFromUri( data);
        }

        return OmniHash.decode( hash );
    }

    private static String encode_hash(String data) {

        return OmniHash.encode( data );
    }

}
