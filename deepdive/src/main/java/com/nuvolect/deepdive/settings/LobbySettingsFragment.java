package com.nuvolect.deepdive.settings;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nuvolect.deepdive.R;
import com.nuvolect.deepdive.license.AppSpecific;
import com.nuvolect.deepdive.license.LicensePersist;
import com.nuvolect.deepdive.main.UserManager;
import com.nuvolect.deepdive.util.ActionBarUtil;
import com.nuvolect.deepdive.util.CConst;
import com.nuvolect.deepdive.util.DeviceInfo;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.PermissionManager;
import com.nuvolect.deepdive.webserver.Comm;
import com.nuvolect.deepdive.webserver.WebUtil;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class LobbySettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final boolean DEBUG = LogUtil.DEBUG;
    static Activity m_act;
    private View m_rootView;
    private String mLicenseSummary;
    final long departureTime = System.currentTimeMillis();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if(DEBUG)LogUtil.log("SettingsFragment onCreate");

        m_act = getActivity();
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.lobby_settings);

        // Show the Up button in the action bar.
        ActionBarUtil.showActionBarUpButton(m_act);

        // Display current port number
        final EditTextPreference portPref = (EditTextPreference)findPreference("port_number");
        portPref.setSummary(String.valueOf(WebUtil.getPort(m_act)));
        portPref.setDefaultValue(String.valueOf(WebUtil.getPort(m_act)));
        portPref.setText(String.valueOf(WebUtil.getPort(m_act)));

        portPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                int portNumber = 0;
                try {
                    portNumber = Integer.valueOf( (String)newValue);
                } catch (NumberFormatException e) {
                    Toast.makeText(m_act, "Invalid, use range 1000:65535",Toast.LENGTH_SHORT).show();
                    return false;
                }
                if( portNumber < 1000 | portNumber > 65535){

                    Toast.makeText(m_act, "Invalid, use range 1000:65535",Toast.LENGTH_SHORT).show();
                    return false;
                }
                WebUtil.setPort(m_act, portNumber);
                WebUtil.resetIpPortCache(m_act);
                preference.setDefaultValue(String.valueOf(portNumber));
                Toast.makeText(m_act, "Restart app to update embedded web server",Toast.LENGTH_SHORT).show();

                preference.setSummary( String.valueOf(portNumber));
                return true;
            }
        });

        // Display current IP address
        final Preference ipPref = findPreference("ip_address");
        ipPref.setSummary(WebUtil.getServerUrl(m_act) + " OFFLINE");
        Map<String, String> parameters = new HashMap<String, String>();

        if (!WebUtil.getServerIp(m_act).contentEquals(CConst.DEFAULT_IP_PORT)) {

            final String thisDeviceUrl = WebUtil.getServerUrl(m_act)+"/connector?cmd=ping";

            Comm.sendPostUi(m_act, thisDeviceUrl, parameters, new Comm.CommPostCallbacks() {
                @Override
                public void success(String jsonString) {

                    /**
                     * A non-web view call is not authenticated generating a 401 error.
                     * This is ok. This demonstrates that the server is alive.
                     */
                    String summary = WebUtil.getServerUrl(m_act) + " ONLINE";
                    ipPref.setSummary(summary);
                }

                @Override
                public void fail(String error) {

                    String summary = WebUtil.getServerUrl(m_act) + " OFFLINE";
                    ipPref.setSummary(summary);
                }
            });
        }

        // Set license summary
        mLicenseSummary = LicensePersist.getLicenseSummary(m_act);
        Preference licensePref = findPreference(LicensePersist.APP_LICENSE);
        licensePref.setSummary(mLicenseSummary);

        // Display list of user names
        Preference userManagerPref = findPreference(CConst.USER_MANAGER);
        String users = UserManager.getInstance(m_act).getSummary();
        userManagerPref.setSummary(users);

        updatePermissionsSummary();
    }

    private void updatePermissionsSummary(){

        // Display current permissions
        String permissions = PermissionManager.getInstance(m_act).getSummary();
        Preference permissionManagerPref = findPreference(CConst.PERMISSION_MANAGER);
        permissionManagerPref.setSummary("Enabled: " + permissions);
    }

    int clickCount = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        m_rootView = inflater.inflate( R.layout.settings_preference, container, false);

        String version = "";
        try {
            PackageInfo pInfo = m_act.getPackageManager().getPackageInfo(m_act.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (NameNotFoundException e1) { }

        /**
         * Use the app version number at the bottom of the screen to capture a unique device install ID.
         * This install ID is can be used to determine if a device is whitelisted.
         */
        TextView appVersionTv = (TextView) m_rootView.findViewById(R.id.settings_app_version);
        appVersionTv.setText(AppSpecific.APP_NAME + " version " + version);
        appVersionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (++clickCount > 6) {
                    clickCount = 0;

                    String udi = null;
                    try {
                        udi = DeviceInfo.getDeviceInfo(m_act).toString(2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Gets a handle to the clipboard service.
                    ClipboardManager clipboard = (ClipboardManager)
                            m_act.getSystemService(Context.CLIPBOARD_SERVICE);

                    // Creates a new text clip to put on the clipboard
                    ClipData clip = ClipData.newPlainText("Unique Device ID", udi);

                    // Set the clipboard's primary clip.
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(m_act, "Device info copied to paste buffer", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return m_rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(DEBUG)LogUtil.log("SettingsFragment onResume");
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(DEBUG)LogUtil.log("SettingsFragment onPause");
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Display the fragment in the provided container.
     * @param act
     * @param containerViewId
     */
    public static LobbySettingsFragment startSettingsFragment(Activity act, int containerViewId){

        FragmentTransaction ft = act.getFragmentManager().beginTransaction();
        LobbySettingsFragment frag = new LobbySettingsFragment();
        ft.replace(containerViewId, frag);
        ft.commit();

        return frag;
    }


    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference.getKey().contentEquals("ip_address")) {

            String ip = WebUtil.getServerUrl(m_act);

            // Gets a handle to the clipboard service.
            ClipboardManager clipboard = (ClipboardManager)
                    m_act.getSystemService(Context.CLIPBOARD_SERVICE);

            // Creates a new text clip to put on the clipboard
            ClipData clip = ClipData.newPlainText("CrypSafe Device address", ip);

            // Set the clipboard's primary clip.
            clipboard.setPrimaryClip(clip);

            Toast.makeText(m_act, "Copied", Toast.LENGTH_SHORT).show();
        }
        if( preference.getKey().contentEquals( LicensePersist.APP_LICENSE)){

//            String url = "http://www.nuvolect.com/donate/#countercloud";
//            Intent i = new Intent(Intent.ACTION_VIEW);
//            i.setData(Uri.parse(url));
//            startActivity(i);
        }
        if( preference.getKey().contentEquals( "open_source_license" )){

            DisplayOpenSourceInfoFragment frag = new DisplayOpenSourceInfoFragment();
            frag.show(getFragmentManager(), "display_open_source_info");
        }
        if( preference.getKey().contentEquals(CConst.USER_MANAGER)){

            UserManager.getInstance(m_act).showDialog(m_act);
        }
        if( preference.getKey().contentEquals(CConst.PERMISSION_MANAGER)){

            PermissionManager.getInstance(m_act).showDialog(
                    new PermissionManager.PermissionMgrCallbacks() {
                        @Override
                        public void dialogOnCancel() {
                            updatePermissionsSummary();
                        }
                    }
            );
        }

        if( preference.getKey().contains("rate_app_google_play")){

            String url = AppSpecific.APP_GOOGLE_PLAY_URL;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public static class DisplayOpenSourceInfoFragment extends DialogFragment {
        static DisplayOpenSourceInfoFragment newInstance() {
            return new DisplayOpenSourceInfoFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View v = inflater.inflate( R.layout.open_source_license, container, false);

            setStyle( DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light);
            getDialog().setTitle("Software Licenses");

            return v;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch( requestCode ){

            default:
                if(DEBUG)LogUtil.log("onActivityResult: default action");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {

    }
}
