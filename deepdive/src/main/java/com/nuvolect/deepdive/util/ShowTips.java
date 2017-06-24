package com.nuvolect.deepdive.util;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nuvolect.deepdive.R;
import com.nuvolect.deepdive.settings.LobbySettingsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Show tips to the user.  This can be started on demand, can display a random tip or
 * show tips of a certain category.
 *
 * Tips can be embedded in a layout using an "include" statement or can be displayed from
 * a dialog.
 */
public class ShowTips {

    private static ShowTips singleton = null;
    private static Activity m_act;
    private static View m_view;
    private int m_currentTipIndex;
    private JSONArray m_tipArray = null;
//    private AlertDialog m_dialog = null;
    private Dialog m_dialog = null;

    public static ShowTips getInstance(Activity act){

        m_act = act;

        if( singleton == null){
            singleton = new ShowTips();
        }
        return singleton;
    }

    /**
     * Constructor, get things setup.  Build main JSON object.
     */
    private ShowTips() {

        /**
         * Load all tips from raw resources into a JSON array
         */
        String fileContents = "";
        StringBuilder sb = new StringBuilder();

        try {
            InputStream is = m_act.getResources().openRawResource(R.raw.tips);

            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) > 0) {

                String s = new String( buffer, 0, len, "UTF-8");
                sb.append( s );
            }
            fileContents = sb.toString();

            if( is != null)
                is.close();
        } catch (FileNotFoundException e) {
            LogUtil.logException(LogUtil.LogType.SHOW_TIPS, e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * Build the JSON array
         */
        try {

            JSONObject obj = new JSONObject( fileContents );
            m_tipArray = obj.getJSONArray("tips");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        m_currentTipIndex = Persist.getCurrentTip(m_act);
    }

    /**
     * Start tips embedded into the activities layout.
     */
    public void embeddedShowTips() {

        /**
         * Set the view to find fields from the Activity
         */
        m_view = m_act.findViewById(android.R.id.content);

        LinearLayout ll = (LinearLayout) m_view.findViewById(R.id.showTipsLl);

        if( ll != null) {

            /**
             * This method is called during startup and lifecycle events.
             * Show a new tip on startup or when users presses Next or Previous tip.
             */

            if (LobbySettingsActivity.getShowTips(m_act)) {

                showTip(getTip(bumpTip(1)));// get next tip
                setButtonOnClicks(m_view); // Configure onClick callbacks for each button
            }
            else
                ShowTips.getInstance(m_act).hideTips();
        }
    }

    /**
     * Start tips in a dialog and disable the show tips checkbox.
     */
    public void dialogShowTips(boolean showTipsCheckBox){

        m_dialog = new Dialog(m_act);

        LayoutInflater myInflater = (LayoutInflater) m_act.getSystemService(m_act.LAYOUT_INFLATER_SERVICE);
        View view = myInflater.inflate(R.layout.show_tips, null);

        if( ! showTipsCheckBox){

            CheckBox cb = (CheckBox) view.findViewById(R.id.showTipsCb);
            cb.setVisibility(CheckBox.GONE);
        }

        m_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_dialog.setContentView(view);
        m_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setButtonOnClicks(view); // Configure onClick callbacks for each button

        m_dialog.show();

        /**
         * Set the view to find fields from the Dialog
         */
        m_view = view;

        showTip(getTip(bumpTip(1)));// get next tip
    }

    private void setButtonOnClicks(View view){

        view.findViewById(R.id.showTipsCb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTipsCbOnClick(v);
            }
        });
        view.findViewById(R.id.closeTipsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTipsButtonOnClick(v);
            }
        });
        view.findViewById(R.id.previousTipButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousTipButtonOnClick(v);
            }
        });
        view.findViewById(R.id.nextTipButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextTipButtonOnClick(v);
            }
        });
    }

    /**
     * Advance or backup the index by bum and persist the value.
     * @param bump
     * @return
     */
    private int bumpTip(int bump){

        m_currentTipIndex += bump;

        if( m_currentTipIndex >= m_tipArray.length())
            m_currentTipIndex = 0;
        else {
            if (m_currentTipIndex < 0)
                m_currentTipIndex = m_tipArray.length() - 1;
        }

        Persist.setCurrentTip( m_act, m_currentTipIndex);
        return m_currentTipIndex;
    }

    private String getTip(int tipIndex ) {

        String tip = "";

        JSONObject obj = null;
        try {
            obj = m_tipArray.getJSONObject(tipIndex);
            tip =  obj.getString("tip");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tip;
    }


    public void showTip(String tip) {

        LinearLayout ll = (LinearLayout) m_view.findViewById(R.id.showTipsLl);
        ll.setVisibility(View.VISIBLE);

        TextView tv = (TextView) m_view.findViewById(R.id.tipTextTv);
        tv.setText(Html.fromHtml(tip, new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                Drawable drawFromPath;
                int path = m_act.getResources().getIdentifier(source, "drawable", m_act.getPackageName());
                drawFromPath = (Drawable) m_act.getResources().getDrawable(path);
                drawFromPath.setBounds(0, 0, drawFromPath.getIntrinsicWidth(),
                        drawFromPath.getIntrinsicHeight());

                /**
                 * Make transparent icons visible
                 */
//                drawFromPath.mutate().setColorFilter(new
//                        PorterDuffColorFilter(0xffff00, PorterDuff.Mode.MULTIPLY));

//                drawFromPath.setTint(m_act.getResources().getColor(R.color.navy));
                return drawFromPath;
            }
        }, null));

        tv.setMovementMethod(LinkMovementMethod.getInstance());

        /**
         * The checkbox will keep state independent of the SettingsActivity. Enable it tip is shown.
         */
        CheckBox cb = (CheckBox) m_view.findViewById(R.id.showTipsCb);
        cb.setChecked(true);
    }

    public void randomTip(){

        m_currentTipIndex = (int) (Math.random()* m_tipArray.length());
        bumpTip(0);// just persist it
        showTip(getTip(m_currentTipIndex));
    }

    public void nextTipButtonOnClick(View view) {

        showTip(getTip(bumpTip(1)));

        Analytics.send(m_act,
                Analytics.SHOW_TIP,
                Analytics.NEXT_TIP,
                Analytics.COUNT, 1);
    }

    public void previousTipButtonOnClick(View view) {

        showTip(getTip(bumpTip(-1)));

        Analytics.send(m_act,
                Analytics.SHOW_TIP,
                Analytics.PREVIOUS_TIP,
                Analytics.COUNT, 1);
    }

    public void closeTipsButtonOnClick(View view) {

        hideTips();

        Analytics.send(m_act,
                Analytics.SHOW_TIP,
                Analytics.CLOSE_TIPS,
                Analytics.COUNT, 1);
    }

    public void hideTips(){

        LinearLayout ll = (LinearLayout) m_view.findViewById(R.id.showTipsLl);
        ll.setVisibility(View.GONE);

        if( m_dialog != null && m_dialog.isShowing()){

            m_dialog.dismiss();
            m_dialog = null;
        }

        Analytics.send(m_act,
                Analytics.SHOW_TIP,
                Analytics.HIDE_TIPS,
                Analytics.COUNT, 1);
    }

    public void showTipsCbOnClick(View view) {

        boolean checked = ((CheckBox)view).isChecked();
        LobbySettingsActivity.setShowTips(m_act, checked);
        hideTips();
    }
}
