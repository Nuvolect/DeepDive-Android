package com.nuvolect.deepdive.main;//

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.nuvolect.deepdive.R;
import com.nuvolect.deepdive.license.AppSpecific;
import com.nuvolect.deepdive.license.LicenseManager;
import com.nuvolect.deepdive.license.LicensePersist;
import com.nuvolect.deepdive.settings.LobbySettingsActivity;
import com.nuvolect.deepdive.util.ActionBarUtil;
import com.nuvolect.deepdive.util.Analytics;
import com.nuvolect.deepdive.util.DialogUtil;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.webserver.WebService;
import com.nuvolect.deepdive.webserver.WebUtil;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * The internal user may require authentication to use the the web server.
 * Internal users use a entry passphrase/keypad or YubiKey for entry,
 * are automatically authenticated and do not use the login.htm page.
 * External users go through a login.htm page if the system is not wide open.
 */

public class MainActivity extends FragmentActivity {

    Activity m_act;
    Context m_ctx;
    private Bundle m_savedInstanceState;
    private final static boolean DEBUG = LogUtil.DEBUG;

    APP_STATE appState = APP_STATE.LOBBY;

    enum APP_STATE { APP_LIST, DEVICE, FILE_MANAGER, LOBBY, SETTINGS, SEARCH_MANAGER, }

    private static final String[] PERMS_ALL={
            WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_act = this;
        m_ctx = getApplicationContext();
        m_savedInstanceState = savedInstanceState;

        ActionBarUtil.init(m_act);
        ActionBarUtil.homeAsUpEnabled(m_act, true);

        setContentView(R.layout.simple_frame_layout);

        Intent serverIntent = new Intent(m_ctx, WebService.class);
        m_ctx.startService(serverIntent);

        if (!Environment.MEDIA_MOUNTED
                .equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "Cannot access external storage!", Toast.LENGTH_LONG).show();
            quitApp();
        }

        /**
         * Kick off the license manager.
         */
        LicenseManager.getInstance(m_act).checkLicense(m_act, mLicenseManagerListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * Restore the action bar when returning from camera activity
         */
        ActionBarUtil.show(m_act);
    }
    @Override
    public void onStart() {
        super.onStart();

        Analytics.start(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        Analytics.stop(this);
    }

    LicenseManager.LicenseCallbacks mLicenseManagerListener = new LicenseManager.LicenseCallbacks(){

        @Override
        public void licenseResult(LicenseManager.LicenseResult license) {

        if(DEBUG) LogUtil.log("License result: "+license.toString());
        LicensePersist.setLicenseResult(m_ctx, license);

        switch ( license) {
            case NIL:
                break;
            case REJECTED_TERMS:
                m_act.finish();
                break;
            case WHITELIST_USER:
            case PRO_USER:
            case APPRECIATED_USER: {

                startGui();
                break;
            }
            case PRO_USER_EXPIRED:{
                DialogUtil.confirmDialog(m_act,
                        "App License Expired",
                        "The app license has expired. To enable Pro features please upgrade your license.\n",
                        "Exit",
                        new DialogUtil.DialogCallback() {
                            @Override
                            public void confirmed() {
                                LicenseManager.upgradeLicense(m_act);
                            }

                            @Override
                            public void canceled() {
                                startGui();
                            }
                        });
            }
            case APP_EXPIRED:{
                DialogUtil.confirmDialog(m_act,
                        "App Version Expired",
                        "This app version has expired and is no longer supported.\n"+
                        "Please update the app from "+CConst.APP_GOOGLE_PLAY_HREF_URL+
                        " or "+CConst.APP_NUVOLECT_HREF_URL+".",
                        "Exit",
                        new DialogUtil.DialogCallback() {
                            @Override
                            public void confirmed() {
                               m_act.finish();
                            }

                            @Override
                            public void canceled() {
                                m_act.finish();
                            }
                        });
            }
            default:
                break;
        }
        }
    };

    private void startGui() {

        if (!haveNecessaryPermissions()) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(PERMS_ALL, 0);
            }
            else
                Toast.makeText(m_act,"Request permission storage",Toast.LENGTH_SHORT).show();
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if( m_savedInstanceState == null) {

            startLobbyFragment();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if( appState == APP_STATE.LOBBY){

            quitApp();
        }else{

            startLobbyFragment();
            ActionBarUtil.show(m_act);
        }
    }

    private void quitApp(){

        m_act.finish();
    }

    private boolean haveNecessaryPermissions() {
        return( hasPermission(WRITE_EXTERNAL_STORAGE));
    }

    private boolean hasPermission(String perm) {
        return(ContextCompat.checkSelfPermission(this, perm)== PackageManager.PERMISSION_GRANTED);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        LogUtil.log("MainActivity.onActivityResult()");

        switch( requestCode ){

            default:
                if(DEBUG) LogUtil.log("ERROR, MainActivity invalid requestCode: "+requestCode);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:{

                Analytics.send(getApplicationContext(),
                        Analytics.MAIN_MENU,
                        Analytics.UP_BUTTON_EXIT,
                        Analytics.COUNT, 1);

                quitApp();
                break;
            }
            case R.id.menu_help:{

                Analytics.send(getApplicationContext(),
                        Analytics.MAIN_MENU,
                        Analytics.HELP,
                        Analytics.COUNT, 1);

                String url = AppSpecific.APP_HELP_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            }
            case R.id.menu_settings:{

                Analytics.send(getApplicationContext(),
                        Analytics.MAIN_MENU,
                        Analytics.SETTINGS,
                        Analytics.COUNT, 1);

                Intent i = new Intent(m_act, LobbySettingsActivity.class);
                startActivity(i);
                break;
            }
            default:{
                LogUtil.log(LogUtil.LogType.MAIN, "uncaught onOptionsItemSelected: "+item.getItemId());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickAction(View view) {

        switch (view.getId()){

            case R.id.deviceTr:{
                ActionBarUtil.hide(m_act);
                startDeviceFragment();
                break;
            }
            case R.id.appsTr:{
                ActionBarUtil.hide(m_act);
                startAppsFragment();
                break;
            }
            case R.id.fileManagerTr:{
                ActionBarUtil.hide(m_act);
                startFileManagerFragment();
                break;
            }
            case R.id.searchManagerTr:{
                ActionBarUtil.hide(m_act);
                startSearchManagerFragment();
                break;
            }
            default:
                LogUtil.log(LogUtil.LogType.MAIN, "uncaught onClickAction: "+view.getId());
        }
    }

    /**
     * Lookup the current app state and remove the current fragment
     */
    private void removeCurrentFragment(){

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        switch( appState ){

            case LOBBY:
                ft = null;
                break;
            case FILE_MANAGER:
                ft.remove(webFragment);
                break;
            case SEARCH_MANAGER:
                ft.remove(webFragment);
                break;
            case SETTINGS:
                ft = null;
                break;
        }
        if( ft != null)
            ft.commitAllowingStateLoss();
    }

    WebFragment webFragment;
    LobbyFragment lobbyFragment;

    private void startDeviceFragment() {

        removeCurrentFragment();
        appState = APP_STATE.DEVICE;
        String url = WebUtil.getServerUrl(getApplicationContext())+CConst.DEVICE_PAGE;
        webFragment = new WebFragment().newInstance(url);
        String fragmentTag = "webFragmentTag";

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Full screen
        ft.replace(android.R.id.content, webFragment, fragmentTag);
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
    }

    private void startAppsFragment() {

        removeCurrentFragment();
        appState = APP_STATE.APP_LIST;
        String url = WebUtil.getServerUrl(getApplicationContext())+CConst.APPS_PAGE;
        webFragment = new WebFragment().newInstance(url);
        String fragmentTag = "webFragmentTag";

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Full screen
        ft.replace(android.R.id.content, webFragment, fragmentTag);
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
    }

    private void startFileManagerFragment() {

        removeCurrentFragment();
        appState = APP_STATE.FILE_MANAGER;
        String url = WebUtil.getServerUrl(getApplicationContext())+CConst.ELFINDER_PAGE;
        webFragment = new WebFragment().newInstance(url);
        String fragmentTag = "webFragmentTag";

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Full screen
        ft.replace(android.R.id.content, webFragment, fragmentTag);
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
    }
    private void startSearchManagerFragment() {

        removeCurrentFragment();
        appState = APP_STATE.SEARCH_MANAGER;
        String url = WebUtil.getServerUrl(getApplicationContext())+CConst.SEARCH_MANAGER_PAGE;
        webFragment = new WebFragment().newInstance(url);
        String fragmentTag = "webFragmentTag";

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Full screen
        ft.replace(android.R.id.content, webFragment, fragmentTag);
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
    }
    private void startLobbyFragment() {

        removeCurrentFragment();
        appState = APP_STATE.LOBBY;
        lobbyFragment = new LobbyFragment();
        String fragmentTag = "lobbyFragmentTag";

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.my_content, lobbyFragment, fragmentTag);
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
    }

}
