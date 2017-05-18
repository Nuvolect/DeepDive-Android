package com.nuvolect.deepdive.main;

import com.nuvolect.deepdive.R;

public class CConst {

    public static final String APP_SIGNATURE        = "com.nuvolect.deepdive";
    public static final String APPS_PAGE            = "/apps.htm";
    public static final String ASSET_DATA_FOLDER    = "data/";
    public static final String AUTHENTICATED        = "authenticated";
    public static final String CHUNK                = "/chunk/";
    public static final String COMPANION_IP_PORT    = "companion_ip_port";//Match settings.xml
    public static final String DEFAULT_IP_PORT      = "0.0.0.0:0000";
    public static final String DEFAULT_PASSPHRASE   = "WhenAllElseFails";
    public static final String DEVICE_PAGE          = "/device.htm";
    public static final String ELF_                 = "elf_";
    public static final String ELFINDER_PAGE        = "/elFinder-2.1.24-nightly/finder.html";
    public static final String EMBEDDED_USER        = "embedded_user";
    public static final String FALSE                = "false";
    public static final String FIELD_CONTENT        = "content";
    public static final String FIELD_FILENAME       = "filename";
    public static final String FIELD_PATH           = "path";
    public static final String FIELD_VOLUME         = "volume_id";
    public static final String FILE_NAME            = "file_name";
    public static final String FILE_PATH            = "file_path";
    public static final String NAME                 = "name";
    public static final String NO_PASSPHRASE        = "no-passphrase";
    public static final String OK                   = "ok";
    public static final String PORT                 = "port";
    public static final String PORT_NUMBER          = "port_number";
    public static final String RANDOM_EDGE          = "h0!U9#Wfnx";// Validates security certificate
    public static final String RESPONSE_CODE        = "response_code";
    public static final String ROOT                 = "/";
    public static final String SEARCH_MANAGER_PAGE  = "/search_manager.htm";
    public static final String SEC_TOK              = "sec_tok";
    public static final String SHOW_TIPS            = "show_tips";
    public static final String SLASH                = "/";
    public static final String STRING32             = "01234567890123456789012345678901";
    public static final String TARGET               = "target";
    public static final String THREAD_STACK_SIZE    = "thread_stack_size";
    public static final String TMP                  = "tmp";
    public static final String TRUE                 = "true";
    public static final String UNIQUE_ID            = "unique_id";
    public static final String URL                  = "url";
    public static final String USER_FOLDER_PATH     = "/DeepDive/";
    public static final String USER_MANAGER         = "user_manager";
    public static final String USERS                = "users";

    public final static int SMALL_ICON              = R.drawable.icon_64;
    public static final int IP_TEST_TIMEOUT_MS      = 8000;// Time for testing for companion device IP

    public static final long DURATION_1_DAY_MS      = 24 * 60 * 60 * 1000L;// 1 day
    public static final long DURATION_7_DAYS_MS     =   7 * DURATION_1_DAY_MS;
    public static final long DURATION_30_DAYS_MS    =  30 * DURATION_1_DAY_MS;
    public static final long DURATION_HALF_YEAR_MS  = 182 * DURATION_1_DAY_MS;
    public static final long DURATION_1_YEAR_MS     = 365 * DURATION_1_DAY_MS;

    public static final String BLOG_URL             = "https://nuvolect.com/blog";
    public static final String APP_TOC_HREF_URL     = "<a href='http://nuvolect.com/deepdive/terms.htm'>Terms and Conditions</a>";
    public static final String APP_PP_HREF_URL      = "<a href='http://nuvolect.com/privacy'>Privacy Policy</a>";
    public static final String APP_GOOGLE_PLAY_HREF_URL = "<a href='https://play.google.com/store/apps/details?id=com.nuvolect.deepdive'>Google Play</a>";
    public static final String APP_NUVOLECT_HREF_URL = "<a href='https://nuvolect.com/deepdive/deepdive_nuvolect.apk'>Nuvolect</a>";
}
