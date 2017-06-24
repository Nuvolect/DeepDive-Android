package com.nuvolect.deepdive.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.nuvolect.deepdive.R;
import com.nuvolect.deepdive.license.AppSpecific;
import com.nuvolect.deepdive.license.AppUpgrade;
import com.nuvolect.deepdive.util.ActionBarUtil;
import com.nuvolect.deepdive.util.Analytics;
import com.nuvolect.deepdive.main.CConst;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.ShowTips;


public class LobbySettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final boolean DEBUG = false;
    Activity m_act;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_act = this;
        setContentView(R.layout.settings_preference);

        ActionBarUtil.showTitleEnabled(m_act, true);
        ActionBarUtil.homeAsUpEnabled(m_act, true);

        LobbySettingsFragment.startSettingsFragment(m_act, R.id.settings_fragmment_container);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.settings_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Analytics.sendMenuItem(m_act, "LobbySettings", item);

        switch (item.getItemId()) {

            case android.R.id.home:{
                m_act.finish();
                return true;
            }
            case R.id.menu_refresh:{
                Toast.makeText(m_act,"Refresh",Toast.LENGTH_SHORT).show();
                LobbySettingsFragment.startSettingsFragment(m_act, R.id.settings_fragmment_container);
                return true;
            }
            case R.id.menu_app_upgrade:{
                // Check app version on background thread and present results to UI
                AppUpgrade.getInstance(m_act).showDialog();
                break;
            }
            case R.id.menu_show_tips:{

                boolean showTipsCheckBox = false;
                ShowTips.getInstance(m_act).dialogShowTips(showTipsCheckBox);
                break;
            }
            case R.id.menu_help:{
                String url = AppSpecific.APP_WIKI_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            }
            case R.id.menu_roadmap:{
                String url = AppSpecific.APP_ROADMAP_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            }
            case R.id.menu_issues:{
                String url = AppSpecific.APP_ISSUES_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            }
            case R.id.menu_developer_feedback:{
                int appVersion = 0;
                try {
                    appVersion = m_act.getPackageManager().getPackageInfo(
                            m_act.getPackageName(), 0).versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"team@nuvolect.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "DeepDive Feedback, App Version: "+appVersion);
                i.putExtra(Intent.EXTRA_TEXT   , "Please share your thoughts or ask a question.");

                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(m_act, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    public static void setShowTips(Context ctx, boolean checked) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        sharedPref.edit().putBoolean(CConst.SHOW_TIPS, checked).apply();
    }
    public static boolean getShowTips(Context ctx) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getBoolean(CConst.SHOW_TIPS, true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        LogUtil.log(LogUtil.LogType.SETTINGS_ACTIVITY, "onNewIntent");
    }
}
