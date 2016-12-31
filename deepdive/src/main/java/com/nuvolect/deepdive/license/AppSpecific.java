package com.nuvolect.deepdive.license;

import android.content.Context;
import android.net.Uri;

import com.nuvolect.deepdive.R;
import com.nuvolect.deepdive.ddUtil.CConst;


/**
 * Details of the license specific to this app. Keep separate from other license classes
 * to enable plug-in-play ease of maintenance.
 */
public class AppSpecific {

    public static final String APP_NAME                   = "DeepDive";

    public static final String APP_SIMPLE_CRYPTO_CRYP_SEED= "I#hQCHma4D@ZQR6li6$h";//deprecated
    public static final String APP_CRYP_SEED_HEX          = "712c786de426d57c53706f05892cac01";
    public static final String EARLY_ADOPTER_CRYP         = "pzdvsnaxqeafuvjdtqpj";
    public static final String EARLY_ADOPTER_CRYP_HEX     = "16ba96b4e2fa961e2620c81e945309ac";

    public final static int SMALL_ICON                    = R.drawable.icon_64;
    public final static String GOOGLE_PLAY_HREF_URL       = CConst.APP_GOOGLE_PLAY_HREF_URL;
    public final static String TOC_HREF_URL               = CConst.APP_TOC_HREF_URL;
    public final static String PP_HREF_URL                = CConst.APP_PP_HREF_URL;

    public final static String APP_GOOGLE_PLAY_URL        = "https://play.google.com/store/apps/details?id=com.nuvolect.deepdive";
    public static final String APP_HELP_URL               = "https://nuvolect.com/deepdive_help/";
    public static final String APP_INFO_URL               = "https://nuvolect.com/deepdive/";
    public static final String APP_ISSUES_URL             = "https://github.com/TeamNuvolect/DeepDive/issues";
    public static final String APP_PRIVACY_URL            = "https://nuvolect.com/privacy/";
    public static final String APP_ROADMAP_URL            = "https://nuvolect.com/blog/posts/pt-roadmap";
    public static final String APP_TERMS_URL              = "https://nuvolect.com/deepdive_terms/";
    public final static Uri APP_GOOGLE_PLAY_URI           = Uri.parse("market://details?id=com.nuvolect.deepdive");

    public static final boolean EARLY_ADOPTER_PERIOD      = true;
    public static final int EVAL_DAYS                     = 7;
    public static final long EVALUATION_PERIOD_MS         = EVAL_DAYS * 24 * 60 * 60 * 1000L;
    /**
     * Period between nags
     */
    public static final long EVAL_EXPIRED_NO_NAG_PERIOD   = 6 * 24 * 60 * 60 * 1000L;// 6 days between nags
    public static final String EVAL_EXPIRED_NAG_KEY       = "eval_expired_nag";
    public static final String WHATS_NET_NAG_KEY          = "whats_new_nag";
    public static final long WHATS_NEW_NAG_PERIOD         = 7 * 24 * 60 * 60 * 1000L;// 7 days between nags

    public static String[] PREMIUM_PITCH_LIST = {
            "Premium get all the best stuff",
            "Get Premium: More features",
            "Premium App means Premium support",
            "Premium App: No ads"
    };

    public static String getAppFolderPath(Context ctx) {

        return ctx.getString(R.string.APP_PUBLIC_FOLDER_RELATIVE_PATH);
    }
}