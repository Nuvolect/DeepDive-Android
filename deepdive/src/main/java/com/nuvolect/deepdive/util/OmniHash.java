package com.nuvolect.deepdive.util;//


import android.content.Context;
import android.util.Base64;

import com.nuvolect.deepdive.webserver.WebUtil;

import java.io.UnsupportedEncodingException;

/**
 * All URLs used by elFinder are hashed. Methods are provided to deal with hashing.
 *
 * For executing most of the commands, passing files path is required to determine
 * which files/directories to act upon. Paths are NEVER passed in clear text format.
 * Passing paths in clear text format will lead to unwanted disclosure of information
 * and can stop client side from working at all. All paths between client and connector
 * are passed in encrypted format called hash.
 *
 * Old 1.0 API used MD5 for encryption - it made connector more complicated and caused
 * problems with performance, so it's not recommended to use it anymore.
 *
 * New 2.x PHP connector uses the following algorithm to create hash from file path:
 *
 * a. remove root path from file path
 * b. encrypt resulting path so it could be later decrypted (not implemented yet, but stub is present)
 * c. encode already encrypted path using base64 with replacement +/= -> -_.
 * d. remove trailing dots
 * e. add prefix - unique volume id (must start with [a-z])
 * f. resulting string must be valid HTML id attribute (that is why base64 is used).
 * g. Using this algorithm even without encryption, client cannot get real file paths on the
 * h. server only relative to root paths. This hash algorithm is recommended but you can
 * i. use your own implementation so long as it matches these 2 rules:
 *
 * 1. hash must be valid for storage in the id attribute of an HTML tag
 * 2. hash must be reversible by connector
 *
 * ----- Custom for Nuvolect Apps
 * The prefix in step e. is:
 * public static String sdcardVolumeId = "l0_"; // Local Volume 0
 * public static String cryptoVolumeId = "c0_"; // Encrypted Volume 0
 */
public class OmniHash {

    /**
     * Encode a filename.
     * @param clearText
     * @return
     */
    public static String encode(String clearText) {

        String encoded = Base64.encodeToString(clearText.getBytes(), Base64.NO_WRAP);
        encoded = encoded.replace('+','-');// order is important
        encoded = encoded.replace('/','.');
        encoded = encoded.replace("=","");  // remove, not required

        return encoded;
    }

    /**
     * Decode file/directory hash
     * @param hash
     * @return
     */
    public static String decode(String hash){

        hash = hash.replace('-','+'); // Reverse encoding substitutions
        hash = hash.replace('.','/');

        String decoded = null;
        try {
            decoded = new String( Base64.decode(hash, Base64.DEFAULT), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return decoded;
    }

    /**
     * Return the volume hash by passing the the volume and a path.
     * @param volumeId
     * @param path
     * @return
     */
    public static String getVolumeHash(String volumeId, String path) {

        String pathWithRoot = path.replace("//","/");
        return volumeId + encode(pathWithRoot);
    }

    /**
     * Given a path such as /deepdive, return a URL with a hashed path
     * that can be pasted into a browser, assuming authentication is in place.
     * @param ctx
     * @param path
     * @return
     */
    public static String getHashedServerUrl(Context ctx, String volumeId, String path){

        String hashedUrl = OmniHash.getVolumeHash(
                volumeId,
                path);
        String url = WebUtil.getServerUrl(ctx)
                +CConst.ELFINDER_PAGE
                +"#"
                +CConst.ELF_
                +hashedUrl;
        return url;
    }
}
