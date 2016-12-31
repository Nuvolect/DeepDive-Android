package com.nuvolect.deepdive.ddUtil;//

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;

/**
 * Send analytics data over time
 */
public class AnalyticsThrottle {

    private static AnalyticsThrottle singleton = null;
    private static Context m_ctx;
    private boolean idle;
    private long lastSentTime;
    private long MINIMUM_SEND_TIME = 2500L;// 2.5 seconds
    private ArrayList<String> categoryArrayList;
    private ArrayList<String> actionArrayList;
    private ArrayList<String> labelArrayList;
    private ArrayList<Long> valueArrayList;
    private Handler mHandler;

    private AnalyticsThrottle(){

        mHandler = new Handler();
        idle = true;
        lastSentTime = 0;

        categoryArrayList = new ArrayList<>();
        actionArrayList = new ArrayList<>();
        labelArrayList = new ArrayList<>();
        valueArrayList = new ArrayList<>();
    }

    public static AnalyticsThrottle getInstance(Context ctx){

        m_ctx = ctx;

        if( singleton == null){
            singleton = new AnalyticsThrottle();
        }
        return singleton;
    }

    public synchronized void send(String category, String action, String label, long value) {

        /**
         * Always queue the next batch even if it will be sent immediately.
         */
        categoryArrayList.add(category);
        actionArrayList.add(action);
        labelArrayList.add(label);
        valueArrayList.add(value);


        /**
         * Check for current and recent activity.
         * If none then send the first and record the time.
         */
        if( idle && System.currentTimeMillis() - lastSentTime > MINIMUM_SEND_TIME){

            sendNext();

        }else{

            /**
             * If idle, there is no timer yet but there is recent activity.
             * Set timer to to meet minimum time threshold.
             */
            if( idle){

                idle = false;
                long timeFromNow = System.currentTimeMillis() - lastSentTime;
                if( timeFromNow > MINIMUM_SEND_TIME)
                    timeFromNow = MINIMUM_SEND_TIME;

                mHandler.removeCallbacks(throttleTimer);
                mHandler.postDelayed( throttleTimer, timeFromNow);
            }else{
                /**
                 * Timer already set, it will restart the timer if the queue is not empty
                 */
            }
        }
    }

    private Runnable throttleTimer = new Runnable(){
        public void run(){

            sendNext();

            if( categoryArrayList.isEmpty()) {

                idle = true;
                mHandler.removeCallbacks(throttleTimer);
            }else{

                mHandler.removeCallbacks(throttleTimer);
                mHandler.postDelayed(throttleTimer, MINIMUM_SEND_TIME);
            }
        }
    };

    private void sendNext(){

        lastSentTime = System.currentTimeMillis();

        Analytics.send(m_ctx,
                categoryArrayList.get(0),
                actionArrayList.get(0),
                labelArrayList.get(0),
                valueArrayList.get(0));

        categoryArrayList.remove(0);
        actionArrayList.remove(0);
        labelArrayList.remove(0);
        valueArrayList.remove(0);

//        LogUtil.log("send, array size: "+categoryArrayList.size());
    }
}
