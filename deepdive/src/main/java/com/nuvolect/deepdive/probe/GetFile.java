package com.nuvolect.deepdive.probe;
//
//TODO create class description
//

public class GetFile {

//    public static JSONObject getText(String volumeId, String path) {// FIXME use an encoded path?
//
//        String file_name = FilenameUtils.getName( path);
//        boolean isText = MimeUtil.isText( FilenameUtils.getExtension(file_name));
//
//        OmniFile file = new OmniFile( volumeId, path );
//        String file_content = "";
//
//        if( file == null || ! file.exists())
//            file_content = "File does not exist: "+path;
//        else
//        if( file.isDirectory())
//            file_content = "File is a directory: "+path;
//        else
//        if( ! isText)
//            file_content = "File is binary: "+path;
//        else{
//            Context ctx = App.getContext();
//            file_content = OmniUtil.readFile(ctx, file);
//        }
//        JSONObject wrapper = new JSONObject();
//        try {
//            wrapper.put("file_name", file_name);
//            wrapper.put("file_content", file_content);
//            wrapper.put("file_relative_path", "/"+path.replaceFirst(Omni.getRoot(Omni.userVolumeId),""));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return wrapper;
//    }
}
