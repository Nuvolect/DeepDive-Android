package com.nuvolect.deepdive.webserver.connector;//

import com.nuvolect.deepdive.ddUtil.CConst;
import com.nuvolect.deepdive.ddUtil.LogUtil;
import com.nuvolect.deepdive.ddUtil.Omni;
import com.nuvolect.deepdive.ddUtil.OmniFile;
import com.nuvolect.deepdive.main.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * <pre>
 * Returns information about requested directory and its content,
 * optionally can return directory tree as files, and options for the current volume.
 *
 * volume(root) "files" object:
 *     {
 * +     "csscls": "elfinder-navbar-root-local",
 *       "dirs": 1,
 * +     "disabled": [ "chmod" ],
 *       "hash": "l1_Lw",
 *       "isowner": false,
 *       "locked": 1,
 *       "mime": "directory",
 *       "name": "Demo",
 *       "read": 1,
 *       "size": 0,
 *       "ts": 1453299075,
 * +     "tmbUrl": "http://hypweb.net/elFinder-nightly/demo/Demo/.tmb/"
 * +     "uiCmdMap": [],
 *       "volumeid": "l1_",
 *       "write": 0,
 *     },
 * cwd object is just like a volume file with the addition of "root"
 *   "cwd":
 *     {
 *       "csscls": "elfinder-navbar-root-local",
 *       "dirs": 1,
 *       "disabled": [ "chmod" ],
 *       "hash": "l1_Lw",
 *       "isowner": false,
 *       "locked": 1,
 *       "mime": "directory",
 *       "name": "Demo",
 *       "read": 1,
 * +     "root": "l1_Lw",
 *       "size": 0,
 *       "tmbUrl": "http://hypweb.net/elFinder-nightly/demo/Demo/.tmb/",
 *       "ts": 1453299075,
 *       "uiCmdMap": [],
 *       "volumeid": "l1_",
 *       "write": 0
 *     },
 * directory "files" object:
 *     {
 * +     "dirs": 1,
 *       "hash": "l1_V2VsY29tZQ",
 * +     "icon": "http://hypweb.net/elFinder-nightly/demo/Demo/Welcome/.diricon.png",
 *       "isowner": false,
 *       "locked": 1,
 *       "mime": "directory",
 *       "name": "Welcome",
 *       "phash": "l1_Lw",
 *       "read": 1,
 *       "size": 0,
 *       "ts": 1458097231,
 * +     "volumeid": "l1_",
 *       "write": 0
 *     },
 * file "files" object:
 *     {
 *       "hash": "l1_UkVBRE1FLm1k",
 *       "isowner": false,
 *       "locked": 1,
 *       "mime": "text/x-markdown",
 *       "name": "README.md",
 *       "phash": "l1_Lw",
 *       "read": 1,
 *       "size": "3683",
 *       "ts": 1418091234,
 *       "write": 0
 *     }
 * </pre>
 */
public class CmdOpen {

    static boolean DEBUG = true; //LogUtil.DEBUG;

    public static ByteArrayInputStream go(Map<String, String> params) {

        long startTime = System.currentTimeMillis();
        /**
         init : (true|false|not set), optional parameter.
         If true indicates that this request is an initialization request and its response must
         include the value api (number or string >= 2) and should include the options object,
         but will still work without it.

         Also, this option affects the processing of parameter target hash value.
         If init == true and target is not set or that directory doesn't exist,
         then the data connector must return the root directory of the default volume.
         Otherwise it must return error "File not found".

         target : (string) Hash of directory to open. Required if init == false or init is not set.

         tree : (true|false), optional. If true, response must contain subfolders trees of roots directories.
         */
        boolean init = false;
        if( params.containsKey("init"))
            init = params.get("init").contentEquals("1");

        /**
         * target : (string) Hash of directory to open. Required if init == false or init is not set
         */
        OmniFile targetFile;
        String target = "";
        if( params.containsKey("target"))
            target = params.get("target");

        /**
         * tree : (true|false), optional. If true, response must contain subfolders trees of roots directories.
         */
        boolean tree = false;
        if( params.containsKey("tree"))
            tree = params.get("tree").contentEquals("1");

        String httpIpPort = params.get("url");

        String volumeId = "";
        JSONObject wrapper = new JSONObject();
        JSONArray uiCmdMap = new JSONArray();
        /**
         * files : (Array) array of objects - files and directories in current directory.
         * If parameter tree == true, then added to the folder of the directory tree to a given depth.
         * The order of files is not important.
         *
         * Note you must include the top-level volume objects here as well
         * (i.e. cwd is repeated here, in addition to other volumes) Information * about File/Directory
         */
        JSONArray fileObjects;

        try {

            if( init ){

                /**
                 * api : (Number) The version number of the protocol, must be >= 2.1,
                 * ATTENTION - return api ONLY for init request!
                 */
                wrapper.put("api","2.1");
            }

            /**
             * An empty target defaults to the root of the default volume otherwise
             * a non-empty target uses a hashed file volume and path.
             * The path starts with the volume appended with an encoded path.
             */
            if( target.isEmpty()){
                volumeId = App.getUser().getDefaultVolumeId();
                targetFile = new OmniFile(volumeId, CConst.ROOT);
                if( DEBUG)
                    LogUtil.log(LogUtil.LogType.CMD_OPEN,"Target empty: "+targetFile.getPath());
            }else {
                /**
                 * A non-empty target is a hashed path starting with with the volume
                 * followed by a encoded relative path.
                 */
                targetFile = new OmniFile( target );
                volumeId = targetFile.getVolumeId();
                if( DEBUG)
                    LogUtil.log(LogUtil.LogType.CMD_OPEN,"Target: "+targetFile.getPath());
            }
            /**
             * Add files that are in the target directory
             *
             * files : (Array) array of objects - files and directories in current directory.
             * If parameter tree == true, then added to the folder of the directory tree to a given depth.
             * The order of files is not important.
             *
             * Note you must include the top-level volume objects here as well (i.e. cwd is repeated here,
             * in addition to other volumes) Information about File/Directory
             */
            fileObjects = targetFile.listFileObjects(httpIpPort);

            /**
             * The current working directory is always a directory and never a file.
             * If the target is a file the cwd is the file's parent directory.
             */
            JSONObject cwd;
            if( targetFile.isDirectory())
                cwd = FileObj.makeObj( volumeId, targetFile, httpIpPort);
            else
                cwd = FileObj.makeObj( volumeId, targetFile.getParentFile(), httpIpPort);

            // cwd is like a volume file with the addition of the root element
            cwd.put("root", cwd.get("hash"));
            wrapper.put("cwd", cwd);

            if( DEBUG)
                LogUtil.log(LogUtil.LogType.CMD_OPEN,
                    "CWD  name: "+targetFile.getName()
                    +", path: "+targetFile.getPath()
                    +", hash: "+targetFile.getHash());

            /**
             * Add additional file volumes
             */

            if( tree){

                String volumeIds[] = Omni.getActiveVolumeIds();

                for(String thisVolumeId : volumeIds){

                    OmniFile thisRootFile = new OmniFile( thisVolumeId, CConst.ROOT);
                    JSONObject thisRootFileObject = thisRootFile.getFileObject(httpIpPort);
                    // Only the root objects get this
                    thisRootFileObject.put("csscls", "elfinder-navbar-root-local");

                    // Add the root volume
                    fileObjects.put( thisRootFileObject);

                    /**
                     * For each volume, get objects for each directory 1 level deep
                     */
                    OmniFile[] files = thisRootFile.listFiles();

                    for( OmniFile file : files){

                        if( file.isDirectory())
                            fileObjects.put( file.getFileObject(httpIpPort));
                    }
                }
            }

            wrapper.put("files", fileObjects);

            /**
             * Optional
             */
            wrapper.put("uplMaxSize", "100M");
            wrapper.put("uplMaxFile", "100");

            // Remove leading slash from the path
            JSONObject options = new JSONObject();
            String path = targetFile.getPath();
            if( path.startsWith("/"))
                path = path.substring(1);
            options.put("path", path);

            /**
             * Using the optional 'url' creates odd results.
             * From elFinder, Get info, link: produces a link combining the hashed path and a clear text filename
             *
             * Omitting the 'url' prodduces a link that is only a hashed url and this is desired,
             * So don't use the 'url' option.
             */
//            options.put("url", httpIpPort +"/"+ targetFile.getHash()+"/");
            /**
             * Normally the client uses this URL as a base to fetch thumbnails,
             * a clear text filename would be added to perform a GET.
             * This works fine for single volume, however to support multiple volumes
             * the volumeId is required. We just set the url to root and use the
             * same volumeId+hash system to fetch thumbnails.
             */
            options.put("tmbUrl", httpIpPort + "/");

            options.put("separator", CConst.SLASH);
//            options.put("dispInlineRegex","^(?:(?:image|text)|application/x-shockwave-flash$)");
            options.put("dispInlineRegex","^(?:image|text/plain$)");
            JSONArray disabled = new JSONArray();
            options.put("disabled",disabled);
            options.put("copyOverwrite",1);
            options.put("uploadOverwrite",1);
            options.put("uploadMaxSize", 2000000000); // 2GB
            options.put("jpgQuality",100);

            /**
             * Of the archivers, "create" and "extract" are JSONArray.
             * "createext" is a JSONObject that also provides a file extension.
             */
            JSONObject archivers = new JSONObject();
            JSONArray create = new JSONArray();
//            create.put("application/x-tar");
//            create.put("application/x-gzip");
//            create.put("application/x-bzip2");
//            create.put("application/x-xz");
            create.put("application/zip");
//            create.put("application/x-7z-compressed");

            JSONObject createext = new JSONObject();
//            createext.put("application/x-tar", "tar");
//            createext.put("application/x-gzip", "tgz");
//            createext.put("application/x-bzip2", "tbz");
//            createext.put("application/x-xz", "xz");
            createext.put("application/zip", "zip");
//            createext.put("application/x-7z-compressed", "7z");

            JSONArray extract = new JSONArray();
//            extract.put("application/x-tar");
//            extract.put("application/x-gzip");
//            extract.put("application/x-bzip2");
//            extract.put("application/x-xz");
            extract.put("application/zip");
//            extract.put("application/x-7z-compressed");

            archivers.put("create",create);
            archivers.put("createext",createext);
            archivers.put("extract",extract);
            options.put("archivers",archivers);


            options.put("uiCmdMap",uiCmdMap);
            options.put("syncChkAsTs",1);
            options.put("syncMinMs", 30000);
            wrapper.put("options", options);

            if( init){

                JSONArray netDriversArray = new JSONArray();
//                netDriversArray.put("ftp");//TODO add netdrivers for FTP, others?
                wrapper.put("netDrivers", netDriversArray);

                JSONObject debug = new JSONObject();
                debug.put("connector","java");
                debug.put("time",(System.currentTimeMillis() - startTime)/1000.0);
                debug.put("memory","3348Kb / 2507Kb / 128M");// FIXME user real memory figures

                JSONArray volumes = new JSONArray();
                JSONObject volume = new JSONObject();
                volume.put("id",volumeId);
                volume.put("driver","localfilesystem");
                volume.put("mimeDetect","internal");
                debug.put("volumes",volumes);

                JSONArray mountErrors = new JSONArray();
                debug.put("mountErrors", mountErrors);

                wrapper.put("debug",debug);
            }

            if( DEBUG){

                LogUtil.log(LogUtil.LogType.CMD_OPEN, wrapper.toString());
                LogUtil.log(LogUtil.LogType.CMD_OPEN, "After wrapper");
            }

            String result = wrapper.toString();
            return new ByteArrayInputStream(result.getBytes("UTF-8"));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
