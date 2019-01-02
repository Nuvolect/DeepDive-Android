/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.main;//

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nuvolect.deepdive.R;
import com.nuvolect.deepdive.util.ShowTips;
import com.nuvolect.deepdive.webserver.WebUtil;

import androidx.fragment.app.Fragment;

public class LobbyFragment extends Fragment {

    private static final String KEY_FILE="file";

    static LobbyFragment newInstance(String file){

        LobbyFragment f = new LobbyFragment();

        Bundle args = new Bundle();

        args.putString(KEY_FILE, file);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance( true );
    }

    @Override
    public void onResume() {
        super.onResume();

        // Display tips if enabled
        ShowTips.getInstance(getActivity()).embeddedShowTips();

        /**
         * Configure refresh button
         */
        ImageView iv = (ImageView) getView().findViewById(R.id.refreshIv);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                WebUtil.resetIpPortCache(getActivity());
                getActivity().recreate();
            }
        });

        /**
         * show server IP address and status
         */
        TextView tv = (TextView) getView().findViewById(R.id.status1Tv);
        tv.setText(WebUtil.getServerUrl(getActivity()));
        tv.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                copyIpToPasteBuffer();
            }
        });
        tv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                browserChooser();
                return false;
            }
        });

        tv = (TextView) getView().findViewById(R.id.status2Tv);
        tv.setText(UserManager.getInstance(getActivity()).isWideOpen()?
                "Unsecured, no password":
                "Secured with password");
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                copyIpToPasteBuffer();
            }

        });
        tv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                browserChooser();
                return false;
            }
        });
    }

    private void copyIpToPasteBuffer() {

        // Gets a handle to the clipboard service.
        ClipboardManager clipboard = (ClipboardManager)
                getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        String ipAddress = WebUtil.getServerUrl(getActivity());

        // Creates a new text clip to put on the clipboard
        ClipData clip = ClipData.newPlainText("App IP address", ipAddress);

        // Set the clipboard's primary clip.
        clipboard.setPrimaryClip(clip);

        Toast.makeText(getActivity(), "IP address copied to paste buffer", Toast.LENGTH_SHORT).show();
    }

    private void browserChooser(){

        Uri uri = Uri.parse( WebUtil.getServerUrl( getActivity()) );
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        Intent chooser = Intent.createChooser( intent, "Choose browser");
        // Verify the intent will resolve to at least one activity
        if (chooser.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(chooser);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.lobby_fragment, container, false);

        return(rootView);
    }
}
