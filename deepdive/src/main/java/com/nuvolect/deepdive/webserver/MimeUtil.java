/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver;//

import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;

import org.apache.commons.io.FilenameUtils;

import java.util.HashSet;
import java.util.Locale;

//TODO create class description
//
public class MimeUtil {

    private static HashSet<String> audioValues = new HashSet<String>();
    private static HashSet<String> imageValues = new HashSet<String>();
    private static HashSet<String> mimeValues = new HashSet<String>();
    private static HashSet<String> textValues = new HashSet<String>();
    private static HashSet<String> videoValues = new HashSet<String>();

    /**
     * Text file extensions
     */
    public static final String[] TEXT_FILE_EXTENSION = {
            "css",
            "htm",
            "html",
            "java",
            "json",
            "md",
            "txt",
            "vcard",
            "vcf",
            "xml",
    };

    /**
     * Common mime types for dynamic content
     */
    public static final String
            MIME_AU	    = "audio/basic",
            MIME_AVI    = "video/avi",
            MIME_BIN    = "application/octet-stream",
            MIME_BMP    = "image/bmp",
            MIME_BZ2    = "application/x-bzip2",
            MIME_CSS    = "text/css",
            MIME_DEFAULT_BINARY  = "application/octet-stream",
            MIME_DOC    = "application/msword",
            MIME_DOCM   = "application/vnd.ms-word.document.macroEnabled.12",
            MIME_DOCX   = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            MIME_DOTM   = "application/vnd.ms-word.template.macroEnabled.12",
            MIME_DOTX   = "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
            MIME_DTD    = "application/xml-dtd",
            MIME_ES	    = "application/ecmascript",
            MIME_EXE    = "application/octet-stream",
            MIME_FLV    = "video/x-flv",
            MIME_GIF    = "image/gif",
            MIME_GZ	    = "application/x-gzip",
            MIME_HQX    = "application/mac-binhex40",
            MIME_HTML   = "text/html",
            MIME_ICO    = "image/x-icon",
            MIME_JAR    = "application/java-archive",
            MIME_JPG    = "image/jpeg",
            MIME_JS	    = "application/javascript",
            MIME_JSON   = "application/json",
            MIME_MD     = "text/x-markdown",
            MIME_MIDI   = "audio/x-midi",
            MIME_MP3    = "audio/mpeg",
            MIME_MP4    = "video/mp4",
            MIME_MOV    = "video/quicktime",
            MIME_MPEG   = "video/mpeg",
            MIME_OGG    = "audio/ogg",
            MIME_OGV    = "video/ogg",
            MIME_PDF    = "application/pdf",
            MIME_PL	    = "application/x-perl",
            MIME_PNG    = "image/png",
            MIME_POTM   = "application/vnd.ms-powerpoint.template.macroEnabled.12",
            MIME_POTX   = "application/vnd.openxmlformats-officedocument.presentationml.template",
            MIME_PPAM   = "application/vnd.ms-powerpoint.addin.macroEnabled.12",
            MIME_PPSM   = "application/vnd.ms-powerpoint.slideshow.macroEnabled.12",
            MIME_PPSX   = "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
            MIME_PPT    = "application/vnd.ms-powerpoint",
            MIME_PPTM   = "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
            MIME_PPTX	= "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            MIME_PS	    = "application/postscript",
            MIME_PSD	= "image/vnd.adobe.photoshop",
            MIME_PY	    = "application/x-python",
            MIME_QT	    = "video/quicktime",
            MIME_RA	    = "audio/x-pn-realaudio, audio/vnd.rn-realaudio",
            MIME_RAM    = "audio/x-pn-realaudio, audio/vnd.rn-realaudio",
            MIME_RAR    = "application/x-rar",
            MIME_RDF    = "application/rdf, application/rdf+xml",
            MIME_RTF    = "application/rtf",
            MIME_SGML   = "text/sgml",
            MIME_SIT    = "application/x-stuffit",
            MIME_SLDX   = "application/vnd.openxmlformats-officedocument.presentationml.slide",
            MIME_SVG    = "image/svg+xml",
            MIME_SWF    = "application/x-shockwave-flash",
            MIME_TAR	= "application/x-tar",
            MIME_TGZ	= "application/x-tar",
            MIME_TIFF	= "image/tiff",
            MIME_TSV	= "text/tab-separated-values",
            MIME_TXT	= "text/plain",
            MIME_VCARD  = "text/vcard",
            MIME_WAV	= "audio/wav",
            MIME_WEBM	= "video/webm",
            MIME_WOFF   = "application/font-woff",
            MIME_XLAM	= "application/vnd.ms-excel.addin.macroEnabled.12",
            MIME_XLS	= "application/vnd.ms-excel",
            MIME_XLSB	= "application/vnd.ms-excel.sheet.binary.macroEnabled.12",
            MIME_XLSM	= "application/vnd.ms-excel.sheet.macroEnabled.12",
            MIME_XLSX	= "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            MIME_XLTM	= "application/vnd.ms-excel.template.macroEnabled.12",
            MIME_XLTX	= "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
            MIME_XML	= "application/xml",
            MIME_ZIP	= "application/zip"
    ;

    public enum MIME_TYPE {
        au,
        avi,
        bin,
        bmp,
        bz2,
        c,
        css,
        doc,
        docm,
        docx,
        dot,
        dotm,
        dotx,
        dtd,
        es,
        exe,
        flv,
        gif,
        gz,
        hqx,
        htm,
        html,
        ico,
        jar,
        java,
        jpg,
        js,
        json,
        m4a,
        md,
        midi,
        mov,
        mp3,
        mp4,
        mpeg,
        ogg,
        ogv,
        pdf,
        php,
        pl,
        png,
        pot,
        potm,
        potx,
        ppa,
        ppam,
        pps,
        ppsm,
        ppsx,
        ppt,
        pptm,
        pptx,
        ps,
        psd,
        py,
        qt,
        ra,
        ram,
        rar,
        rb,
        rdf,
        rtf,
        sdlx,
        sgml,
        sh,
        sit,
        sql,
        svg,
        swf,
        tar,
        tgz,
        tiff,
        tsv,
        ttf,
        txt,
        vcard,
        vcf,
        wav,
        webm,
        woff,
        woff2,
        xla,
        xlam,
        xls,
        xlsb,
        xlsm,
        xlsx,
        xlt,
        xltm,
        xltx,
        xml,
        zip,

    }

    /**
     * Returns the string mime type of a file extension.
     * The extensions are expected to be in lower case.
     * @param file
     * @return
     */

    public static String getMime(OmniFile file) {

        String extension = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.US);

        if( file.isDirectory())
            return "directory";

        return getMime(extension);
    }

    public static String getMime(java.io.File file) {

        String extension = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.US);

        if( file.isDirectory())
            return "directory";

        return getMime(extension);
    }

    public static String getMime(String extension){

        if( mimeValues.isEmpty()){

            for (MIME_TYPE c : MIME_TYPE.values()) {
                mimeValues.add(c.name());
            }
        }

        if( ! mimeValues.contains(extension)){

//            if( ! extension.isEmpty())
//                LogUtil.log(LogUtil.LogType.MIME_UTIL, "Extension missing: "+extension);
            return MIME_DEFAULT_BINARY;
        }

        MIME_TYPE mime_type = MIME_TYPE.bin;

        mime_type = MIME_TYPE.valueOf(extension);

        try {
            switch (mime_type){

                case au: return MIME_AU;
                case avi: return MIME_AVI;
                case bin: return MIME_BIN;
                case bmp: return MIME_BMP;
                case bz2: return MIME_BZ2;
                case c: return MIME_TXT;
                case css: return MIME_CSS;
                case doc: return MIME_DOC;
                case docm: return MIME_DOCM;
                case docx: return MIME_DOCX;
                case dot: return MIME_DOC;
                case dotm: return MIME_DOTM;
                case dotx: return MIME_DOTX;
                case dtd: return MIME_DTD;
                case es: return MIME_ES;
                case exe: return MIME_EXE;
                case flv: return MIME_FLV;
                case gif: return MIME_GIF;
                case gz: return MIME_GZ;
                case hqx: return MIME_HQX;
                case htm: return MIME_HTML;
                case html: return MIME_HTML;
                case ico: return MIME_ICO;
                case jar: return MIME_JAR;
                case java: return MIME_TXT;
                case jpg: return MIME_JPG;
                case js: return MIME_JS;
                case json: return MIME_JSON;
                case m4a: return MIME_MP4;
                case md: return MIME_MD;
                case midi: return MIME_MIDI;
                case mov: return MIME_MOV;
                case mp3: return MIME_MP3;
                case mp4: return MIME_MP4;
                case mpeg: return MIME_MPEG;
                case ogg: return MIME_OGG;
                case ogv: return MIME_OGV;
                case pdf: return MIME_PDF;
                case php: return MIME_TXT;
                case pl: return MIME_PL;
                case png: return MIME_PNG;
                case pot: return MIME_PPT;
                case potm: return MIME_POTM;
                case potx: return MIME_POTX;
                case ppsx: return MIME_PPSX;
                case ppa: return MIME_PPT;
                case ppam: return MIME_PPAM;
                case pps: return MIME_PPT;
                case ppsm: return MIME_PPSM;
                case ppt: return MIME_PPT;
                case pptm: return MIME_PPTM;
                case pptx: return MIME_PPTX;
                case ps: return MIME_PS;
                case psd: return MIME_PSD;
                case py: return MIME_PY;
                case qt: return MIME_QT;
                case ra: return MIME_RA;
                case ram: return MIME_RAM;
                case rar: return MIME_RAR;
                case rb: return MIME_TXT;
                case rdf: return MIME_RDF;
                case rtf: return MIME_RTF;
                case sdlx: return MIME_SLDX;
                case sgml: return MIME_SGML;
                case sh: return MIME_TXT;
                case sit: return MIME_SIT;
                case sql: return MIME_TXT;
                case svg: return MIME_SVG;
                case swf: return MIME_SWF;
                case tar: return MIME_TAR;
                case tgz: return MIME_TGZ;
                case tiff: return MIME_TIFF;
                case tsv: return MIME_TSV;
                case ttf: return MIME_BIN;
                case txt: return MIME_TXT;
                case vcard: return MIME_VCARD;
                case vcf: return MIME_VCARD;
                case wav: return MIME_WAV;
                case webm: return MIME_WEBM;
                case woff: return MIME_WOFF;
                case woff2: return MIME_WOFF;
                case xlam: return MIME_XLAM;
                case xla: return MIME_XLS;
                case xls: return MIME_XLS;
                case xlsb: return MIME_XLSB;
                case xlsm: return MIME_XLSM;
                case xlsx: return MIME_XLSX;
                case xlt: return MIME_XLS;
                case xltm: return MIME_XLTM;
                case xltx: return MIME_XLTX;
                case xml: return MIME_XML;
                case zip: return MIME_ZIP;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.logException(LogUtil.LogType.MIME_UTIL, e);
        }
        return MIME_DEFAULT_BINARY;
    }


    public static boolean isImage(String fileExtension) {

        if( imageValues.isEmpty()){

            imageValues.add("jpg");
            imageValues.add("jpeg");
            imageValues.add("png");
            imageValues.add("tif");
            imageValues.add("tiff");
            imageValues.add("gif");
        }
        return imageValues.contains( fileExtension);
    }

    public static boolean isAudio(String fileExtension) {

        if (audioValues.isEmpty()) {

            audioValues.add("pcm");
            audioValues.add("aac");
            audioValues.add("mp3");
        }
        return audioValues.contains(fileExtension);
    }
    public static boolean isVideo(String fileExtension) {

        if (videoValues.isEmpty()) {

            videoValues.add("mp4");
            videoValues.add("mov");
            videoValues.add("mpeg");
        }
        return videoValues.contains(fileExtension);
    }

    public static boolean isText(String fileExtension) {

        if (textValues.isEmpty()) {

            for( String extension : TEXT_FILE_EXTENSION){

                textValues.add(extension);
            }
        }
        return textValues.contains(fileExtension);
    }

}
