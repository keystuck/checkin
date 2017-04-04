package com.example.android.checkin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

// Receiver listens for the broadcast of "USER_PRESENT" by the system

public class ActionReceiver extends BroadcastReceiver {


    private static final String LOG_TAG = ActionReceiver.class.getSimpleName();


    public ActionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Access the settings to find out if this is the first time today the broadcast was received
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean usedToday = sharedPreferences.getBoolean("user_present_today", false);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Calendar mTimeReport = Calendar.getInstance();
        mTimeReport.setTimeInMillis(System.currentTimeMillis());
        String timeUsed = mTimeReport.get(Calendar.HOUR_OF_DAY) + ":" + mTimeReport.get(Calendar.MINUTE);
        editor.putString(context.getString(R.string.last_used_time), timeUsed);
        editor.apply();


        //if so, start the service
        if (!usedToday) {
  //          String sb = "";
  //          sb = sb + "Action: " + intent.getAction() + "\n";
  //          sb = sb + "URI: " + intent.toUri(Intent.URI_INTENT_SCHEME) + "\n";
  //          String log = sb;
 //           Log.d(LOG_TAG, log);
 //           Toast.makeText(context, log, Toast.LENGTH_LONG).show();

            Intent newServiceIntent = new Intent(context, CheckInService.class);
            newServiceIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.first_use_today));
            context.startService(newServiceIntent);
        }


        //if not, do nothing
    }
}
