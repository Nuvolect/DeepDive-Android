package com.nuvolect.deepdive.webserver;//

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.nuvolect.deepdive.main.CConst;
import com.nuvolect.deepdive.util.LogUtil;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

/** RESTfull communication support */
public class Comm {

    private static final boolean VERBOSE = true;
    private static int attempt = 1;

    public static final MediaType MEDIA_TYPE_MARKDOWN
// Fails with a 203 error, bad MD5 test
//    = MediaType.parse("text/x-markdown; charset=utf-8");

// Fails with a 203 error, bad MD5 test
//            = MediaType.parse("text/plain; charset=utf-8");

// Fails with a 203 error, bad MD5 test
//      = MediaType.parse("text/plain; charset=ISO-8859-1");

// exception, unsupported charset exception: x-user-defined
//    = MediaType.parse("application/x-www-form-urlencoded; charset=x-user-defined");

            // Fails with a 203 error, bad MD5 test
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

// fails with error "bad request: content type is multipart/form-data but next chunk does not start with boundary. Use Get /example/fle.html
//    = MediaType.parse("multipart/form-data; charset=utf-8");

    public interface TestConnectionCallbacks {

        void result(boolean reachable);
    }

    public interface CommPostCallbacks {

        void success(String jsonObject);
        void fail(String error);
    }

    /**
     * Send a post message.
     * @param ctx
     * @param url
     * @param params
     * @param listener
     */
    public static void sendPost(Context ctx, String url, final Map<String, String> params,
                                CommPostCallbacks listener) {

        sendPost( url, params, listener);
    }

    /**
     * Send a post message.
     * @param url
     * @param params
     * @param listener
     */
    public static void sendPost(String url, final Map<String, String> params,
                                CommPostCallbacks listener) {

        String postBody = "";
        String amphersand = "";

        OkHttpClient okHttpClient = WebService.getOkHttpClient();

        for (Map.Entry<String, String> entry : params.entrySet()) {

            postBody += amphersand;
            postBody += entry.getKey();
            postBody += "=";
            postBody += entry.getValue();
            amphersand = "&";
        }

        Request request = new Request.Builder()
                .url( url )
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, postBody))
                .header(CConst.SEC_TOK, CrypServer.getSecTok())
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();

            if ( listener != null && response.isSuccessful()){

                listener.success( response.body().string());
            }else{

                if( listener != null)
                    listener.fail("Unexpected code "+response);
            };

            /**
             * Causes exception, can only read the response body 1 time
             * https://github.com/square/okhttp/issues/1240
             */
//            LogUtil.log(LogUtil.LogType.REST, response.body().string());

        } catch (IOException e) {
            listener.fail("IOException: "+e.getLocalizedMessage());
            e.printStackTrace();
        }
    }


    public static void sendPostUi(final Context ctx, String url, final Map<String, String> params,
                                  final CommPostCallbacks cb) {

        String postBody = "";
        String amphersand = "";

        OkHttpClient okHttpClient = WebService.getOkHttpClient();

        for (Map.Entry<String, String> entry : params.entrySet()) {

            postBody += amphersand;
            postBody += entry.getKey();
            postBody += "=";
            postBody += entry.getValue();
            amphersand = "&";
        }

        Request request = new Request.Builder()
                .url( url )
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, postBody))
                .header(CConst.SEC_TOK, CrypServer.getSecTok())
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            Handler mainHandler = new Handler(ctx.getMainLooper());

            @Override
            public void onFailure(Request request, IOException e) {

                LogUtil.log("onFailure");
                LogUtil.logException(LogUtil.LogType.REST, e);
            }

            @Override
            public void onResponse(Response response) throws IOException {

                final String responseStr;
                final boolean success = response.isSuccessful();

                if( success )
                    responseStr = response.body().string();
                else
                    responseStr = response.message();

                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                            if ( ! success) {
                                if( cb != null)
                                    cb.fail("unexpected code "+ responseStr);
                                return;
                            }
                            if( cb != null)
                                cb.success(responseStr);

                    }
                });

            }
        });
    }

    /**
     * Check if a url is reachable.  Runs on a non-UI thread.
     * @param ip
     * @param listener result true, url is reachable.
     */
    public static void testConnection(final String ip, final TestConnectionCallbacks listener) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    boolean reachable = InetAddress.getByName(ip).isReachable(CConst.IP_TEST_TIMEOUT_MS);
                    listener.result( reachable);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Check if url is reachable.  Runs on UI tread.
     * @param ip
     * @param listener
     */
    public static void testConnectionOnUi(final String ip, final TestConnectionCallbacks listener) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    final boolean reachable = InetAddress.getByName(ip).isReachable(CConst.IP_TEST_TIMEOUT_MS);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            listener.result( reachable);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
