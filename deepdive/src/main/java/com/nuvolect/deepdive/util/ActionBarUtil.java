/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;//

import android.app.ActionBar;
import android.app.Activity;
import android.view.Window;

import com.nuvolect.deepdive.R;

/**
 * Methods specific to using the ActionBar.
 */
public class ActionBarUtil {

    /**
     * Show the Up button in the action bar.
     */
    public static boolean showActionBarUpButton(Activity act){

        return homeAsUpEnabled(act, true);
    }
    public static boolean homeAsUpEnabled(Activity act, boolean state){

        ActionBar actionBar = act.getActionBar();
        if( actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(state);
            return true;
        } else{
            return false;
        }
    }
    public static boolean showTitleEnabled(Activity act, boolean b) {

        ActionBar actionBar = act.getActionBar();
        if( actionBar != null){
            actionBar.setDisplayShowTitleEnabled(b);
            return true;
        } else{
            return false;
        }
    }
    public static boolean showTitleEnabled(Activity act, String title) {

        ActionBar actionBar = act.getActionBar();
        if( actionBar != null){
            if( title.isEmpty()){

                actionBar.setDisplayShowTitleEnabled( false );
            }
            else{
                actionBar.setDisplayShowTitleEnabled( true );
                actionBar.setTitle( title );
                act.setTitle(title);
            }
            return true;
        } else{
            return false;
        }
    }

    public static void hide(Activity act) {

        ActionBar actionBar = act.getActionBar();
        if( actionBar != null)
            actionBar.hide();
    }

    public static void show(Activity act) {

        ActionBar actionBar = act.getActionBar();
        if( actionBar != null)
            actionBar.show();
    }

    public static void init(Activity act) {

        /**
         * Request feature must be called before adding content
         */
        act.requestWindowFeature(Window.FEATURE_ACTION_BAR);
    }

    public static void setTransparent(Activity act){

//        ColorDrawable newColor = new ColorDrawable(Color.TRANSPARENT);
//
//        ActionBar actionBar = act.getActionBar();
//        actionBar.setBackgroundDrawable( newColor);
//        actionBar.setElevation(0);
//        actionBar.setStackedBackgroundDrawable( newColor);

        ActionBar ab=act.getActionBar();

        if (ab!=null) {
            ab.setBackgroundDrawable(act
                    .getResources()
                    .getDrawable(R.drawable.cwac_cam2_action_bar_bg_transparent));
        }
    }
}
