/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.main;//

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.nuvolect.deepdive.R;
import com.nuvolect.deepdive.license.AppSpecific;
import com.nuvolect.deepdive.license.LicenseManager;
import com.nuvolect.deepdive.license.LicensePersist;
import com.nuvolect.deepdive.license.LicenseUtil;
import com.nuvolect.deepdive.settings.LobbySettingsActivity;
import com.nuvolect.deepdive.util.ActionBarUtil;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.Util;
import com.nuvolect.deepdive.webserver.WebService;
import com.nuvolect.deepdive.webserver.WebUtil;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * The internal user may require authentication to use the the web server.
 * Internal users use a entry passphrase/keypad or YubiKey for entry,
 * are automatically authenticated and do not use the login.htm page.
 * External users go through a login.htm page if the system is not wide open.
 */

public class MainActivity extends FragmentActivity {

    private static Activity m_act;
    private static Context m_ctx;
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

    LicenseManager.LicenseCallbacks mLicenseManagerListener = new LicenseManager.LicenseCallbacks(){

        @Override
        public void licenseResult(LicenseManager.LicenseResult license) {

        if(DEBUG) LogUtil.log(LogUtil.LogType.MAIN_ACTIVITY, "License result: "+license.toString());
        LicensePersist.setLicenseResult(m_ctx, license);

        switch ( license) {
            case NIL:
                break;
            case REJECTED_TERMS:{
                m_act.finish();
                break;
            }
            case WHITELIST_USER:
            case PRO_USER: {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    m_ctx.startForegroundService(new Intent(m_ctx, WebService.class));
                } else {
                    m_ctx.startService(new Intent(m_ctx, WebService.class));
                }

                startGui();
                break;
            }
            default:
                break;
        }
        }
    };

    private void startGui() {

        /**
         * Detect app upgrade and provide a placeholder for managing upgrades, database changes, etc.
         */
        boolean appUpgraded = LicenseUtil.appUpgraded(m_act);

        if(appUpgraded) {

            Toast.makeText( m_act, "Application upgraded", Toast.LENGTH_LONG).show();

            // Execute upgrade methods
        }

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

        Intent serverIntent = new Intent(m_ctx, WebService.class);
        boolean serverStopped = m_ctx.stopService( serverIntent);
        LogUtil.log( LogUtil.LogType.MAIN_ACTIVITY, "Embedded webserver stopped: "+serverStopped);
        m_act.finish();
        delayShutdownForToast.dispatchMessage(new Message());
    }

    private Handler delayShutdownForToast = new Handler() {
        @Override
        public void handleMessage(Message message) {
            Toast.makeText( m_act, "Webserver shutting down", Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    }
                    catch (Exception e) { }
                    LogUtil.log( LogUtil.LogType.MAIN_ACTIVITY, "Calling system.exit: ");
                    System.exit(0);
                }
            }).start();
        }
    };

    private boolean haveNecessaryPermissions() {
        return( hasPermission(WRITE_EXTERNAL_STORAGE));
    }

    private boolean hasPermission(String perm) {
        return(ContextCompat.checkSelfPermission(this, perm)== PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Based on user preference, the service may be restarted to mount external storage.
     * @param anInt
     * @param strs
     * @param ints
     */
    @Override
    public void onRequestPermissionsResult(int anInt, String[] strs, int[] ints){

        Intent serverIntent = new Intent(m_ctx, WebService.class);
        m_ctx.stopService(serverIntent);
        m_ctx.startService(serverIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        LogUtil.log( LogUtil.LogType.MAIN_ACTIVITY, "MainActivity.onActivityResult()");

        switch( requestCode ){

            default:
                if(DEBUG) LogUtil.log( LogUtil.LogType.MAIN_ACTIVITY, "ERROR, MainActivity invalid requestCode: "+requestCode);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        if( (LicenseManager.isWhitelistUser() || Boolean.valueOf( m_act.getString(R.string.verbose_logging)))
                && DeveloperDialog.isEnabled()){

            Util.showMenu( menu, R.id.menu_developer );
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:{

                quitApp();
                break;
            }
            case R.id.menu_help:{

                String url = AppSpecific.APP_WIKI_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            }
            case R.id.menu_settings:{

                Intent i = new Intent(m_act, LobbySettingsActivity.class);
                startActivity(i);
                break;
            }
            case R.id.menu_developer:{
                DeveloperDialog.start(m_act);
                break;
            }
            default:{
                LogUtil.log( LogUtil.LogType.MAIN_ACTIVITY, "uncaught onOptionsItemSelected: "+item.getItemId());
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
                LogUtil.log(LogUtil.LogType.MAIN_ACTIVITY, "uncaught onClickAction: "+view.getId());
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
