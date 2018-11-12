package com.nuvolect.deepdive.license;

import android.content.Context;

import com.nuvolect.deepdive.R;
import com.nuvolect.deepdive.main.CConst;


/**
 * Details of the license specific to this app. Keep separate from other license classes
 * to enable plug-in-play ease of maintenance.
 */
public class AppSpecific {

    public static final String APP_NAME                   = "DeepDive";

    public static final String APP_CRYP_SEED_HEX          = "712c786de426d57c53706f05892cac01";

    public final static int SMALL_ICON                    = R.drawable.icon_64;
    public final static String TOC_HREF_URL               = CConst.APP_TOC_HREF_URL;
    public final static String PP_HREF_URL                = CConst.APP_PP_HREF_URL;

    public static final String APP_INFO_URL               = "https://nuvolect.com/deepdive/";
    public static final String APP_WIKI_URL               = "https://github.com/Nuvolect/DeepDive-Android/wiki";
    public static final String APP_ISSUES_URL             = "https://github.com/Nuvolect/DeepDive-Android/issues";
    public static final String APP_PRIVACY_URL            = "https://nuvolect.com/privacy/";
    public static final String APP_ROADMAP_URL            = "https://nuvolect.com/blog/posts/dd-roadmap";
    public static final String APP_TERMS_URL              = "https://nuvolect.com/deepdive_terms/";

    public static String getAppFolderPath(Context ctx) {

        return ctx.getString(R.string.APP_PUBLIC_FOLDER_RELATIVE_PATH);
    }
}