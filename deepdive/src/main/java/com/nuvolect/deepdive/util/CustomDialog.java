/*
 * Copyright (c) 2017. Nuvolect LLC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Contact legal@nuvolect.com for a less restrictive commercial license if you would like to use the
 * software without the GPLv3 restrictions.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package com.nuvolect.deepdive.util;//

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.nuvolect.deepdive.R;


/**
 * Custom dialogs for Rate this app and Make a donation.
 */
public class CustomDialog {

    private static Activity m_act;
    private static View m_view;
    private static Dialog m_dialog = null;
    private static final String PERSIST_NAME           = "custom_dialog_persist";
    private static long MS_PER_DAY = 24 * 60 * 60 * 1000;

    /**
     * Sets requirements for when to prompt the user.
     *
     * @param minLaunchesUntilInitialPrompt
     *            Minimum of launches before the user is prompted for the first
     *            time. One call of .run() counts as launch.
     * @param minDaysUntilInitialPrompt
     *            Minimum of days before the user is prompted for the first
     *            time.
     * @param minLaunchesUntilNextPrompt
     *            Minimum of launches before the user is prompted for each next
     *            time. One call of .run() counts as launch.
     * @param minDaysUntilNextPrompt
     *            Minimum of days before the user is prompted for each next
     *            time.
     */
    private static int rate_minLaunchesUntilInitialPrompt = 10;
    private static int rate_minDaysUntilInitialPrompt     = 14;
    private static int rate_minLaunchesUntilNextPrompt    = 10;
    private static int rate_minDaysUntilNextPrompt        = 30;

    private static int donate_minLaunchesUntilInitialPrompt = 15;
    private static int donate_minDaysUntilInitialPrompt     = 16;
    private static int donate_minLaunchesUntilNextPrompt    = 10;
    private static int donate_minDaysUntilNextPrompt        = 30;

    private static int minLaunchesUntilInitialPrompt;
    private static int minDaysUntilInitialPrompt;
    private static int minLaunchesUntilNextPrompt;
    private static int minDaysUntilNextPrompt;
    private static boolean cancelAlreadyCalled;

    /**
     * Call as part of the daily use cycle and interact with use
     * as dictated by the the rate* parameters.
     *FUTURE, the cancel button sometimes fails
     * @param act
     * @param testDialogNoMetrics
     */
    public static void rateThisApp(Activity act, final boolean testDialogNoMetrics){

        m_act = act;
        minLaunchesUntilInitialPrompt = rate_minLaunchesUntilInitialPrompt;
        minDaysUntilInitialPrompt = rate_minDaysUntilInitialPrompt;
        minLaunchesUntilNextPrompt = rate_minLaunchesUntilNextPrompt;
        minDaysUntilNextPrompt = rate_minDaysUntilNextPrompt;
        String title = "Rate this app?";
        String message = "Help us become a 5 star app!";
        final String dialogPrefix = "rate_";
        cancelAlreadyCalled = false;

        if( testDialogNoMetrics || shouldShowDialog(dialogPrefix)){

            showDialog( title, message, new CustomDialogCallbacks() {
                @Override
                public void okButton() {

                    if( testDialogNoMetrics )
                        Toast.makeText(m_act, "ok button", Toast.LENGTH_SHORT).show();
                    else
                        putInt(m_act, dialogPrefix+OK_ALREADY_SELECTED, 1);
                    String url =
                            "https://play.google.com/store/apps/details?id=com.nuvolect.deepdive";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    m_act.startActivity(i);

                    cancelDialog();
                }

                @Override
                public void cancel() {

                    if( ! cancelAlreadyCalled){

                        if( testDialogNoMetrics )
                            Toast.makeText(m_act, "cancel button", Toast.LENGTH_SHORT).show();

                        cancelDialog();

                        cancelAlreadyCalled = true;
                    }
                }

                @Override
                public void dontAskAgain() {

                    if( testDialogNoMetrics )
                        Toast.makeText(m_act, "don't ask again", Toast.LENGTH_SHORT).show();
                    else
                        putInt(m_act, dialogPrefix+DONT_ASK, 1);

                    cancelDialog();
                }
            });
        }
    }

    /**
     * Call as part of the daily use cycle and interact with use
     * as dictated by the the donate* parameters.
     *FUTURE, the cancel button sometimes fails
     * @param act
     * @param testDialogNoMetrics
     */
    public static void makeDonation(Activity act, final boolean testDialogNoMetrics){

        m_act = act;
        minLaunchesUntilInitialPrompt = donate_minLaunchesUntilInitialPrompt;
        minDaysUntilInitialPrompt = donate_minDaysUntilInitialPrompt;
        minLaunchesUntilNextPrompt = donate_minLaunchesUntilNextPrompt;
        minDaysUntilNextPrompt = donate_minDaysUntilNextPrompt;
        String title = "Make a donation?";
        String message = "This app is 100% clean, no adware, spyware or other unwanted stuff. Your donations make it possible.";
        final String dialogPrefix = "donate_";
        cancelAlreadyCalled = false;

        if( testDialogNoMetrics || shouldShowDialog(dialogPrefix)){

            showDialog( title, message, new CustomDialogCallbacks() {
                @Override
                public void okButton() {

                    if( testDialogNoMetrics )
                        Toast.makeText(m_act, "ok button", Toast.LENGTH_SHORT).show();
                    // Note that a user can make multiple donations

                    String url = "https://www.nuvolect.com/donate";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    m_act.startActivity(i);

                    cancelDialog();
                }

                /**
                 * Cancel is called with user hits cancel button, backbutton, changes apps or
                 * in any way dismisses the dialog.
                 */
                @Override
                public void cancel() {

                    if( ! cancelAlreadyCalled){

                        if( testDialogNoMetrics )
                            Toast.makeText(m_act, "cancel button", Toast.LENGTH_SHORT).show();

                        cancelDialog();

                        cancelAlreadyCalled = true;
                    }
                }

                @Override
                public void dontAskAgain() {

                    if( testDialogNoMetrics )
                        Toast.makeText(m_act, "don't ask again", Toast.LENGTH_SHORT).show();
                    else
                        putInt(m_act, dialogPrefix+DONT_ASK, 1);

                    cancelDialog();
                }
            });
        }
    }

    private static String DONT_ASK = "dont_ask";
    private static String LAUNCHES = "launches";
    private static String FIRST_LAUNCH = "first_launch";
    private static String NUMBER_OF_PROMPTS = "number_of_prompts";
    private static String LAUNCHES_SINCE_LAST_PROMPT = "launches_since_last_prompt";
    private static String TIME_OF_LAST_PROMPT = "time_of_last_prompt";
    private static String OK_ALREADY_SELECTED = "ok_already_selected";

    private static boolean shouldShowDialog(String dialogPrefix) {

        if( getInt(m_act, dialogPrefix+DONT_ASK) > 0)
            return false;

        if( getInt(m_act, dialogPrefix+OK_ALREADY_SELECTED) > 0)
            return false;

        int launches = 1 + getInt(m_act, dialogPrefix+LAUNCHES);
        putInt(m_act, dialogPrefix+LAUNCHES, launches);

        long time_of_first_launch = System.currentTimeMillis();
        if( launches == 1)
            putLong(m_act, dialogPrefix+FIRST_LAUNCH, time_of_first_launch);
        else
            time_of_first_launch = getLong(m_act, dialogPrefix+FIRST_LAUNCH);

        /**
         * The user must have a minimum number of launches before the first prompt.
         */
        if( launches < minLaunchesUntilInitialPrompt)
            return false;

        /**
         * Compare the minimum days after the first launch to the current time.
         * When the minimum days is in the future, it is too early to show the dialog.
         */
        if( time_of_first_launch + minDaysUntilInitialPrompt * MS_PER_DAY > System.currentTimeMillis())
            return false;

        /**
         * Make the initial launch
         */
        int number_of_prompts = getInt(m_act, dialogPrefix+NUMBER_OF_PROMPTS);
        if( number_of_prompts == 0 ){

            putInt(m_act,  dialogPrefix+NUMBER_OF_PROMPTS, 1 );
            putInt(m_act,  dialogPrefix+LAUNCHES_SINCE_LAST_PROMPT, 0);
            putLong(m_act, dialogPrefix+TIME_OF_LAST_PROMPT, System.currentTimeMillis());
            return true;
        }

        int launches_since_last_prompt = 1 + getInt(m_act, dialogPrefix+LAUNCHES_SINCE_LAST_PROMPT);
        putInt(m_act,dialogPrefix+LAUNCHES_SINCE_LAST_PROMPT, launches_since_last_prompt);

        if( launches_since_last_prompt < minLaunchesUntilNextPrompt)
            return false;

        long time_of_last_prompt = getLong(m_act, dialogPrefix+TIME_OF_LAST_PROMPT);
        if( time_of_last_prompt + minDaysUntilNextPrompt * MS_PER_DAY > System.currentTimeMillis())
            return false;

        putInt(m_act,  dialogPrefix+LAUNCHES_SINCE_LAST_PROMPT, 0);
        putInt(m_act,  dialogPrefix+NUMBER_OF_PROMPTS, 1 + number_of_prompts);
        putLong(m_act, dialogPrefix+TIME_OF_LAST_PROMPT, System.currentTimeMillis());

        return true;
    }

    public interface CustomDialogCallbacks{

        public void okButton();
        public void cancel();
        public void dontAskAgain();
    }

    private static CustomDialogCallbacks m_callbacks = null;

    /**
     * Start tips in a dialog and disable the show tips checkbox.
     */
    private static void showDialog( String title, String message, CustomDialogCallbacks callbacks){

        m_callbacks = callbacks;
        m_dialog = new Dialog(m_act);

        LayoutInflater myInflater = (LayoutInflater) m_act.getSystemService(m_act.LAYOUT_INFLATER_SERVICE);
        View view = myInflater.inflate(R.layout.custom_dialog, null);
        /**
         * Set the view to find fields from the Dialog
         */
        m_view = view;

        m_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_dialog.setContentView(view);
        m_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tv = (TextView) m_view.findViewById(R.id.titleCustomDialogTv);
        tv.setText( title );
        tv = (TextView) m_view.findViewById(R.id.messageCustomDialogTv);
        tv.setText( message );

        m_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                m_callbacks.cancel();
            }
        });
        m_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                m_callbacks.cancel();
            }
        });

        setButtonOnClicks(view); // Configure onClick callbacks for each button

        m_dialog.show();
    }

    private static void setButtonOnClicks(View view){

        view.findViewById(R.id.dontAskAgainCb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                m_callbacks.dontAskAgain();
            }
        });
        view.findViewById(R.id.okCustomDialogButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                m_callbacks.okButton();
            }
        });
        view.findViewById(R.id.cancelCustomDialogButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                m_callbacks.cancel();
            }
        });
    }

    private static void cancelDialog(){

        if( m_dialog != null && m_dialog.isShowing())
            m_dialog.cancel();
        m_dialog = null;
    }

    /**
     * Remove all persistent data.
     */
    public static void clearAll(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().clear().commit();
    }

    /**
     * Simple get int.  Return 0 if not found
     * @param ctx
     * @param key
     * @return
     */
    public static int getInt(Context ctx, String key) {

        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getInt(key, 0);
    }

    /**
     * Simple put value with the given key.
     * @param ctx
     * @param key
     * @param value
     */
    public static void putInt(Context ctx, String key, int value){
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        pref.edit().putInt(key, value).commit();
    }

    public static void putLong(Context ctx, String key, long value){
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        pref.edit().putLong(key, value).commit();
    }

    public static long getLong(Context ctx, String key) {

        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getLong(key, 0);
    }
}
